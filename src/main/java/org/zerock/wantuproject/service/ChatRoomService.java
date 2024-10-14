package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zerock.wantuproject.dto.AddUserRequest;
import org.zerock.wantuproject.dto.ChatRoomDTO;
import org.zerock.wantuproject.entity.ChatMessage;
import org.zerock.wantuproject.entity.ChatRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.ChatMessageRepository;
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
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoomDTO matchAndCreateRoom(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));

        // 현재 사용자의 관심사 목록
        List<String> userInterests = currentUser.getInterests().stream()
                .map(interest -> interest.getInterestName())
                .collect(Collectors.toList());

        // 관심사 목록 중 하나라도 일치하는 사용자 목록 조회 (이미 매칭된 사용자 제외)
        List<User> potentialMatches = userRepository.findUsersByInterestsAndDifferentGender(
                userInterests, currentUser.getId(), currentUser.getGender());

        // 매칭될 사용자가 없을 경우 예외 처리
        if (potentialMatches.isEmpty()) {
            throw new RuntimeException("No matching users found");
        }

        // 랜덤하게 한 명의 사용자를 선택
        User matchedUser = potentialMatches.get(new Random().nextInt(potentialMatches.size()));

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

    public ChatRoomDTO matchAndCreateRoom(String userUid) {
        // uid로 현재 사용자 조회
        User currentUser = userRepository.findByUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found with uid: " + userUid));

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

    public ChatRoomDTO matchAndCreateNewRoom(String userUid) {
        // uid로 새로운 상대를 찾는 로직 (기존 로직과 유사)
        return matchAndCreateRoom(userUid);
    }

    // 채팅방을 저장하는 메서드
    public void saveChatRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다: " + roomId));

        // 채팅방을 저장하는 로직 (채팅 기록이나 기타 정보를 업데이트할 수 있음)
        chatRoomRepository.save(chatRoom);
    }

    // 프로필 ID로 채팅방 정보 가져오기
    public ChatRoomDTO getChatRoomByProfileId(Long profileId) {
        User selectedUser = userRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + profileId));

        ChatRoom chatRoom = chatRoomRepository.findByUser1OrUser2(selectedUser, selectedUser)
                .orElseThrow(() -> new RuntimeException("No chat room found for user: " + profileId));

        return ChatRoomDTO.builder()
                .roomid(chatRoom.getRoomid())  // 채팅방 ID 설정
                .roomname(chatRoom.getRoomname())
                .user1Id(chatRoom.getUser1().getId())  // 사용자 1 ID
                .user2Id(chatRoom.getUser2().getId())  // 사용자 2 ID
                .otherUser(userService.convertToDto(selectedUser))  // 상대방 정보
                .profileImageUrls(userService.getProfileImageUrls(selectedUser))  // 프로필 이미지들
                .build();

}



    public ChatRoomDTO createChatRoomWithSelectedUser(Long currentUserId, Long selectedUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + currentUserId));

        User selectedUser = userRepository.findById(selectedUserId)
                .orElseThrow(() -> new RuntimeException("Selected user not found with userId: " + selectedUserId));

        // 기존에 채팅방이 있는지 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUsers(currentUser, selectedUser);

        // 기존 채팅방이 있을 경우 해당 채팅방 반환
        if (existingRoom.isPresent()) {
            return entityToDto(existingRoom.get(), selectedUser);
        }

        // 기존 채팅방이 없을 경우 새로 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser1(currentUser);
        chatRoom.setUser2(selectedUser);
        chatRoom.setRoomname("Room-" + currentUser.getName() + "-and-" + selectedUser.getName());

        chatRoomRepository.save(chatRoom);  // 채팅방 저장

        // 채팅방 DTO 반환
        return entityToDto(chatRoom, selectedUser);
    }



    public Optional<ChatRoom> findExistingChatRoom(Long currentUserId, Long selectedUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + currentUserId));

        User selectedUser = userRepository.findById(selectedUserId)
                .orElseThrow(() -> new RuntimeException("Selected user not found with userId: " + selectedUserId));

        // 기존 채팅방 조회 (양방향으로 확인)
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUser1AndUser2(currentUser, selectedUser);
        if (existingRoom.isEmpty()) {
            existingRoom = chatRoomRepository.findByUser2AndUser1(currentUser, selectedUser);
        }
        return existingRoom; // 기존 채팅방이 있으면 반환, 없으면 빈 값 반환
    }

    public List<User> findNewUsersWithoutChatRoom(Long currentUserId, String currentGender, int limit) {
        Pageable pageable = PageRequest.of(0, limit);  // 가져올 유저 수 제한
        return  chatRoomRepository.findUsersWithoutChatRoom(currentUserId, currentGender, pageable);
    }

    public List<User> getRandomUsersByGenderExcludingExistingRooms(Long currentUserId, String currentGender) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + currentUserId));

        // 성별이 다른 유저들을 모두 가져옴
        List<User> allUsersOfOppositeGender = userRepository.findUsersByGenderNotAndIdNot(currentGender, currentUserId);

        // 기존 채팅방이 없는 유저만 필터링
        List<User> filteredUsers = allUsersOfOppositeGender.stream()
                .filter(user -> !this.isChatRoomExists(currentUserId, user.getId())) // 기존 채팅방이 없는 유저만
                .collect(Collectors.toList());

        // 필터링된 유저 중에서 3명을 랜덤으로 선택 (필터링된 유저가 3명 이상인 경우)
        Random random = new Random();
        List<User> randomUsers;
        if (filteredUsers.size() > 3) {
            randomUsers = random.ints(0, filteredUsers.size())
                    .distinct()
                    .limit(3)
                    .mapToObj(filteredUsers::get)
                    .collect(Collectors.toList());
        } else {
            // 필터링된 유저가 3명 미만인 경우는 그대로 반환
            randomUsers = filteredUsers;
        }

        return randomUsers;
    }

    public boolean isChatRoomExists(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + user1Id));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + user2Id));

        // 두 사용자 간의 채팅방 존재 여부 확인
        return chatRoomRepository.existsByUser1AndUser2(user1, user2)
                || chatRoomRepository.existsByUser2AndUser1(user2, user1);
    }


    public void deleteChatRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found with id: " + roomId));

        chatRoomRepository.delete(chatRoom); // 채팅방 삭제
    }

    public Optional<ChatRoom> getExistingChatRoomWithUserIds(Long currentUserId, List<Long> selectedUserIds) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + currentUserId));

        // 선택된 사용자 ID 리스트를 User 리스트로 변환
        List<User> selectedUsers = selectedUserIds.stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId)))
                .collect(Collectors.toList());

        // 각 선택된 사용자에 대해 채팅방 존재 여부 확인
        for (User selectedUser : selectedUsers) {
            Optional<ChatRoom> existingRoom = chatRoomRepository.findByUser1AndUser2(currentUser, selectedUser);
            if (existingRoom.isEmpty()) {
                existingRoom = chatRoomRepository.findByUser1AndUser2(selectedUser, currentUser); // 순서를 바꿔서도 확인
            }
            if (existingRoom.isPresent()) {
                return existingRoom; // 채팅방이 있으면 바로 반환
            }
        }

        return Optional.empty(); // 채팅방이 없으면 빈 Optional 반환
    }

    public List<User> matchAndCreateNewRooms(String userUid, List<Long> selectedUserIds) {
        // 현재 로그인한 사용자 조회
        User currentUser = userRepository.findByUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found with uid: " + userUid));

        // 현재 사용자와 성별이 다른 유저 중에서 이미 선택된 유저는 제외한 리스트를 가져옴
        List<User> allUsersOfOppositeGender = userRepository.findUsersByGenderNotAndIdNot(currentUser.getGender(), currentUser.getId());

        // 이미 선택된 유저나 기존에 채팅방이 있는 유저를 제외한 새로운 유저들만 필터링
        List<User> newUsers = allUsersOfOppositeGender.stream()
                .filter(user -> !selectedUserIds.contains(user.getId()))  // 이미 선택된 유저 제외
                .filter(user -> !(chatRoomRepository.existsByUser1AndUser2(currentUser, user) ||
                        chatRoomRepository.existsByUser2AndUser1(currentUser, user)))  // 기존 채팅방이 없는 유저만 선택
                .limit(3)  // 최대 3명 선택
                .collect(Collectors.toList());

        return newUsers;  // 새로운 유저 리스트 반환
    }



    // 기존의 채팅 메시지 가져오는 메서드 그대로 사용
    public List<ChatMessage> getMessagesByRoom(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderByTimestampAsc(chatRoom);
    }






}
