package org.zerock.wantuproject.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.wantuproject.entity.User;
import org.zerock.wantuproject.repository.UserRepository;

import static org.zerock.wantuproject.entity.QUser.user;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public User loadUserByUsername(String uid) throws UsernameNotFoundException {
        return userRepository.findByUid(uid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + uid));


    }
}