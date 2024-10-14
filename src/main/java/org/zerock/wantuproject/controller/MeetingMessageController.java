package org.zerock.wantuproject.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.zerock.wantuproject.dto.MeetingMessageDTO;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.service.MeetingMessageService;
import org.zerock.wantuproject.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MeetingMessageController {

    private final MeetingMessageService meetingMessageService;
    private final UserService userService;

    @MessageMapping("/meeting/{roomId}/sendMessage")
    @SendTo("/topic/meeting/{roomId}")
    public MeetingMessageDTO sendMessage(@DestinationVariable String roomId, @Payload MeetingMessageDTO messageDTO) {
        log.info("Received message: " + messageDTO);

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

        // 메시지 저장 로직 호출
        MeetingMessageDTO savedMessage = meetingMessageService.saveMessage(roomId, sender.getUsername(), messageDTO.getContent());

        log.info("메시지 저장 완료: " + savedMessage);

        // 저장된 메시지를 반환하여 실시간으로 클라이언트에게 전송
        return savedMessage;
    }


}
