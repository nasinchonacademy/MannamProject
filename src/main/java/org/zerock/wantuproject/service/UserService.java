package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.zerock.wantuproject.controller.MainController;
import org.zerock.wantuproject.dto.AddUserRequest;
import org.zerock.wantuproject.entity.Interest;
import org.zerock.wantuproject.entity.LikedUser;
import org.zerock.wantuproject.entity.ProfileImage;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.LikedUserRepository;
import org.zerock.wantuproject.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(MainController.class); // 로거 선언
    private final LikedUserRepository likedUserRepository;


    public Long save(AddUserRequest dto, List<String> savedFileNames) {
        // User 객체 생성
        User user = User.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .uid(dto.getUid())
                .birthDate(dto.getBirthDate())
                .nickname(dto.getNickname())
                .introduce(dto.getIntroduce())
                .gender(dto.getGender())
                .address(dto.getAddress())
                .build();

        // 프로필 이미지 추가 (여러 개 처리)
        if (savedFileNames != null && !savedFileNames.isEmpty()) {
            List<ProfileImage> profileImages = savedFileNames.stream()
                    .map(fileName -> ProfileImage.builder()
                            .imageUrl(fileName)  // 저장된 파일 이름 저장
                            .user(user)
                            .build())
                    .collect(Collectors.toList());

            System.out.println("프로필 이미지 저장service: " + profileImages);

            // 프로필 이미지를 유저에 추가
            user.setProfileImages(profileImages);
        }

        // 관심사 추가
        List<Interest> interests = dto.getInterests().stream()
                .map(interestName -> Interest.builder()
                        .interestName(interestName)
                        .user(user)
                        .build())
                .collect(Collectors.toList());
        user.setInterests(interests);


        // User 저장 및 ID 반환
        return userRepository.save(user).getId();
    }

    // 아이디 중복 여부 확인
    public boolean isUsernameTaken(String uid) {
        return userRepository.existsByUid(uid);
    }

    public AddUserRequest convertToDto(User user) {
        AddUserRequest dto = new AddUserRequest();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUid(user.getUid());
        dto.setPassword(user.getPassword());
        dto.setNickname(user.getNickname());
        dto.setName(user.getName());
        dto.setGender(user.getGender());
        dto.setIntroduce(user.getIntroduce());
        dto.setBirthDate(user.getBirthDate());
        dto.setAddress(user.getAddress());
        dto.setLikeCount(user.getLikeCount());
        dto.setInterests(user.getInterests().stream()
                .map(interest -> interest.getInterestName())
                .collect(Collectors.toList()));

        // 프로필 이미지 URL을 profileImage 필드에 설정
        dto.setProfileImage(user.getProfileImages().stream()
                .map(ProfileImage::getImageUrl)
                .collect(Collectors.toList()));

        // 프로필 이미지 경로는 별도의 변수로 처리하고 템플릿에 전달
        List<String> profileImageUrls = user.getProfileImages().stream()
                .map(ProfileImage::getImageUrl)
                .collect(Collectors.toList());

        // 템플릿에 profileImageUrls 전달 (여기서는 model에 직접 넣거나 필요한 곳에서 사용)
        // 예시로 반환된 dto에 추가하지 않음
        return dto;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByUsername(String uid) {
        return userRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + uid));
    }

    public Optional<User> findByUid(String uid) {
        logger.info("DB에서 uid로 사용자 조회: {}", uid);
        return userRepository.findByUid(uid);
    }

    // 사용자 간 좋아요 로직 구현 (ID로 처리)
    // 사용자 간 좋아요 로직 구현 (likerUid는 String, likedUserId는 Long)
    public void likeUserByUidAndId(String likerUid, Long likedUserId) {
        // liker는 UID로 조회
        User liker = userRepository.findByUid(likerUid)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + likerUid));
        // likedUser는 ID로 조회
        User likedUser = userRepository.findById(likedUserId)
                .orElseThrow(() -> new RuntimeException("대상 사용자를 찾을 수 없습니다: " + likedUserId));

        // 이미 좋아요를 눌렀는지 확인
        if (!likedUserRepository.existsByLikerAndLikedUser(liker, likedUser)) {
            // 좋아요 기록이 없으면 새롭게 저장
            LikedUser likedUserEntity = LikedUser.builder()
                    .liker(liker)
                    .likedUser(likedUser)
                    .likedAt(LocalDateTime.now())
                    .build();

            likedUserRepository.save(likedUserEntity); // 좋아요 기록 저장
        }
    }

    public List<User> getRandomUsersByGender(Long currentUserId, String currentGender) {
        List<User> randomUsers = userRepository.findRandomUsersByGender(currentUserId, currentGender, PageRequest.of(0, 3));
        return randomUsers;
    }

    public List<String> getProfileImageUrls(User user) {
        // 유저의 프로필 이미지 URL들을 가져옴
        return user.getProfileImages().stream()
                .map(ProfileImage::getImageUrl)
                .collect(Collectors.toList());
    }




}

