package org.zerock.wantuproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 좋아요를 눌렀는지 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liker_id")
    @JsonIgnore
    private User liker;

    // 누구를 좋아했는지 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liked_id")
    @JsonIgnore
    private User likedUser;

    private LocalDateTime likedAt; // 좋아요를 누른 시간
}
