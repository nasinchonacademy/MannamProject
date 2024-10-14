package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.wantuproject.entity.MeetingRoom;
import org.zerock.wantuproject.entity.MeetingRoomParticipant;
import org.zerock.wantuproject.entity.User;

import java.util.List;
import java.util.Optional;

public interface MeetingRoomParticipantRepository extends JpaRepository<MeetingRoomParticipant, Long> {

    Optional<MeetingRoomParticipant> findByMeetingRoomAndUser(MeetingRoom meetingRoom, User user);

    List<MeetingRoomParticipant> findByUserUid(String uid);
}
