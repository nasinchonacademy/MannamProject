package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.wantuproject.entity.LikedUser;
import org.zerock.wantuproject.entity.User;

public interface LikedUserRepository extends JpaRepository<LikedUser, Long> {

    // 특정 사용자가 다른 사용자를 좋아했는지 여부를 확인하는 쿼리
    boolean existsByLikerAndLikedUser(User liker, User likedUser);

    // 특정 유저가 받은 좋아요 수를 카운트하는 쿼리
    @Query("SELECT COUNT(l) FROM LikedUser l WHERE l.likedUser.id = :userId")
    int countLikesByUserId(@Param("userId") Long userId);
}
