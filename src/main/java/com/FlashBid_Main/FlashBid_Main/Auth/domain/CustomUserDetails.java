package com.FlashBid_Main.FlashBid_Main.Auth.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String userId;
    private final String password;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomUserDetails(Long id, String userId, String password, String nickname,
                              Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(User user) {
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new CustomUserDetails(
            user.getId(),
            user.getUserId(),
            user.getPassword(),
            user.getNickname(),
            authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
