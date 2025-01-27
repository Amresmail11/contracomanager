package com.example.contracomanager.service;

import com.example.contracomanager.dto.auth.AuthenticationRequest;
import com.example.contracomanager.dto.auth.AuthenticationResponse;
import com.example.contracomanager.dto.auth.ClientRegisterRequest;
import com.example.contracomanager.dto.auth.RegisterRequest;
import com.example.contracomanager.model.User;
import com.example.contracomanager.model.UserRole;
import com.example.contracomanager.repository.UserRepository;
import com.example.contracomanager.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .job(request.getJob())
                .createdAt(ZonedDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        
        String jwtToken = jwtService.generateToken(new SecurityUser(savedUser));
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();
    }

    @Transactional
    public AuthenticationResponse registerClient(ClientRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .job(request.getJob())
                .createdAt(ZonedDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        
        String jwtToken = jwtService.generateToken(savedUser);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authenticate using email
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),  // Use email for authentication
                request.getPassword()
            )
        );
        
        // Find user by email to get their stored username and other details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Create SecurityUser with the complete user object
        SecurityUser securityUser = new SecurityUser(user);
        
        // Generate token using SecurityUser (which will use the stored username)
        String jwtToken = jwtService.generateToken(securityUser);
        
        // Build response with the stored username
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .username(user.getUsername())  // Use the stored username
                .role(user.getRole())
                .build();
    }

    public boolean validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String userEmail = jwtService.extractUsername(token);
            User userDetails = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return jwtService.isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    public AuthenticationResponse refreshToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String userEmail = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (jwtService.isTokenValid(token, user)) {
            String newToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder()
                    .token(newToken)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();
        }
        
        throw new IllegalStateException("Invalid token");
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }
} 