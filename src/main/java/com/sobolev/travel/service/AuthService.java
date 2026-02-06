package com.sobolev.travel.service;

import com.sobolev.travel.dto.auth.AuthResponse;
import com.sobolev.travel.dto.auth.LoginRequest;
import com.sobolev.travel.dto.auth.RegisterRequest;
import com.sobolev.travel.entity.User;
import com.sobolev.travel.entity.UserProfile;
import com.sobolev.travel.exception.ConflictException;
import com.sobolev.travel.repository.UserRepository;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Servizio per la registrazione e autenticazione degli utenti.
 *
 * - `register` crea un nuovo utente dopo aver verificato unicità username/email
 *   (case-insensitive), codifica la password e genera un token JWT.
 * - `login` usa l'AuthenticationManager per autenticare le credenziali e genera un token.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Case-insensitive check for existing username
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("Username already exists");
        }

        // Case-insensitive check for existing email
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // Create empty profile
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setPreferredUnits(request.preferredUnits() != null ? request.preferredUnits() : "metric");
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        user.setProfile(profile);

        user = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getPasswordHash(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = tokenProvider.generateToken(userDetails);

        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate using usernameOrEmail (case-insensitive handled in CustomUserDetailsService)
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userDetails);

        // Find user by username from the authenticated principal
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}
