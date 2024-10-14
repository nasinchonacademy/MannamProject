package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zerock.wantuproject.dto.MeetingMessageDTO;
import org.zerock.wantuproject.entity.MeetingMessage;
import org.zerock.wantuproject.entity.MeetingRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.MeetingMessageRepository;
import org.zerock.wantuproject.repository.MeetingRoomRepository;
import org.zerock.wantuproject.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingMessageService {

    private final MeetingMessageRepository messageRepository;
    private final MeetingRoomRepository roomRepository;
    private final UserRepository userRepository;


    // 메시지를 저장하는 로직
    public MeetingMessageDTO saveMessage(String roomId, String senderName, String content) {
        MeetingRoom room = roomRepository.findById(Long.parseLong(roomId))
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        User sender = userRepository.findByUid(senderName)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        MeetingMessage message = MeetingMessage.builder()
                .meetingRoom(room)
                .user(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        return new MeetingMessageDTO(message.getId(), sender.getUsername(), message.getContent(), roomId, message.getSentAt(),sender.getName());
    }

    // 특정 채팅방의 메시지 내역을 가져오는 메서드
    public List<MeetingMessageDTO> getMessagesByRoomId(Long roomId) {
        MeetingRoom meetingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
        List<MeetingMessage> messages =  messageRepository.findByMeetingRoom(meetingRoom);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MeetingMessageDTO convertToDto(MeetingMessage message) {
        return new MeetingMessageDTO(
                message.getId(),
                message.getUser().getUsername(),
                message.getContent(),
                message.getMeetingRoom().getId().toString(),
                message.getSentAt(),
                message.getUser().getName()
        );
    }
}

