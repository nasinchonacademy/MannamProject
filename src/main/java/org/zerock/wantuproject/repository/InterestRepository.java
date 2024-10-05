package org.zerock.wantuproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.wantuproject.entity.Interest;
import org.zerock.wantuproject.entity.User;

import java.util.List;

public interface InterestRepository extends JpaRepository<Interest, Long> {


}
