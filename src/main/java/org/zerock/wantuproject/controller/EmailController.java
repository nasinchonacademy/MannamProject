package org.zerock.wantuproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.wantuproject.dto.AddUserRequest;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.UserRepository;
import org.zerock.wantuproject.service.EmailService;
import org.zerock.wantuproject.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
public class EmailController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 이메일로 아이디/비밀번호 찾기 요청 API
    @GetMapping("/send-recovery-code")
    public ResponseEntity<Map<String, Boolean>> sendRecoveryCode(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();

        // 인증 코드 생성
        String verificationCode = UUID.randomUUID().toString().substring(0, 6); // 6자리 인증 코드 생성
        emailService.saveVerificationCode(email, verificationCode);

        // 인증 코드 이메일로 전송
        String subject = "아이디/비밀번호 찾기 인증 코드";
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
    @GetMapping("/verify-recovery-code")
    public ResponseEntity<Map<String, Boolean>> verifyRecoveryCode(@RequestParam String email, @RequestParam String code) {
        Map<String, Boolean> response = new HashMap<>();

        boolean isVerified = emailService.verifyCode(email, code);
        response.put("verified", isVerified);

        return ResponseEntity.ok(response);
    }

    // 비밀번호 재설정 또는 아이디 찾기 완료 API
    @PostMapping("/recover-id-password")
    public ResponseEntity<Map<String, Object>> recoverIdOrPassword(@RequestBody AddUserRequest userRequest) {
        Map<String, Object> response = new HashMap<>();

        // 이메일로 아이디 찾기 로직
        Optional<User> optionalUser = userRepository.findByEmail(userRequest.getEmail());
        if (optionalUser.isPresent()) {
            if (userRequest.getPassword() != null) {
                // 비밀번호 재설정 로직
                User user = optionalUser.get();
                user.setPassword(bCryptPasswordEncoder.encode(userRequest.getPassword()));
                userRepository.save(user);
                response.put("message", "비밀번호가 성공적으로 재설정되었습니다.");
            } else {
                // 아이디 반환
                response.put("username", optionalUser.get().getUsername());
            }
        } else {
            response.put("message", "해당 이메일로 등록된 사용자가 없습니다.");
        }

        return ResponseEntity.ok(response);
    }


}
