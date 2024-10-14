package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.wantuproject.entity.MeetingMessage;
import org.zerock.wantuproject.entity.MeetingRoom;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingMessageRepository extends JpaRepository<MeetingMessage, Long> {

    // 해당 채팅방의 모든 메시지 로드
    List<MeetingMessage> findByMeetingRoom(MeetingRoom meetingRoom);

    // 사용자가 참여한 이후의 메시지만 로드
    @Query("SELECT m FROM MeetingMessage m WHERE m.meetingRoom.id = :roomId AND m.sentAt > :joinedAt")
    List<MeetingMessage> findMessagesAfterJoin(@Param("roomId") Long roomId, @Param("joinedAt") LocalDateTime joinedAt);

    // 참여자가 입장한 이후의 메시지를 조회하는 메서드
    List<MeetingMessage> findByMeetingRoomAndSentAtAfter(MeetingRoom meetingRoom, LocalDateTime joinedAt);


}
