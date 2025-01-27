package com.example.contracomanager.repository;

import com.example.contracomanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findAllByUsernameIn(List<String> usernames);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    @Query("SELECT DISTINCT up.user FROM UserProject up WHERE up.project.id = :projectId")
    List<User> findAllByProjectId(@Param("projectId") UUID projectId);
} 