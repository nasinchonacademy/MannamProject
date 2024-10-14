package org.zerock.wantuproject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"meetingRoom"})
public class MeetingRoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    @JsonBackReference  // 순환 참조 방지
    private MeetingRoom meetingRoom;

    private LocalDateTime joinedAt; // 참여 시간

    @Override
    public String toString() {
        return "MeetingRoomParticipant{" +
                "id=" + id +
                ", joinedAt=" + joinedAt +
                // 다른 필드들
                '}';
    }
}