package org.zerock.wantuproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.wantuproject.entity.MeetingRoom;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.service.MypageService;
import java.io.IOException;
import java.util.List;

@Controller
public class MypageController {

    private final MypageService mypageService;

    public MypageController(MypageService mypageService) {
        this.mypageService = mypageService;
    }

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String uid = userDetails.getUsername();
        User user = mypageService.getUserProfile(uid);
        List<MeetingRoom> meetingRooms = mypageService.getMeetingRooms(uid);

        if (meetingRooms.isEmpty()) {
            System.out.println("No meeting rooms found for user in controller: " + uid);
        } else {
            System.out.println("Meeting rooms retrieved in controller for user: " + uid + " -> " + meetingRooms);
        }

        model.addAttribute("user", user);
        model.addAttribute("likesGiven", mypageService.getLikesGiven(uid));
        model.addAttribute("likesReceived", mypageService.getLikesReceived(uid));
        model.addAttribute("chatRooms", mypageService.getChatRooms(uid));
        model.addAttribute("meetingRooms", meetingRooms);
        return "member/mypage";
    }


    @GetMapping("/mypage/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String uid = userDetails.getUsername();
        User user = mypageService.getUserByUid(uid);

        model.addAttribute("user", user);
        return "member/mypageEdit";
    }

    @PostMapping("/mypage/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("introduce") String introduce,
                                @RequestParam("address") String address,
                                @RequestParam("interests") List<String> interests,
                                @RequestParam(value = "existingProfileImages", required = false) List<String> existingProfileImages,
                                @RequestParam(value = "profileImages", required = false) MultipartFile[] profileImages) {
        String uid = userDetails.getUsername();
        try {
            mypageService.updateUserProfile(uid, introduce, address, interests, existingProfileImages, profileImages);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/mypage/edit?error";
        }
        return "redirect:/mypage";
    }
}


