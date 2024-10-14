package org.zerock.wantuproject.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.wantuproject.dto.MeetingMessageDTO;
import org.zerock.wantuproject.service.MeetingMessageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MeetingMessageRestController {

    private final MeetingMessageService meetingMessageService;

    // 특정 채팅방의 메시지 내역을 반환하는 API
    @GetMapping("/api/meetingRoom/{roomId}/messages")
    public List<MeetingMessageDTO> getChatMessages(@PathVariable Long roomId) {
        // 채팅방 메시지 내역 가져오기
        return meetingMessageService.getMessagesByRoomId(roomId);
    }

}
