package org.zerock.wantuproject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
            "AND u.id NOT IN (SELECT cr.user2.id FROM ChatRoom cr WHERE cr.user1.id = :userId " +
            "UNION SELECT cr.user1.id FROM ChatRoom cr WHERE cr.user2.id = :userId) " + // 이미 매칭된 사용자 제외
            "GROUP BY u " +
            "ORDER BY COUNT(i) DESC")
    List<User> findUsersByInterestsAndDifferentGender(List<String> interestNames, Long userId, String gender);

    // 성별이 다른 유저를 랜덤으로 가져오는 쿼리 (페이징 지원)
    @Query("SELECT u FROM User u WHERE u.gender != :gender AND u.id != :currentUserId ORDER BY FUNCTION('RAND')")
    List<User> findRandomUsersByGender(@Param("currentUserId") Long currentUserId, @Param("gender") String gender, Pageable pageable);


    @Query("SELECT u FROM User u WHERE u.gender <> :gender AND u.id <> :userId AND u.id NOT IN :selectedUserIds ORDER BY function('RAND')")
    List<User> findTop3ByGenderNotAndIdNotAndIdNotInOrderByRandom(
            @Param("gender") String gender,
            @Param("userId") Long userId,
            @Param("selectedUserIds") List<Long> selectedUserIds);



    @Query("SELECT u FROM User u WHERE u.gender != :gender AND u.id != :userId")
    List<User> findUsersByGenderNotAndIdNot(@Param("gender") String gender, @Param("userId") Long userId);

}

