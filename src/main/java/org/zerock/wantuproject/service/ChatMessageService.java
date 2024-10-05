package org.zerock.wantuproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zerock.wantuproject.dto.ChatMessageDTO;
import org.zerock.wantuproject.entity.ChatMessage;
import org.zerock.wantuproject.entity.ChatRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.ChatMessageRepository;
import org.zerock.wantuproject.repository.ChatRoomRepository;
import org.zerock.wantuproject.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository; // ChatRoom 엔티티 조회를 위해 사용

    @Autowired
    private UserRepository userRepository;

    public ChatMessageDTO saveMessage(ChatMessageDTO messageDTO) {
        System.out.println("roomId로 메시지 저장 중: " + messageDTO.getRoomId());

        // roomId로 ChatRoom 객체를 가져옴
        ChatRoom chatRoom = chatRoomRepository.findById(messageDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("해당 roomId로 채팅방을 찾을 수 없습니다: " + messageDTO.getRoomId()));

        // sender의 이름을 기반으로 User 객체를 조회
        User sender = userRepository.findByUid(messageDTO.getSender())
                .orElseThrow(() -> new RuntimeException("해당 사용자 이름을 가진 사용자를 찾을 수 없습니다: " + messageDTO.getSender()));

        // ChatMessageDTO와 ChatRoom을 이용해 ChatMessage 객체 생성
        ChatMessage message = ChatMessage.builder()
                .sender(sender)  // 조회한 User 객체로 설정
                .content(messageDTO.getContent())
                .chatRoom(chatRoom)  // ChatRoom 엔티티 설정
                .timestamp(messageDTO.getTimestamp() != null ? messageDTO.getTimestamp() : LocalDateTime.now())
                .build();

        // 메시지를 저장
        ChatMessage savedMessage = chatMessageRepository.save(message);
        return entityToDto(savedMessage);
    }


    public List<ChatMessageDTO> getMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByChatRoomRoomid(roomId).stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public void deleteMessagesByRoomId(Long roomId) {
        chatMessageRepository.deleteByChatRoomRoomid(roomId);
    }

    // DTO -> 엔티티 변환
    private ChatMessage dtoToEntity(ChatMessageDTO dto, ChatRoom chatRoom, User sender) {
        return ChatMessage.builder()
                .id(dto.getId())
                .sender(sender)  // User 객체를 직접 설정
                .content(dto.getContent())
                .chatRoom(chatRoom)
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .build();
    }

    public ChatMessageDTO entityToDto(ChatMessage entity) {
        return ChatMessageDTO.builder()
                .id(entity.getId())
                .senderName(entity.getSender().getName()) // sender의 이름
                .senderId(entity.getSender().getId()) // sender의 ID 추가
                .sender(entity.getSender().getUsername())  // User 객체에서 username을 추출
                .content(entity.getContent())
                .roomId(entity.getChatRoom().getRoomid())  // ChatRoom의 ID를 DTO로 변환
                .timestamp(entity.getTimestamp())
                .build();
    }



}

