package com.example.contracomanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    @Builder.Default
    private String code = "PROJ-" + String.format("%03d", new Random().nextInt(900) + 100);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "due_date")
    private ZonedDateTime dueDate;

    @Column(name = "project_owner")
    private String projectOwner;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "project")
    @Where(clause = "role IN ('MEMBER', 'ADMIN')")
    @Builder.Default
    private Set<UserProject> userProjects = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        if (code == null || code.trim().isEmpty()) {
            code = "PROJ-" + String.format("%03d", new Random().nextInt(900) + 100);
        }
    }

    public boolean hasAccess(User user) {
        if (user == null) return false;
        if (createdBy != null && createdBy.getId().equals(user.getId())) return true;
        return userProjects.stream()
            .anyMatch(up -> up.getUser().getId().equals(user.getId()));
    }

    public boolean isAdmin(User user) {
        return userProjects.stream()
            .anyMatch(up -> up.getUser().getId().equals(user.getId()) && up.getRole().equals("ADMIN"));
    }

    public Set<User> getMembers() {
        return userProjects.stream()
            .map(UserProject::getUser)
            .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return id != null && id.equals(project.getId());
    }

    @Override
    public String toString() {
        return "Project{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", code='" + code + '\'' +
            '}';
    }
}