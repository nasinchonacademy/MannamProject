package org.zerock.wantuproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.zerock.wantuproject.dto.ChatRoomDTO;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.service.ChatRoomService;
import org.zerock.wantuproject.service.UserService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;

    @PostMapping("/match")
    public ModelAndView matchAndRedirect(String interestName, Model model) {
        // 인증 객체로부터 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        String username = authentication.getName();

        // 1. 현재 사용자 정보 로그
        User currentUser = userService.findByUsername(username);
        System.out.println("로그인된 사용자: " + currentUser);

        Long userId = currentUser.getId();
        model.addAttribute("loggedInUserId", userId); // 로그인 사용자 ID 전달

        // 2. 매칭 시작 로그
        System.out.println("매칭 시작 - 사용자: " + username + ", 관심사: " + interestName);

        model.addAttribute("user", currentUser); // user 객체와 관심사 데이터를 템플릿에 전달

        ChatRoomDTO chatRoomDTO = chatRoomService.matchAndCreateRoom(userId);

        // 3. 채팅방 결과 로그
        System.out.println("채팅방 생성 결과: " + chatRoomDTO);

        ModelAndView modelAndView = new ModelAndView("chat/chatroom");
        modelAndView.addObject("roomId", chatRoomDTO.getRoomid());
        System.out.println("----------------------------------------------------------------"+chatRoomDTO.getOtherUser().getId());

        if (chatRoomDTO.getOtherUser() == null) {
            System.out.println("상대방을 찾을 수 없습니다.");
            modelAndView.addObject("message", "상대방을 찾을 수 없습니다.");
            modelAndView.addObject("otherUser", null);
            modelAndView.addObject("profileImageUrls", null);
            modelAndView.addObject("otherUserId", null);  // 상대방 사용자 ID 추가
        } else {
            modelAndView.addObject("otherUser", chatRoomDTO.getOtherUser());
            modelAndView.addObject("profileImageUrls", chatRoomDTO.getProfileImageUrls());
            modelAndView.addObject("otherUserId", chatRoomDTO.getOtherUser().getId());  // 상대방 사용자 ID 추가
        }

        return modelAndView;
    }
}
