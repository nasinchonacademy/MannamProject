package org.zerock.wantuproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomDto {
    private Long id;
    private String title;
    private String imagePath;
    private List<String> interests;
    private int participantsCount;
    private int likes;
    private String address;
    private String description;
}
