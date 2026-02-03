package com.kerem.phinance.service;

import com.kerem.phinance.dto.auth.LoginRequest;
import com.kerem.phinance.dto.auth.RegisterRequest;
import com.kerem.phinance.dto.auth.AuthResponse;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.model.User;
import com.kerem.phinance.repository.CategoryRepository;
import com.kerem.phinance.repository.UserRepository;
import com.kerem.phinance.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setPreferredCurrency("USD");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId("user123");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getJwtExpiration()).thenReturn(86400000L);
        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refreshToken");
        when(jwtTokenProvider.getJwtExpiration()).thenReturn(86400000L);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("test@example.com", response.getUser().getEmail());
    }
}
