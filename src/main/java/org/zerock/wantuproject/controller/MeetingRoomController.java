package org.zerock.wantuproject.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.wantuproject.dto.MeetingRoomDto;
import org.zerock.wantuproject.entity.*;
import org.zerock.wantuproject.repository.*;
import org.zerock.wantuproject.service.LikeService;
import org.zerock.wantuproject.service.MeetingRoomService;
import org.zerock.wantuproject.service.UserService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MeetingRoomController {


    private final MeetingRoomRepository meetingRoomRepository;
    private final UserRepository userRepository;
    private final LikeService likeService;
    private final UserService userService;
    private final MeetingRoomParticipantRepository meetingRoomParticipantRepository;
    private final MeetingMessageRepository meetingMessageRepository;
    private final MeetingRoomService meetingRoomService;
    private static final Logger logger = LoggerFactory.getLogger(MeetingRoomController.class);
    private final LikeRepository likeRepository;

    // 단체 채팅방 생성 페이지로 이동
    @GetMapping("/createMeetingRoom")
    public String showCreateMeetingRoomForm() {
        return "meeting/meetingRoomRegister";  // createMeetingRoom.html 템플릿으로 이동
    }

    @PostMapping("/createMeetingRoom")
    @Transactional
    public String createMeetingRoom(
            @RequestParam("title") String title,
            @RequestParam("image") MultipartFile image,
            @RequestParam("interests") String interests,
            @RequestParam("address") String address,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("description") String description,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 로그인한 사용자의 사용자명 (username) 가져오기

        // 이미지 파일 저장 처리
        String imagePath = "";
        if (!image.isEmpty()) {
            try {
                String fileName = image.getOriginalFilename();
                String uploadDir = "C:/uploads";  // 업로드 경로 수정
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs(); // 폴더가 없으면 생성
                }
                imagePath = uploadDir + "/" + UUID.randomUUID().toString() + "_" + fileName;
                File dest = new File(imagePath);
                image.transferTo(dest);
            } catch (IOException e) {
                e.printStackTrace();
                return "이미지 업로드 실패";  // 단순 문자열 반환
            }
        }

        // 모임방 생성 및 저장
        MeetingRoom meetingRoom = MeetingRoom.builder()
                .title(title)
                .imagePath(imagePath)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .interests(Arrays.asList(interests.split("\\s*,\\s*"))) // 쉼표로 구분하여 리스트로 변환
                .description(description)
                .build();
        log.info("Saving meeting room: " + meetingRoom);
        meetingRoomRepository.save(meetingRoom);
        log.info("Meeting room saved with ID: " + meetingRoom.getId());

        // 방 생성 후 채팅방 참여로 리다이렉트
        return "redirect:/joinChatRoom/" + meetingRoom.getId();
    }


    // AJAX 요청을 처리하여 JSON 형식으로 채팅방 목록 반환
    @GetMapping("/getMeetingRooms")
    @ResponseBody  // JSON 형식으로 반환할 때 필요
    public ResponseEntity<List<String>> getMeetingRooms(@RequestParam String markerTitle) {
        List<String> meetingRooms = new ArrayList<>();

        if ("모임방 1".equals(markerTitle)) {
            meetingRooms.add("마포구 러닝크루");
            meetingRooms.add("홍대 걷고싶은 사람들");
        } else if ("모임방 2".equals(markerTitle)) {
            meetingRooms.add("신촌 산책모임");
            meetingRooms.add("홍대 다이어트 런닝");
        } else {
            meetingRooms.add("모임방 없음");
        }

        return ResponseEntity.ok(meetingRooms);  // JSON으로 채팅방 목록 반환
    }


    // 모든 채팅방 데이터를 불러오는 엔드포인트
    @GetMapping("/getAllMeetingRooms")
    @ResponseBody
    public List<MeetingRoom> getAllMeetingRooms() {
        return meetingRoomRepository.findAll();
    }

    // 초기 위치를 기준으로 20km 이내의 채팅방 정보 가져오기
    @GetMapping("/getNearbyMeetingRooms")
    @ResponseBody
    public String getNearbyMeetingRooms(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MeetingRoom> meetingRooms = meetingRoomRepository.findByLocationWithin(latitude, longitude, 20.0, pageable);

        // HTML 형식으로 채팅방 목록 반환
        return meetingRooms.getContent().stream()
                .map(room -> "<div class='meeting-room'>" + room.getTitle() + " - " + room.getDescription() + "</div>")
                .collect(Collectors.joining()) +
                "<div class='pagination'>" + generatePaginationLinks(meetingRooms) + "</div>";
    }

    // HTML 페이지를 로드하는 기본 GET 메서드
    @GetMapping("/meetingRoomsMap")
    public String getMeetingRoomsMap() {
        // 이 메서드는 단순히 HTML 페이지를 반환합니다.
        return "meeting/meetingList"; // 지도를 표시하는 HTML 페이지
    }

    @GetMapping("/getUserLocation")
    @ResponseBody
    public ResponseEntity<Map<String, Double>> getUserLocation(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());

        // 외부 API 호출을 통해 주소를 위도와 경도로 변환하는 코드
        double[] coordinates = geocodeAddress(user.getAddress()); // 주소 -> 좌표 변환

        Map<String, Double> locationMap = new HashMap<>();
        locationMap.put("latitude", coordinates[0]);
        locationMap.put("longitude", coordinates[1]);

        return ResponseEntity.ok(locationMap);  // 좌표를 JSON으로 반환
    }

    @GetMapping("/getMeetingRoomsByLocation")
    @ResponseBody
    public ResponseEntity<List<MeetingRoomDto>> getMeetingRoomsByLocation(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MeetingRoom> meetingRooms = meetingRoomRepository.findByLatitudeAndLongitude(latitude, longitude, pageable);

        List<MeetingRoomDto> meetingRoomDtos = meetingRooms.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(meetingRoomDtos);
    }

    private String generatePaginationLinks(Page<MeetingRoom> meetingRooms) {
        StringBuilder paginationHtml = new StringBuilder();

        // 이전 페이지 링크
        if (meetingRooms.hasPrevious()) {
            paginationHtml.append("<a href='#' onclick='loadPage(" + (meetingRooms.getNumber() - 1) + ")'>이전</a>");
        }

        // 페이지 번호 링크
        for (int i = 0; i < meetingRooms.getTotalPages(); i++) {
            paginationHtml.append("<a href='#' onclick='loadPage(" + i + ")'>" + (i + 1) + "</a>");
        }

        // 다음 페이지 링크
        if (meetingRooms.hasNext()) {
            paginationHtml.append("<a href='#' onclick='loadPage(" + (meetingRooms.getNumber() + 1) + ")'>다음</a>");
        }

        log.info("Generated Pagination HTML: " + paginationHtml.toString()); // 페이징 HTML 로그 출력
        return paginationHtml.toString();
    }

    @GetMapping("/searchMeetingRooms")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchMeetingRooms(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "interest", required = false) String interest,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MeetingRoom> meetingRooms;

        // 검색 조건에 따라 MeetingRoom 목록을 조회
        if ((title == null || title.isEmpty()) && (interest == null || interest.isEmpty())) {
            meetingRooms = meetingRoomRepository.findAll(pageable);
        } else if (title != null && !title.isEmpty()) {
            meetingRooms = meetingRoomRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
        } else if (interest != null && !interest.isEmpty()) {
            meetingRooms = meetingRoomRepository.findByInterestsContainingIgnoreCase(interest.trim(), pageable);
        } else {
            meetingRooms = meetingRoomRepository.findAll(pageable);
        }

        List<MeetingRoomDto> meetingRoomDtos = meetingRooms.getContent().stream()
                .map(room -> new MeetingRoomDto(
                        room.getId(),
                        room.getTitle(),
                        room.getImagePath(),
                        room.getInterests(),
                        room.getParticipants().size(),
                        room.getLikes(),
                        room.getAddress(),
                        room.getDescription()
                ))
                .collect(Collectors.toList());

        // JSON 응답 형식으로 totalPages와 currentPage 포함
        Map<String, Object> response = new HashMap<>();
        response.put("meetingRooms", meetingRoomDtos);
        response.put("totalPages", meetingRooms.getTotalPages());
        response.put("currentPage", meetingRooms.getNumber());

        return ResponseEntity.ok(response);
    }


    // 좋아요 기능
    @PostMapping("/likeRoom")
    @ResponseBody
    public ResponseEntity<Integer> likeRoom(@RequestParam("roomId") Long roomId) {
        MeetingRoom room = meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setLikes(room.getLikes() + 1);  // 좋아요 수 증가
        meetingRoomRepository.save(room);
        return ResponseEntity.ok(room.getLikes());  // 업데이트된 좋아요 수 반환
    }

    @PostMapping("/likeMeetingRoom")
    public ResponseEntity<LikeResponse> likeMeetingRoom(@RequestParam("roomId") Long roomId, Authentication authentication) {
        try {
            // 좋아요 추가/취소 처리 및 최신 좋아요 상태와 좋아요 수 반환
            LikeResponse response = likeService.toggleLike(roomId, authentication);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    public static class LikeResponse {
        private boolean isLiked; // 필드 이름을 'isLiked'로 변경
        private int likesCount;

        // 기본 생성자
        public LikeResponse() {}

        public LikeResponse(boolean isLiked, int likesCount) {
            this.isLiked = isLiked;
            this.likesCount = likesCount;
        }

        public boolean isLiked() {
            return isLiked;
        }

        public void setIsLiked(boolean isLiked) {
            this.isLiked = isLiked;
        }

        public int getLikesCount() {
            return likesCount;
        }

        public void setLikesCount(int likesCount) {
            this.likesCount = likesCount;
        }
    }

    // 외부 API 호출을 통해 주소를 위도와 경도로 변환하는 메서드 (Kakao API 사용 예시)
    private double[] geocodeAddress(String address) {
        String apiKey = "945f5139b696bdaf8d654cc08372c269";  // 카카오 API 키를 여기에 입력


        try {
            // 주소를 URL 인코딩
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encodedAddress;

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "KakaoAK " + apiKey);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 응답을 JSON 객체로 변환
            String responseBody = response.toString();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // JSON 파싱하여 위도와 경도를 추출
            JSONArray documents = jsonResponse.getJSONArray("documents");
            if (documents.length() > 0) {
                JSONObject addressObject = documents.getJSONObject(0).getJSONObject("address");
                double latitude = addressObject.getDouble("y");  // 위도
                double longitude = addressObject.getDouble("x");  // 경도

                return new double[]{latitude, longitude};
            } else {
                throw new RuntimeException("해당 주소에 대한 좌표 정보를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return new double[]{0, 0};  // 실패 시 기본값 반환

    }

    private MeetingRoomDto convertToDto(MeetingRoom room) {
        return new MeetingRoomDto(
                room.getId(),
                room.getTitle(),
                room.getImagePath(),
                room.getInterests(),
                room.getParticipants().size(),
                room.getLikes(),
                room.getAddress(),
                room.getDescription()
        );
    }

    // 채팅방 상세 페이지 조회
    @GetMapping("/meetingRoomDetail/{roomId}")
    public String getMeetingRoomDetail(@PathVariable Long roomId, Model model) {
        // 서비스 레이어에서 DTO를 통해 채팅방 정보 조회
        MeetingRoomDto meetingRoom = meetingRoomService.getMeetingRoomById(roomId);

        // Model에 채팅방 정보 추가
        model.addAttribute("meetingRoom", meetingRoom);

        // meetingRoomDetail.html 템플릿을 반환하여 상세페이지로 이동
        return "meeting/meetingRoomDetail";
    }

    @GetMapping("/joinChatRoom/{roomId}")
    public String joinChatRoom(
            @PathVariable Long roomId,
            Authentication authentication,
            Model model) {

        // 현재 로그인한 사용자 정보 가져오기
        User user = userService.findByUsername(authentication.getName());
        log.info("Retrieved User: " + user);

        // 채팅방 정보 가져오기
        MeetingRoom meetingRoom = meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 사용자가 이미 채팅방에 참여했는지 확인
        Optional<MeetingRoomParticipant> participantOpt = meetingRoomParticipantRepository.findByMeetingRoomAndUser(meetingRoom, user);

        List<MeetingMessage> messages;

        if (participantOpt.isEmpty()) {
            // 새로운 참여자: 사용자 정보를 채팅방에 추가
            MeetingRoomParticipant newParticipant = MeetingRoomParticipant.builder()
                    .user(user)
                    .meetingRoom(meetingRoom)
                    .joinedAt(LocalDateTime.now())  // 참여 시간 저장
                    .build();

            meetingRoom.addParticipant(newParticipant);
            meetingRoomParticipantRepository.save(newParticipant);

            // 참여 후 작성된 메시지만 로드
            messages = meetingMessageRepository.findByMeetingRoomAndSentAtAfter(meetingRoom, newParticipant.getJoinedAt());
            model.addAttribute("isNewParticipant", true);  // 새로운 참여자임을 표시
        } else {
            // 기존 참여자: 모든 메시지 로드
            messages = meetingMessageRepository.findByMeetingRoom(meetingRoom);
            model.addAttribute("isNewParticipant", false);  // 기존 참여자임을 표시
        }

        // 메시지 및 채팅방 정보 추가
        model.addAttribute("messages", messages);
        MeetingRoomDto meetingRoomDto = convertToDto(meetingRoom);
        model.addAttribute("meetingRoom", meetingRoomDto);

        // 모델에 roomId와 사용자 정보 추가
        model.addAttribute("roomId", roomId);
        model.addAttribute("usersname",user.getName());
        model.addAttribute("username",user.getUid());
        model.addAttribute("userId", user.getId());

        // 채팅방 상세 페이지로 이동
        return "meeting/meetingRoom";
    }

    @GetMapping("/getLikes")
    public ResponseEntity<Integer> getLikesCount(@RequestParam("roomId") Long roomId) {
        try {
            int likesCount = likeService.getLikesCount(roomId);
            return ResponseEntity.ok(likesCount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(0);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(0);
        }
    }


}
