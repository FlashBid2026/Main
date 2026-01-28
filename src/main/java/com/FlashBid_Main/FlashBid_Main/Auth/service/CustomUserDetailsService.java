package com.FlashBid_Main.FlashBid_Main.Auth.service;

import com.FlashBid_Main.FlashBid_Main.Auth.domain.CustomUserDetails;
import com.FlashBid_Main.FlashBid_Main.Auth.domain.User;
import com.FlashBid_Main.FlashBid_Main.Auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return CustomUserDetails.from(user);
    }
}
