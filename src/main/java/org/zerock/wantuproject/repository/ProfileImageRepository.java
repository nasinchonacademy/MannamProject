package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.wantuproject.entity.ProfileImage;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
}
