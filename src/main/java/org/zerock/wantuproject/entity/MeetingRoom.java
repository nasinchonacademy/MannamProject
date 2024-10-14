package org.zerock.wantuproject.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"participants"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MeetingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;  // 모임방 제목
    private String imagePath;  // 이미지 경로
    private String address;   // 위치
    private Double latitude;  // 위도
    private Double longitude; // 경도
    private String description;

    // 관심사 목록을 배열로 저장하기 위한 필드
    @ElementCollection
    @CollectionTable(name = "meeting_interests", joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "interest")
    private List<String> interests;  // 관심사 목록

    @OneToMany(mappedBy = "meetingRoom", cascade = CascadeType.ALL)
    @JsonIgnore  // 순환 참조 방지
    private Set<MeetingRoomParticipant> participants = new HashSet<>();


    @ManyToOne
    private User host;

    private int likes = 0;

    // 참여자를 추가하는 메서드
    public void addParticipant(MeetingRoomParticipant participant) {
        if (this.participants == null) {
            this.participants = new HashSet<>();
        }
        this.participants.add(participant);
    }

    public void removeParticipant(User user) {
        this.participants.remove(user);
    }

    // 좋아요 수 증가
    public void increaseLikes() {
        this.likes++;
    }

    // 좋아요 수 감소
    public void decreaseLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public int getParticipantsCount() {
        return participants != null ? participants.size() : 0;
    }

    @Override
    public String toString() {
        return "MeetingRoom{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", address='" + address + '\'' +
                // 다른 필드들
                '}';
    }


}
