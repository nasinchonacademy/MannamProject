package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zerock.wantuproject.dto.AddUserRequest;
import org.zerock.wantuproject.dto.ChatRoomDTO;
import org.zerock.wantuproject.entity.ChatRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.ChatRoomRepository;
import org.zerock.wantuproject.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    private final UserService userService;

    public ChatRoomDTO matchAndCreateRoom(Long userId) {
        // userId로 현재 사용자 조회
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));

        // 현재 사용자의 관심사 목록을 가져옴
        List<String> userInterests = currentUser.getInterests().stream()
                .map(interest -> interest.getInterestName())
                .collect(Collectors.toList());

        // 관심사 목록 중 하나라도 일치하는 사용자 목록 조회 (성별이 다른 사용자)
        List<User> potentialMatches = userRepository.findUsersByInterestsAndDifferentGender(
                userInterests, currentUser.getId(), currentUser.getGender());

        // 매칭될 사용자가 없을 경우 예외 처리
        if (potentialMatches.isEmpty()) {
            throw new RuntimeException("No matching users found");
        }

        // 랜덤하게 한 명의 사용자를 선택
        User matchedUser = potentialMatches.get(random.nextInt(potentialMatches.size()));

        // 기존 채팅방 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUser1AndUser2(currentUser, matchedUser);
        if (existingRoom.isEmpty()) {
            existingRoom = chatRoomRepository.findByUser2AndUser1(currentUser, matchedUser);
        }

        // 기존 채팅방이 있으면 해당 방을 반환, 없으면 새로 생성
        if (existingRoom.isPresent()) {
            return entityToDto(existingRoom.get(), matchedUser);
        }

        // 새로운 채팅방 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser1(currentUser);
        chatRoom.setUser2(matchedUser);
        chatRoom.setRoomname("Room-" + currentUser.getName() + "-and-" + matchedUser.getName());

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return entityToDto(savedRoom, matchedUser);
    }

    private ChatRoomDTO entityToDto(ChatRoom chatRoom, User otherUser) {
        AddUserRequest otherUserDto = userService.convertToDto(otherUser); // User -> AddUserRequest로 변환
        return ChatRoomDTO.builder()
                .roomid(chatRoom.getRoomid())
                .roomname(chatRoom.getRoomname())
                .user1Id(chatRoom.getUser1().getId())  // 현재 사용자 ID
                .user2Id(chatRoom.getUser2().getId())  // 상대방 사용자 ID
                .otherUser(otherUserDto)               // 상대방 정보 추가
                .profileImageUrls(getProfileImageUrls(otherUser))  // 상대방 프로필 이미지 추가
                .build();
    }

    // 상대방의 프로필 이미지 URL을 리스트로 변환
    private List<String> getProfileImageUrls(User user) {
        return user.getProfileImages().stream()
                .map(profileImage -> profileImage.getImageUrl())
                .collect(Collectors.toList());
    }

    // roomId로 ChatRoom을 찾는 메소드 추가
    public ChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found with id: " + roomId));
    }
}
