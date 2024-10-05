package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zerock.wantuproject.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"interests"})
    Optional<User> findByUid(String Uid);
    // 아이디 중복 여부 확인 메서드
    boolean existsByUid(String uid);

    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndEmail(String name, String email);
//    // 동일한 관심사를 가진 사용자 목록을 조회
//    @Query("SELECT u FROM User u JOIN u.interests i WHERE i.interestName = :interestName AND u.gender <> :gender AND u.id <> :userId")
//    List<User> findAllByInterestNameAndGenderNotAndIdNot(String interestName, String gender, Long userId);

    @Query("SELECT u FROM User u " +
            "JOIN u.interests i " +
            "WHERE i.interestName IN :interestNames " +
            "AND u.id != :userId " +
            "AND u.gender != :gender " +
            "GROUP BY u " +
            "ORDER BY COUNT(i) DESC")
    List<User> findUsersByInterestsAndDifferentGender(List<String> interestNames, Long userId, String gender);

}