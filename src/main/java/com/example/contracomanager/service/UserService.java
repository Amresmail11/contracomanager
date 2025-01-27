package com.example.contracomanager.service;

import com.example.contracomanager.model.User;
import com.example.contracomanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UUID> getUidByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(User::getId);
    }
}