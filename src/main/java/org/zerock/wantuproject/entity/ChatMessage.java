package org.zerock.wantuproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties({"email", "uid", "profileImages", "interests"}) // 필요한 필드만 포함
    private User sender;  // 보낸 사람

    private String content;

    // roomId를 ChatRoom과 ManyToOne 관계로 설정
    @ManyToOne
    @JoinColumn(name = "room_id")
    @ToString.Exclude // 순환 참조 방지
    private ChatRoom chatRoom;

    private LocalDateTime timestamp; // 메시지 보낸 시간

    @Transient
    @JsonProperty("senderName")
    public String getSenderName() {
        return sender != null ? sender.getName() : "알 수 없는 사용자";
    }

}