package org.zerock.wantuproject.dto;


import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AddUserRequest {
    private Long id;
    private String email;
    private String uid;
    private String password;
    private String nickname;
    private String name;
    private String gender;
    private String introduce;
    private LocalDate birthDate;
    private List<String> interests;
    private List<MultipartFile> profileImages;
    private String address;
}
