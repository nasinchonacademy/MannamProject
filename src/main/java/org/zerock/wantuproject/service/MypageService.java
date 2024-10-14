package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.wantuproject.controller.FileController;
import org.zerock.wantuproject.entity.*;
import org.zerock.wantuproject.repository.ChatRoomRepository;
import org.zerock.wantuproject.repository.MeetingRoomParticipantRepository;
import org.zerock.wantuproject.repository.UserRepository;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MeetingRoomParticipantRepository meetingRoomParticipantRepository;


    public User getUserProfile(String uid) {
        return userRepository.findByUid(uid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private final FileController fileController;


    public List<LikedUser> getLikesGiven(String uid) {
        User user = getUserProfile(uid);
        return user.getLikedUsers();
    }

    public List<LikedUser> getLikesReceived(String uid) {
        User user = getUserProfile(uid);
        return user.getLikedByUsers();
    }

    public List<ChatRoom> getChatRooms(String uid) {
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 사용자가 참여한 모든 채팅방 목록을 가져옴
        List<ChatRoom> chatRooms = new ArrayList<>();

        // user1으로 참여한 채팅방
        chatRooms.addAll(chatRoomRepository.findByUser1(user));

        // user2으로 참여한 채팅방
        chatRooms.addAll(chatRoomRepository.findByUser2(user));

        return chatRooms;
    }


    public List<MeetingRoom> getMeetingRooms(String uid) {
        List<MeetingRoomParticipant> participants = meetingRoomParticipantRepository.findByUserUid(uid);

        List<MeetingRoom> meetingRooms = new ArrayList<>();

        if (participants == null || participants.isEmpty()) {
            System.out.println("No meeting rooms found for user with uid: " + uid);
        } else {
            for (MeetingRoomParticipant participant : participants) {
                if (participant != null && participant.getMeetingRoom() != null) {
                    meetingRooms.add(participant.getMeetingRoom());
                    System.out.println("Meeting room added: " + participant.getMeetingRoom().getTitle());
                } else {
                    System.out.println("Participant found without meeting room or null participant: " + (participant != null ? participant.getId() : "null"));
                }
            }
        }

        System.out.println("Final meeting rooms for user with uid: " + uid + " are: " + meetingRooms);
        return meetingRooms;
    }

    public User getUserByUid(String uid) {
        Optional<User> userOptional = userRepository.findByUid(uid);
        return userOptional.orElse(null);
    }


    @Transactional
    public void updateUserProfile(String uid, String introduce, String address, List<String> interests,
                                  List<String> existingProfileImages, MultipartFile[] newProfileImages) throws IOException {
        Optional<User> userOptional = userRepository.findByUid(uid);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIntroduce(introduce);
            user.setAddress(address);

            log.info("User found: {}", user);

            // 관심사 업데이트
            if (interests != null && !interests.isEmpty()) {
                List<Interest> newInterests = interests.stream()
                        .map(interestName -> Interest.builder().interestName(interestName).user(user).build())
                        .collect(Collectors.toList());
                user.getInterests().clear();
                user.getInterests().addAll(newInterests);
                log.info("Updated interests: {}", newInterests);
            }

            // 프로필 이미지 업데이트
            List<ProfileImage> currentImages = user.getProfileImages();

            // 기존 프로필 이미지 유지
            if (existingProfileImages != null && !existingProfileImages.isEmpty()) {
                currentImages.removeIf(image -> !existingProfileImages.contains(image.getImageUrl()));
                log.info("Remaining profile images after existing images check: {}", currentImages);
            } else {
                // 기존 이미지 모두 삭제
                currentImages.clear();
                log.info("All existing profile images cleared.");
            }

            // 새로운 프로필 이미지 생성 및 저장
            if (newProfileImages != null && newProfileImages.length > 0) {
                for (MultipartFile file : newProfileImages) {
                    if (!file.isEmpty()) {
                        // 파일 저장
                        String savedFileName = fileController.saveFile(file);
                        log.info("New profile image saved: {}", savedFileName);

                        // 새로운 프로필 이미지 객체 생성 후 유저와 연결
                        ProfileImage newImage = new ProfileImage();
                        newImage.setImageUrl(savedFileName); // 저장된 파일 이름만 저장
                        newImage.setUser(user);
                        currentImages.add(newImage);

                        log.info("New ProfileImage added: {}", newImage);
                    }
                }
            }

            // User 정보 저장
            userRepository.save(user);
            log.info("User profile updated successfully for uid: {}", uid);
        } else {
            log.warn("User not found for uid: {}", uid);
        }
    }


}