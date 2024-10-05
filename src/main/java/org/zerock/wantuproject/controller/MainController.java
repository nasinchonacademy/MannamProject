package org.zerock.wantuproject.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.service.UserService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/main")
    public String mainPage(Model model) {
        // 인증 객체로부터 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uid = authentication.getName(); // 로그인된 사용자의 uid

        logger.info("로그인된 사용자 UID: " + uid);

        // uid로 사용자 조회
        User currentUser = userService.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + uid));

        logger.info("DB에서 uid로 사용자 조회: " + currentUser);

        // 로그인된 사용자 정보를 모델에 추가
        model.addAttribute("user", currentUser);

        return "main";  // main.html 템플릿 반환
    }
}
