package com.company.project.auth.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.project.auth.auth.dto.SignupRequest;
import com.company.project.auth.user.AppUser;
import com.company.project.auth.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signupRejectsAnAlreadyRegisteredEmailIgnoringCase() {
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(new SignupRequest("Ada", "  ADA@example.com ", "password123")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void signupStoresNormalisedEmailAndHashedPassword() {
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("{bcrypt}hash");
        when(userRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.signup(new SignupRequest(" Ada Lovelace ", "Ada@Example.com", "password123"));

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ada@example.com");
        assertThat(saved.getValue().getFullName()).isEqualTo("Ada Lovelace");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("{bcrypt}hash");
        verify(tokenService).issueFor(saved.getValue());
    }
}
