package com.upply.auth;

import com.upply.auth.dto.ForgotPasswordRequest;
import com.upply.auth.dto.ResetPasswordRequest;
import com.upply.email.EmailService;
import com.upply.email.EmailTemplate;
import com.upply.exception.custom.BusinessLogicException;
import com.upply.exception.custom.ResourceNotFoundException;
import com.upply.token.ResetPasswordToken;
import com.upply.token.ResetPasswordTokenRepository;
import com.upply.user.User;
import com.upply.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService unit tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authService;

    private User testUser;
    private ForgotPasswordRequest forgotPasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private ResetPasswordToken resetPasswordToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetPasswordBaseUrl", "https://app.upply.tech/reset-password");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .accountActivated(true)
                .accountLocked(false)
                .build();

        forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail("test@example.com");

        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setToken("validToken");
        resetPasswordRequest.setNewPassword("newPassword123");

        resetPasswordToken = ResetPasswordToken.builder()
                .id(1)
                .token("validToken")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("forgotPassword should send reset email when user exists")
    void shouldSendResetEmailWhenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(resetPasswordTokenRepository.save(any(ResetPasswordToken.class))).thenReturn(resetPasswordToken);
        doNothing().when(resetPasswordTokenRepository).markAllTokensAsUsedForUser(1L);

        authService.forgotPassword(forgotPasswordRequest);

        verify(userRepository).findByEmail("test@example.com");
        verify(emailService).sendEmail(
                eq("test@example.com"),
                eq("Reset Your Password"),
                eq(EmailTemplate.ResetPassword),
                anyMap()
        );
    }

    @Test
    @DisplayName("forgotPassword should not throw when user does not exist (prevent email enumeration)")
    void shouldNotThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("nonexistent@example.com");

        assertDoesNotThrow(() -> authService.forgotPassword(request));

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(emailService, never()).sendEmail(anyString(), anyString(), any(EmailTemplate.class), anyMap());
    }

    @Test
    @DisplayName("forgotPassword should not send email when user email is empty")
    void shouldNotSendEmailWhenEmailIsEmpty() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("");

        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.forgotPassword(request));

        verify(emailService, never()).sendEmail(anyString(), anyString(), any(EmailTemplate.class), anyMap());
    }

    @Test
    @DisplayName("resetPassword should throw ResourceNotFoundException when token is invalid")
    void shouldThrowExceptionWhenTokenIsInvalid() {
        when(resetPasswordTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalidToken");
        request.setNewPassword("newPassword123");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals("Invalid reset token", exception.getMessage());
    }

    @Test
    @DisplayName("resetPassword should throw BusinessLogicException when token is already used")
    void shouldThrowExceptionWhenTokenIsAlreadyUsed() {
        resetPasswordToken.setUsed(true);
        when(resetPasswordTokenRepository.findByToken("validToken")).thenReturn(Optional.of(resetPasswordToken));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> authService.resetPassword(resetPasswordRequest)
        );

        assertEquals("Reset token is already used", exception.getMessage());
    }

    @Test
    @DisplayName("resetPassword should throw BusinessLogicException when token is expired")
    void shouldThrowExceptionWhenTokenIsExpired() {
        resetPasswordToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(resetPasswordTokenRepository.findByToken("validToken")).thenReturn(Optional.of(resetPasswordToken));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> authService.resetPassword(resetPasswordRequest)
        );

        assertEquals("Reset token is expired", exception.getMessage());
    }

    @Test
    @DisplayName("resetPassword should update password successfully when token is valid")
    void shouldUpdatePasswordSuccessfully() {
        when(resetPasswordTokenRepository.findByToken("validToken")).thenReturn(Optional.of(resetPasswordToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(resetPasswordTokenRepository.save(resetPasswordToken)).thenReturn(resetPasswordToken);

        authService.resetPassword(resetPasswordRequest);

        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        verify(resetPasswordTokenRepository).save(resetPasswordToken);
        assertTrue(resetPasswordToken.isUsed());
    }

    @Test
    @DisplayName("resetPassword should encode password with BCrypt")
    void shouldEncodePasswordWithBCrypt() {
        when(resetPasswordTokenRepository.findByToken("validToken")).thenReturn(Optional.of(resetPasswordToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(resetPasswordTokenRepository.save(any(ResetPasswordToken.class))).thenReturn(resetPasswordToken);

        authService.resetPassword(resetPasswordRequest);

        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    @DisplayName("resetPassword should mark token as used after successful reset")
    void shouldMarkTokenAsUsedAfterReset() {
        when(resetPasswordTokenRepository.findByToken("validToken")).thenReturn(Optional.of(resetPasswordToken));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(resetPasswordTokenRepository.save(resetPasswordToken)).thenReturn(resetPasswordToken);

        assertFalse(resetPasswordToken.isUsed());

        authService.resetPassword(resetPasswordRequest);

        assertTrue(resetPasswordToken.isUsed());
        verify(resetPasswordTokenRepository).save(resetPasswordToken);
    }

    @Test
    @DisplayName("forgotPassword should invalidate existing tokens for user")
    void shouldInvalidateExistingTokensForUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(resetPasswordTokenRepository).markAllTokensAsUsedForUser(1L);
        when(resetPasswordTokenRepository.save(any(ResetPasswordToken.class))).thenReturn(resetPasswordToken);

        authService.forgotPassword(forgotPasswordRequest);

        verify(resetPasswordTokenRepository).markAllTokensAsUsedForUser(1L);
    }
}