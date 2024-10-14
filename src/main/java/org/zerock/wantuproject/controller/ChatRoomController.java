package org.zerock.wantuproject.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.zerock.wantuproject.dto.AddUserRequest;
import org.zerock.wantuproject.dto.ChatRoomDTO;
import org.zerock.wantuproject.entity.ChatRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.UserRepository;
import org.zerock.wantuproject.service.ChatMessageService;
import org.zerock.wantuproject.service.ChatRoomService;
import org.zerock.wantuproject.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);
    private final UserRepository userRepository;

    @PostMapping("/match")
    public ModelAndView matchAndRedirect(String interestName, Model model) {
        // 인증 객체로부터 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        String username = authentication.getName();

        // 1. 현재 사용자 정보 로그
        User currentUser = userService.findByUsername(username);
        System.out.println("로그인된 사용자: " + currentUser);

        Long userId = currentUser.getId();
        model.addAttribute("loggedInUserId", userId); // 로그인 사용자 ID 전달

        // 2. 매칭 시작 로그
        System.out.println("매칭 시작 - 사용자: " + username + ", 관심사: " + interestName);

        model.addAttribute("user", currentUser); // user 객체와 관심사 데이터를 템플릿에 전달

        ChatRoomDTO chatRoomDTO = chatRoomService.matchAndCreateRoom(userId);

        // 3. 채팅방 결과 로그
        System.out.println("채팅방 생성 결과: " + chatRoomDTO);

        ModelAndView modelAndView = new ModelAndView("chat/chatroom");
        modelAndView.addObject("roomId", chatRoomDTO.getRoomid());
        System.out.println("----------------------------------------------------------------"+chatRoomDTO.getOtherUser().getId());

        if (chatRoomDTO.getOtherUser() == null) {
            System.out.println("상대방을 찾을 수 없습니다.");
            modelAndView.addObject("message", "상대방을 찾을 수 없습니다.");
            modelAndView.addObject("otherUser", null);
            modelAndView.addObject("profileImageUrls", null);
            modelAndView.addObject("otherUserId", null);  // 상대방 사용자 ID 추가
        } else {
            modelAndView.addObject("otherUser", chatRoomDTO.getOtherUser());
            modelAndView.addObject("profileImageUrls", chatRoomDTO.getProfileImageUrls());
            modelAndView.addObject("otherUserId", chatRoomDTO.getOtherUser().getId());  // 상대방 사용자 ID 추가
        }

        return modelAndView;
    }

    // uid로 다른 상대 찾기
    @PostMapping("/findAnotherUser/{userUid}")
    public ResponseEntity<ChatRoomDTO> findAnotherUser(@PathVariable String userUid) {
        try {
            // 새로운 상대를 찾고 새로운 채팅방을 생성
            ChatRoomDTO newRoomDTO = chatRoomService.matchAndCreateNewRoom(userUid);

            // 기존 채팅방의 메시지 삭제
            chatMessageService.deleteMessagesByRoomId(newRoomDTO.getRoomid());



            // 새로운 채팅방 정보 반환
            return ResponseEntity.ok(newRoomDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // 에러 발생 시 응답 처리
        }
    }

    @PostMapping("/likeAndSave/{roomId}")
    public ResponseEntity<ChatRoomDTO> likeAndSaveRoom(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> payload) {

        // likerUid는 String으로 받아서 처리
        String likerUid = (String) payload.get("likerUid"); // 좋아요를 누른 사용자의 UID
        Object likedUserIdObj = payload.get("likedUserId");


        // likedUserId는 String으로 받을 수 있으므로 Long으로 변환
        String likedUserIdStr = (String) payload.get("likedUserId"); // 좋아요를 받은 사용자의 ID(String)
        Long likedUserId = null;

        try {
            likedUserId = Long.parseLong(likedUserIdStr); // String을 Long으로 변환
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid likedUserId: " + likedUserIdStr);
        }

        // 로그로 확인
        log.info("Liker UID: {}", likerUid);
        log.info("Liked User ID: {}", likedUserId);

        // 1. 좋아요 기록을 저장 (likerUid는 String, likedUserId는 Long)
        userService.likeUserByUidAndId(likerUid, likedUserId);

        // 2. 현재 채팅방을 저장
        chatRoomService.saveChatRoom(roomId);

        // 3. 새로운 사용자와 매칭하여 새로운 채팅방 생성 (likerUid를 넘김)
        ChatRoomDTO newChatRoom = chatRoomService.matchAndCreateNewRoom(likerUid);

        // 4. 새로운 채팅방 정보 반환
        return ResponseEntity.ok(newChatRoom);
    }

    @GetMapping("/selectProfile/{profileId}")
    public ResponseEntity<ChatRoomDTO> selectProfile(@PathVariable Long profileId) {
        try {
            ChatRoomDTO chatRoomDTO = chatRoomService.getChatRoomByProfileId(profileId);
            return ResponseEntity.ok(chatRoomDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/findMultipleUsers")
    public String findMultipleUsers(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());

        // 성별이 다른 3명의 유저를 랜덤으로 가져옴 (기존 채팅방이 없는 유저만)
        List<User> potentialMatches = chatRoomService.getRandomUsersByGenderExcludingExistingRooms(
                currentUser.getId(), currentUser.getGender());

        // 유저 리스트를 DTO로 변환
        List<AddUserRequest> userDtos = potentialMatches.stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());

        // 모델에 필요한 정보 추가
        model.addAttribute("randomUsers", userDtos);
        model.addAttribute("loggedInUserId", currentUser.getId());
        model.addAttribute("loggedInUserUid", currentUser.getUid());
        model.addAttribute("loggedInUserName", currentUser.getName());

        return "chat/profileChatroom";
    }

    @PostMapping("/startChatWithSelectedUser")
    public ResponseEntity<ChatRoomDTO> startChatWithSelectedUser(@RequestParam Long selectedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());

        // 1. 현재 사용자와 선택된 사용자 간의 채팅방이 이미 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomService.findExistingChatRoom(currentUser.getId(), selectedUserId);

        // 2. 채팅방이 이미 존재하는 경우, 채팅방을 반환하지 않음
        if (existingRoom.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // 이미 존재하는 채팅방이 있을 경우 에러 응답
        }

        // 3. 채팅방이 없을 경우 새로운 채팅방을 생성
        ChatRoomDTO newChatRoom = chatRoomService.createChatRoomWithSelectedUser(currentUser.getId(), selectedUserId);

        // 4. 생성된 채팅방의 roomId가 없는 경우 에러 반환
        if (newChatRoom.getRoomid() == null) {
            return ResponseEntity.badRequest().body(null); // roomId가 없는 경우 에러 반환
        }

        return ResponseEntity.ok(newChatRoom); // roomId가 포함된 새로운 채팅방 정보 반환
    }

    @PostMapping("/findNewUser/{userUid}")
    public ResponseEntity<Map<String, Object>> findNewUser(@PathVariable String userUid, @RequestBody Map<String, Object> payload) {
        try {
            // 1. 현재 사용자 조회
            logger.info("findNewUser - 요청받은 userUid: {}", userUid);

            User currentUser = userService.findByUid(userUid)
                    .orElseThrow(() -> new RuntimeException("User not found with uid: " + userUid));

            logger.info("findNewUser - 조회된 사용자: {}, 사용자 ID: {}", currentUser.getName(), currentUser.getId());

            // 2. 기존 선택된 사용자 목록
            List<Long> selectedUserIds = ((List<?>) payload.get("selectedUserIds"))
                    .stream()
                    .map(id -> Long.parseLong(id.toString()))
                    .collect(Collectors.toList());

            logger.info("findNewUser - 선택된 사용자 ID 목록: {}", selectedUserIds);

            // 3. 기존 채팅방이 있는 경우 채팅 메시지만 삭제
            Optional<ChatRoom> existingRoom = chatRoomService.getExistingChatRoomWithUserIds(currentUser.getId(), selectedUserIds);
            if (existingRoom.isPresent()) {
                chatMessageService.deleteMessagesByRoomId(existingRoom.get().getRoomid());
                logger.info("기존 채팅방의 메시지 삭제됨: roomId = {}", existingRoom.get().getRoomid());
            }

            // 4. 새로운 상대를 찾기만 하고 채팅방을 생성하지 않음
            List<User> newUsers = chatRoomService.matchAndCreateNewRooms(userUid, selectedUserIds);
            logger.info("findNewUser - 선택된 3명의 새로운 사용자: {}", newUsers.stream()
                    .map(user -> "ID: " + user.getId() + ", 이름: " + user.getName())
                    .collect(Collectors.toList()));

            // 5. 반환할 사용자 DTO 리스트로 변환 (새로운 상대방 목록만 제공)
            List<AddUserRequest> userDtos = newUsers.stream()
                    .map(userService::convertToDto)
                    .collect(Collectors.toList());

            logger.info("findNewUser - 변환된 사용자 DTO: {}", userDtos);

            // 6. 응답 데이터를 Map으로 구성 (채팅방 생성하지 않음)
            Map<String, Object> response = new HashMap<>();
            response.put("users", userDtos);  // 사용자 정보만 반환

            logger.info("findNewUser - 최종 응답 데이터: users: {}", userDtos);

            return ResponseEntity.ok(response);  // 3명의 사용자 정보만 반환
        } catch (Exception e) {
            logger.error("새 사용자를 찾는 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 에러 발생 시 처리
        }
    }

    @PostMapping("/likeAndChatSave/{roomId}")
    public ResponseEntity<Map<String, Object>> likeAndSaveChatRoom(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> payload) {

        String likerUid = (String) payload.get("likerUid");
        Long likedUserId = Long.valueOf((Integer) payload.get("likedUserId")); // Integer -> Long

        log.info("Liker UID: {}", likerUid);
        log.info("Liked User ID: {}", likedUserId);

        // 사용자가 좋아요를 누른 사용자 처리
        userService.likeUserByUidAndId(likerUid, likedUserId);
        chatRoomService.saveChatRoom(roomId);

        try {
            // 사용자 UID를 로그에 출력
            logger.info("findNewUser - 요청받은 userUid: {}", likerUid);
            User currentUser = userService.findByUid(likerUid)
                    .orElseThrow(() -> new RuntimeException("User not found with uid: " + likerUid));

            logger.info("findNewUser - 조회된 사용자: {}, 사용자 ID: {}", currentUser.getName(), currentUser.getId());

            // selectedUserIds를 안전하게 처리
            Object selectedUserIdsObj = payload.get("selectedUserIds");

            // selectedUserIds가 null인지 확인
            if (selectedUserIdsObj == null) {
                throw new IllegalArgumentException("selectedUserIds cannot be null.");
            }

            // selectedUserIds가 List<Integer>인지 확인
            if (!(selectedUserIdsObj instanceof List<?>)) {
                throw new IllegalArgumentException("selectedUserIds must be a List.");
            }

            // selectedUserIds에서 Integer를 Long으로 변환
            List<Long> selectedUserIds = ((List<?>) payload.get("selectedUserIds"))
                    .stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue(); // Integer를 Long으로 변환
                        } else if (id instanceof String) {
                            return Long.valueOf((String) id); // String을 Long으로 변환
                        } else {
                            throw new IllegalArgumentException("Invalid user ID type.");
                        }
                    })
                    .collect(Collectors.toList());


            logger.info("findNewUser - 선택된 사용자 ID 목록: {}", selectedUserIds);

            if (selectedUserIds.isEmpty()) {
                throw new IllegalArgumentException("selectedUserIds must not be empty.");
            }

            List<User> newUsers = chatRoomService.matchAndCreateNewRooms(likerUid, selectedUserIds);
            if (newUsers.isEmpty()) {
                logger.warn("findNewUser - 새로운 사용자가 발견되지 않았습니다.");
            }

            logger.info("findNewUser - 선택된 3명의 새로운 사용자: {}", newUsers.stream()
                    .map(user -> "ID: " + user.getId() + ", 이름: " + user.getName())
                    .collect(Collectors.toList()));

            List<AddUserRequest> userDtos = newUsers.stream()
                    .map(userService::convertToDto)
                    .collect(Collectors.toList());

            logger.info("findNewUser - 변환된 사용자 DTO: {}", userDtos);

            Map<String, Object> response = new HashMap<>();
            response.put("users", userDtos);

            logger.info("findNewUser - 최종 응답 데이터: users: {}", userDtos);

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            logger.error("새 사용자를 찾는 중 에러 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    // 특정 채팅방 조회 및 진입
    @GetMapping("/{roomId}")
    public ModelAndView getChatRoom(@PathVariable Long roomId, Model model) {
        log.info("Entering getChatRoom with roomId: {}", roomId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        String username = authentication.getName();

        // 1. 현재 사용자 정보 로그
        User currentUser = userService.findByUsername(username);
        System.out.println("로그인된 사용자: " + currentUser);

        Long userId = currentUser.getId();
        model.addAttribute("loggedInUserId", userId); // 로그인 사용자 ID 전달

        // 채팅방 정보 가져오기
        ChatRoom chatRoom = chatRoomService.findById(roomId);
        if (chatRoom == null) {
            log.error("ChatRoom not found with roomId: {}", roomId);
            throw new RuntimeException("Chat room not found");
        }

        User otherUser = chatRoom.getUser1().equals(currentUser) ? chatRoom.getUser2() : chatRoom.getUser1();

        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("user1", chatRoom.getUser1());
        model.addAttribute("user2", chatRoom.getUser2());
        model.addAttribute("otherUserId", otherUser.getId());
        model.addAttribute("messages", chatRoomService.getMessagesByRoom(chatRoom));

        return new ModelAndView("chat/chatroom");
    }



}








