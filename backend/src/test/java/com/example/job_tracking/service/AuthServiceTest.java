package com.example.job_tracking.service;

/*
 * Varsayımlar (kaynağı görmediğim yerler):
 * - RegisterRequest: no-arg constructor + setEmail/setFullname/setPassword setter'ları (getFullname() ile birebir isimlendirme)
 * - LoginRequest: no-arg constructor + setEmail/setPassword setter'ları
 * - User entity: setId, setEmail (ApplicationServiceTest'teki gibi) + setFullName, setPassword, getPassword
 * - AuthResponse: tek parametreli ctor (String token) + getToken()
 */

import com.example.job_tracking.dto.AuthResponse;
import com.example.job_tracking.dto.LoginRequest;
import com.example.job_tracking.dto.RegisterRequest;
import com.example.job_tracking.entity.User;
import com.example.job_tracking.repository.UserRepository;
import com.example.job_tracking.security.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "daria@example.com";
    private static final String RAW_PASSWORD = "plainPassword123";
    private static final String ENCODED_PASSWORD = "encodedHash";
    private static final String TOKEN = "jwt-token-value";

    // ---------- register ----------

    @Test
    void register_success_encodesPasswordSavesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setFullname("Daria K");
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(EMAIL)).thenReturn(TOKEN);

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo(TOKEN);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    void register_emailAlreadyExists_throwsAndDoesNotSaveOrIssueToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    // ---------- login ----------

    @Test
    void login_success_authenticatesAndReturnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);

        when(jwtService.generateToken(EMAIL)).thenReturn(TOKEN);

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo(TOKEN);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo(EMAIL);
        assertThat(authCaptor.getValue().getCredentials()).isEqualTo(RAW_PASSWORD);
    }

    @Test
    void login_badCredentials_throwsAndDoesNotIssueToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verifyNoInteractions(jwtService);
    }
}