package com.example.contracomanager.controller;

import com.example.contracomanager.dto.auth.AuthenticationRequest;
import com.example.contracomanager.dto.auth.AuthenticationResponse;
import com.example.contracomanager.dto.auth.RegisterRequest;
import com.example.contracomanager.dto.auth.ClientRegisterRequest;
import com.example.contracomanager.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        log.info("Processing registration request for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/register/client")
    public ResponseEntity<AuthenticationResponse> registerClient(
            @RequestBody @Valid ClientRegisterRequest request
    ) {
        log.info("Processing client registration request for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.registerClient(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        log.info("Processing login request for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(authenticationService.validateToken(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(authenticationService.refreshToken(token));
    }
} 