package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.wantuproject.dto.MeetingRoomDto;
import org.zerock.wantuproject.entity.MeetingRoom;
import org.zerock.wantuproject.entity.MeetingRoomParticipant;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.MeetingRoomParticipantRepository;
import org.zerock.wantuproject.repository.MeetingRoomRepository;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;
    private static final Logger logger = LoggerFactory.getLogger(MeetingRoomService.class);
    private final MeetingRoomParticipantRepository meetingRoomParticipantRepository;

    @Transactional(readOnly = true)
    public MeetingRoomDto getMeetingRoom(Long roomId) {
        // DTO를 바로 리포지토리에서 가져옴
        return meetingRoomRepository.findMeetingRoomDtoById(roomId);
    }

//    @Transactional
//    public void joinMeetingRoom(Long roomId, User user) {
//        logger.info("Joining meeting room: roomId={}, userId={}", roomId, user.getId());
//
//        MeetingRoom meetingRoom = meetingRoomRepository.findById(roomId)
//                .orElseThrow(() -> {
//                    logger.error("Room not found: roomId={}", roomId);
//                    return new RuntimeException("Room not found");
//                });
//
//        logger.info("Meeting room found: {}", meetingRoom.getTitle());
//
//        MeetingRoomParticipant participant = new MeetingRoomParticipant(null, user, meetingRoom, LocalDateTime.now());
//        meetingRoom.addParticipant(participant);
//        meetingRoomParticipantRepository.save(participant);
//
//        logger.info("Participant added: userId={}, roomId={}", user.getId(), roomId);
//    }

    @Transactional
    public void joinMeetingRoom(Long roomId, User user) {
        try {
            logger.info("Joining meeting room: roomId={}, userId={}", roomId, user.getId());

            // EntityGraph로 MeetingRoom을 조회해 참가자와 관련된 정보를 함께 로드
            MeetingRoom meetingRoom = meetingRoomRepository.findByIdWithParticipants(roomId)
                    .orElseThrow(() -> {
                        logger.error("Room not found: roomId={}", roomId);
                        return new RuntimeException("Room not found");
                    });

            MeetingRoomParticipant participant = new MeetingRoomParticipant(null, user, meetingRoom, LocalDateTime.now());

            // MeetingRoom에 참가자를 추가하고 저장
            meetingRoom.addParticipant(participant);
            meetingRoomParticipantRepository.save(participant);

            logger.info("Participant added: userId={}, roomId={}", user.getId(), roomId);
        } catch (Exception e) {
            logger.error("Error while joining meeting room: roomId={}, error={}", roomId, e.getMessage());
            throw e; // 필요 시 적절한 예외로 변환 후 다시 던짐
        }
    }



    // 채팅방 ID를 통해 MeetingRoomDto 반환
    @Transactional(readOnly = true)
    public MeetingRoomDto getMeetingRoomById(Long roomId) {
        // MeetingRoom 엔티티를 조회
        MeetingRoom meetingRoom = meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Meeting Room not found"));

        // MeetingRoom 엔티티를 MeetingRoomDto로 변환
        return convertToDto(meetingRoom);
    }

    // MeetingRoom 엔티티를 MeetingRoomDto로 변환하는 메서드
    private MeetingRoomDto convertToDto(MeetingRoom meetingRoom) {
        return new MeetingRoomDto(
                meetingRoom.getId(),
                meetingRoom.getTitle(),
                meetingRoom.getImagePath(),
                meetingRoom.getInterests(),
                meetingRoom.getParticipants().size(),
                meetingRoom.getLikes(),
                meetingRoom.getAddress(),
                meetingRoom.getDescription()
        );
    }
}


