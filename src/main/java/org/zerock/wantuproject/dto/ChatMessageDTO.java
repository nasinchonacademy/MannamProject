package org.zerock.wantuproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zerock.wantuproject.entity.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long id;
    private String sender; // User 대신 String으로 수정 (보낸 사람의 사용자 이름)
    private String senderName; // sender의 name
    private String content;
    private Long roomId; // roomId로 ChatRoom 대신 사용
    private Long senderId; // sender의 ID
    private LocalDateTime timestamp;
}
