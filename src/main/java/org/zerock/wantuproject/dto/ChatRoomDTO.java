package org.zerock.wantuproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {

    private Long roomid;
    private String roomname;
    private Long user1Id;
    private Long user2Id;

    // 상대방 정보 추가
    private AddUserRequest otherUser;  // 상대방의 프로필 정보

    // 상대방 프로필 이미지 추가
    private List<String> profileImageUrls;  // 상대방의 프로필 이미지 URL 리스트
}
