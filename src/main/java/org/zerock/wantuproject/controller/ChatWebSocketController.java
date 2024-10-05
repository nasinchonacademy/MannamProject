package org.zerock.wantuproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.zerock.wantuproject.dto.ChatMessageDTO;
import org.zerock.wantuproject.entity.ChatMessage;
import org.zerock.wantuproject.entity.ChatRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.service.ChatMessageService;
import org.zerock.wantuproject.service.ChatRoomService;
import org.zerock.wantuproject.service.UserService;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final ChatRoomService chatRoomService;

    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageDTO sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageDTO messageDTO) {
        System.out.println("Received message: " + messageDTO);

        // 현재 인증된 사용자 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated.");
        }

        String currentUsername = authentication.getName(); // 인증된 사용자 이름 가져오기

        // 현재 인증된 사용자로부터 User 객체를 조회
        User sender = userService.findByUsername(currentUsername);
        if (sender == null) {
            throw new RuntimeException("User not found with username: " + currentUsername);
        }

        // 채팅방 ID로 ChatRoom 객체를 조회
        ChatRoom chatRoom = chatRoomService.findById(messageDTO.getRoomId());
        if (chatRoom == null) {
            throw new RuntimeException("ChatRoom not found with id: " + messageDTO.getRoomId());
        }

        // ChatMessage 객체 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(messageDTO.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        // 메시지 저장 후 DTO로 변환

        ChatMessageDTO chatMessageDTO = chatMessageService.entityToDto(chatMessage);

        // roomId 및 기타 로직 처리
        messageDTO.setRoomId(roomId);

        chatMessageService.saveMessage(chatMessageDTO);

        return chatMessageDTO; // 변환된 메시지를 반환
    }
}
