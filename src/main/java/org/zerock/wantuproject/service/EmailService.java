package org.zerock.wantuproject.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.zerock.wantuproject.entity.VerificationCode;
import org.zerock.wantuproject.repository.VerificationCodeRepository;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerificationCodeRepository verificationCodeRepository;

    public void sendVerificationEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // HTML로 이메일을 전송하기 위해 true 설정

        mailSender.send(message); // 이메일 전송
    }

    // 이메일로 인증 코드를 저장하는 메서드
    public void saveVerificationCode(String email, String code) {
        VerificationCode verificationCode = new VerificationCode(email, code);
        verificationCodeRepository.save(verificationCode);
    }

    // 인증 코드 확인 메서드
    public boolean verifyCode(String email, String code) {
        Optional<VerificationCode> optionalCode = verificationCodeRepository.findByEmailAndCode(email, code);
        if (optionalCode.isPresent()) {
            verificationCodeRepository.delete(optionalCode.get()); // 인증 완료 후 코드 삭제
            return true;
        }
        return false;
    }
}
