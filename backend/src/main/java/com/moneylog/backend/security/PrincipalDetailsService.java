package com.moneylog.backend.security;

import com.moneylog.backend.entity.User;
import com.moneylog.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일 입니다"));

        if(user.getDeletedAt() != null) {
            throw new UsernameNotFoundException("탈퇴한 계정 입니다");
        }

        return new PrincipalDetails(user);
    }

}
