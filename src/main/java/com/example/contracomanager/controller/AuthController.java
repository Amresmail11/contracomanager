package com.example.contracomanager.controller;

import com.example.contracomanager.dto.auth.AuthenticationRequest;
import com.example.contracomanager.dto.auth.AuthenticationResponse;
import com.example.contracomanager.dto.auth.RegisterRequest;
import com.example.contracomanager.dto.auth.ClientRegisterRequest;
import com.example.contracomanager.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final Environment env;

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

    @GetMapping("/config-check")
    public ResponseEntity<?> checkConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("database", env.getProperty("spring.datasource.url") != null ? "configured" : "missing");
        config.put("jwt", env.getProperty("jwt.secret") != null ? "configured" : "missing");
        config.put("cors", env.getProperty("cors.allowed-origins") != null ? "configured" : "missing");
        config.put("profile", env.getProperty("spring.profiles.active"));
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Configuration check",
            "config", config
        ));
    }
} 