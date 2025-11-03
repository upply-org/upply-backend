package com.upply.security;

import com.upply.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override

    // is important here because it ensures that when the user is loaded from the database, all related data
    // like roles or authorities is fetched within the same open database session.
    // Without it, if those relationships are lazy-loaded,
    // you could get a LazyInitializationException later when Spring tries to access them outside the transaction.
    @Transactional
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {

        // pass email because it's the unique identifier of the user in the app
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with Email: " + userEmail));

    }
}
