package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.wantuproject.entity.ChatRoom;
import org.zerock.wantuproject.entity.User;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // Custom query to check if a room already exists between two users
    // user1과 user2로 채팅방을 찾는 메서드
    Optional<ChatRoom> findByUser1AndUser2(User user1, User user2);

    // user2와 user1으로 채팅방을 찾는 메서드 (순서 반대)
    Optional<ChatRoom> findByUser2AndUser1(User user2, User user1);

    Optional<ChatRoom> findByRoomid(Long roomId);
}
