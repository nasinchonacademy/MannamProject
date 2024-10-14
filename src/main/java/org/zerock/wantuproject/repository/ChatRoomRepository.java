package org.zerock.wantuproject.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.wantuproject.entity.ChatRoom;
import org.zerock.wantuproject.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // Custom query to check if a room already exists between two users
    // user1과 user2로 채팅방을 찾는 메서드
    Optional<ChatRoom> findByUser1AndUser2(User user1, User user2);

    // user2와 user1으로 채팅방을 찾는 메서드 (순서 반대)
    Optional<ChatRoom> findByUser2AndUser1(User user2, User user1);

    // 특정 사용자와 이미 매칭된 상대방의 ID 목록을 조회
    @Query("SELECT CASE WHEN c.user1.id = :userId THEN c.user2.id ELSE c.user1.id END " +
            "FROM ChatRoom c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    List<Long> findMatchedUserIdsByUser(@Param("userId") Long userId);

    // User1 또는 User2가 해당 사용자일 경우 채팅방을 찾는 메서드
    Optional<ChatRoom> findByUser1OrUser2(User user1, User user2);

    @Query("SELECT r FROM ChatRoom r WHERE (r.user1 = :user1 AND r.user2 = :user2) OR (r.user1 = :user2 AND r.user2 = :user1)")
    Optional<ChatRoom> findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    // 이미 채팅방이 있는 유저는 제외하고, 새로운 유저를 조회하는 메서드
    @Query("SELECT u FROM User u WHERE u.gender <> :currentGender AND u.id NOT IN " +
            "(SELECT cr.user2.id FROM ChatRoom cr WHERE cr.user1.id = :currentUserId) " +
            "AND u.id NOT IN " +
            "(SELECT cr.user1.id FROM ChatRoom cr WHERE cr.user2.id = :currentUserId)")
    List<User> findUsersWithoutChatRoom(@Param("currentUserId") Long currentUserId,
                                        @Param("currentGender") String currentGender,
                                        Pageable pageable);


    // 두 사용자 사이에 존재하는 채팅방이 있는지 확인하는 메서드
    boolean existsByUser1AndUser2(User user1, User user2);

    // 순서를 바꿔서도 확인하는 메서드
    boolean existsByUser2AndUser1(User user2, User user1);

    // 사용자1로 참여한 채팅방을 찾는 메서드
    List<ChatRoom> findByUser1(User user1);

    // 사용자2로 참여한 채팅방을 찾는 메서드
    List<ChatRoom> findByUser2(User user2);


}
