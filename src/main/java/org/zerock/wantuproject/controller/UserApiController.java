package org.zerock.wantuproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.wantuproject.dto.AddUserRequest;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.UserRepository;
import org.zerock.wantuproject.service.EmailService;
import org.zerock.wantuproject.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final UserService userService;
    private final EmailService emailService;
    private final FileController fileController;
    private final UserRepository userRepository;

    @PostMapping("/user")
    public String signup(AddUserRequest request, @RequestParam("profileImages[]") MultipartFile[] profileImagesFiles) {
        // 빈 파일을 제외하고 처리
        List<MultipartFile> validFiles = Arrays.stream(profileImagesFiles)
                .filter(file -> !file.isEmpty())
                .collect(Collectors.toList());

        // 파일 저장 로직
        List<String> savedFileNames = fileController.handleMultipleFileUpload(validFiles.toArray(new MultipartFile[0])).getBody();

        // 저장된 파일 이름 리스트를 UserService로 전달
        userService.save(request, savedFileNames);
        return "redirect:/login";
    }

    // 아이디 중복 체크 API
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String uid) {
        boolean exists = userService.isUsernameTaken(uid);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }



    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }

    // 이메일 인증 코드 전송 API
    @GetMapping("/send-verification-code")
    public ResponseEntity<Map<String, Boolean>> sendVerificationCode(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();

        // 인증 코드 생성
        String verificationCode = UUID.randomUUID().toString().substring(0, 6); // 6자리 인증 코드
        emailService.saveVerificationCode(email, verificationCode);

        // 인증 코드 이메일로 전송
        String subject = "회원가입 인증 코드";
        String content = "인증 코드는 다음과 같습니다: " + verificationCode;
        try {
            emailService.sendVerificationEmail(email, subject, content);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
        }

        return ResponseEntity.ok(response);
    }

    // 이메일 인증 코드 확인 API
    @GetMapping("/verify-code")
    public ResponseEntity<Map<String, Boolean>> verifyCode(@RequestParam String email, @RequestParam String code) {
        Map<String, Boolean> response = new HashMap<>();

        boolean isVerified = emailService.verifyCode(email, code);
        response.put("verified", isVerified);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/find-username")
    public ResponseEntity<Map<String, Object>> findUsername(@RequestBody Map<String, String> requestData) {
        Map<String, Object> response = new HashMap<>();

        String email = requestData.get("email");
        String name = requestData.get("name");

        // 이름과 이메일로 유저 찾기 로직
        Optional<User> optionalUser = userRepository.findByNameAndEmail(name, email);
        if (optionalUser.isPresent()) {
            // 아이디 반환
            response.put("uid", optionalUser.get().getUid());
        } else {
            response.put("message", "해당 정보로 등록된 사용자가 없습니다.");
        }

        return ResponseEntity.ok(response);
    }
}
