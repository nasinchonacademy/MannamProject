package org.zerock.wantuproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zerock.wantuproject.entity.LikedUser;
import org.zerock.wantuproject.entity.MeetingRoomParticipant;
import org.zerock.wantuproject.entity.User;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDto {
    private User user;
    private List<LikedUser> likesGiven;
    private List<LikedUser> likesReceived;
    private Set<MeetingRoomParticipant> meetingRooms;
}