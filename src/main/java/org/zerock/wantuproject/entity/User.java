package org.zerock.wantuproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", nullable = false)
    private String uid;

    @Column(name = "user_name", nullable = false) // 사용자 이름, null 불가
    private String name;

    @Column(nullable = false) // 이메일은 null 불가, 고유 값으로 설정
    private String email;

    @Column(nullable = false) // 비밀번호는 null 불가
    private String password;

    @Column
    private String nickname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "registration_date")
    private LocalDate regidate;

    @Column
    private String gender;

    @Column
    private String introduce;

    @Column
    private String address;

    @Column(nullable = false)
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL , orphanRemoval = true)
    @JsonIgnore
    private List<ProfileImage> profileImages;

    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL , orphanRemoval = true)
    private List<Interest> interests;

    // 내가 좋아요를 누른 사용자 목록 (양방향 관계 설정)
    @OneToMany(mappedBy = "liker", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LikedUser> likedUsers = new ArrayList<>();

    // 나를 좋아요한 사용자 목록
    @OneToMany(mappedBy = "likedUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<LikedUser> likedByUsers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MeetingRoomParticipant> meetingRooms;



    @Builder
    public User(Long id,String name, String email,String password, String nickname,LocalDate birthDate, String gender, String introduce, String roleUser,String uid, String address) {
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.birthDate =  birthDate;
        this.gender = gender;
        this.introduce = introduce;
        this.role = roleUser;
        this.address = address;
    }

    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = "USER"; // 기본값 설정
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));  // role은 권한 정보가 저장된 필드
    }


    @Override
    public String getUsername() {
        return this.uid;  // 인증 시 사용할 고유 ID
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getPassword() {
        return this.password;  // 암호화된 비밀번호
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", birthDate=" + birthDate +
                ", regidate=" + regidate +
                ", gender='" + gender + '\'' +
                ", introduce='" + introduce + '\'' +
                ", address='" + address + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public String getProfileImageUrl() {
        if (profileImages != null && !profileImages.isEmpty()) {
            return profileImages.get(0).getImageUrl();  // 첫 번째 이미지를 기본 이미지로 사용
        }
        return "/images/default-profile.png";  // 기본 이미지 경로
    }

    // 좋아요 받은 수 반환
    public int getLikeCount() {
        return this.likedByUsers.size();  // 나를 좋아요한 사용자 목록 크기를 반환
    }


}
