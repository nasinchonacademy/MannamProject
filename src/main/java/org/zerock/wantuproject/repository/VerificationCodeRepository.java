package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.wantuproject.entity.VerificationCode;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    // 이메일과 인증 코드를 이용해 VerificationCode 조회
    Optional<VerificationCode> findByEmailAndCode(String email, String code);

    // 이메일을 기반으로 인증 코드 삭제
    void deleteByEmail(String email);
}
