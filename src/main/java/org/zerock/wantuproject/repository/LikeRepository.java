package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.wantuproject.entity.MeetingRoomLike;
import org.zerock.wantuproject.entity.MeetingRoom;
import org.zerock.wantuproject.entity.User;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<MeetingRoomLike, Long> {

    // 특정 사용자가 특정 모임방을 좋아요했는지 확인하는 메서드
    Optional<MeetingRoomLike> findByUserAndMeetingRoom(User user, MeetingRoom meetingRoom);

    boolean existsByUserAndMeetingRoom(User user, MeetingRoom meetingRoom);
}

