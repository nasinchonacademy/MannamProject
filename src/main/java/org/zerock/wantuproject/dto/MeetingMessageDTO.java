package org.zerock.wantuproject.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MeetingMessageDTO {
    private Long id;
    private String sender;  // 메시지를 보낸 사람의 이름
    private String content; // 메시지 내용
    private String roomId;  // 채팅방 식별자
    private LocalDateTime sentAt; // 메시지가 전송된 시간
    private String usersname; // 메시지를 보낸 사람의 이름 추가

    public MeetingMessageDTO(Long id, String sender, String content, String roomId, LocalDateTime sentAt, String usersname) {
        this.id = id;
        this.sender = sender;
        this.usersname = usersname;
        this.content = content;
        this.roomId = roomId;
        this.sentAt = sentAt;
    }
}
