package com.upply.auth;

import com.upply.auth.dto.LoginRequest;
import com.upply.auth.dto.LoginResponse;
import com.upply.auth.dto.RegisterRequest;
import com.upply.email.EmailService;
import com.upply.email.EmailTemplate;
import com.upply.security.JwtService;
import com.upply.token.ActivationToken;
import com.upply.token.ActivationTokenRepository;
import com.upply.user.User;
import com.upply.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final ActivationTokenRepository activationTokenRepository;

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    @Value("${app.activation-base-url}")
    private String activationBaseUrl;



    private String generateActivationToken() {

        // class that generates cryptographically secure random numbers
        SecureRandom secureRandom = new SecureRandom();

        byte[] bytes = new byte[32];

        // Fills the bytes array with random values.
        secureRandom.nextBytes(bytes);

        // This converts the random bytes into a URL-safe string
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateAndSaveActivationToken(User user) {

        activationTokenRepository.markAllTokensAsUsedForUser(user.getId());

        String tokenString = generateActivationToken();

        ActivationToken activationToken = ActivationToken.builder()
                .token(tokenString)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .user(user)
                .build();

        // TODO : Hash Activation token before storing it

        activationTokenRepository.save(activationToken);
        return tokenString;
    }

    private void sendActivationEmail(User user) {

        String activationToken = generateAndSaveActivationToken(user);
        String activationLink = activationBaseUrl + "?token=" + activationToken;

        Map<String, Object> vars = new HashMap<>();
        vars.put("firstName", user.getFirstName());
        vars.put("activationLink", activationLink);

        emailService.sendEmail(
                user.getEmail(),
                "Activate Your Account",
                EmailTemplate.Activation,
                vars
        );
    }




    public void register(@Valid RegisterRequest request) {

        String email = request.getEmail();
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isPresent()) {

            User existingUser = optionalUser.get();

            if(existingUser.isAccountActivated()) {
                throw new IllegalArgumentException("Email is already registered");
            }

            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            existingUser.setAccountLocked(false);
            existingUser.setAccountActivated(false);

            userRepository.save(existingUser);
            sendActivationEmail(existingUser);
            return;
        }

        User newUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .accountActivated(false)
                .build();

        userRepository.save(newUser);
        sendActivationEmail(newUser);
    }

    @Transactional
    public void activate(String activationToken) {

        ActivationToken token = activationTokenRepository.findByToken(activationToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation Token"));

        User user = token.getUser();
        if(user.isAccountActivated()) {
            throw new IllegalArgumentException("Account is already activated");
        }

        if(token.isUsed()) {
            throw new IllegalArgumentException("Activation token is already used");
        }

        if(token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Activation Token is expired");
        }

        user.setAccountActivated(true);
        userRepository.save(user);

        token.setUsed(true);
        activationTokenRepository.save(token);
    }

    public LoginResponse login(@Valid LoginRequest request) {

        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var user = ((User) auth.getPrincipal());

        if(!user.isAccountActivated()) {
            throw new IllegalArgumentException("Account is not activated");
        }

        if(user.isAccountLocked()) {
            throw new IllegalArgumentException("Account is locked");
        }

        var claims = new HashMap<String, Object>();
        claims.put("fullName", user.getFirstName());

        var jwtToken = jwtService.generateToken(claims, user);

        return LoginResponse.builder()
                .token(jwtToken)
                .build();
    }
}
