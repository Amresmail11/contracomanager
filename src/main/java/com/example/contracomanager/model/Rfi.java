package com.example.contracomanager.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "rfis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rfi {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String priority;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "assigned_type")
    private String assignedType; // "USER" or "GROUP"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_group_id")
    private Group assignedToGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @OneToMany(mappedBy = "rfi", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RfiReply> replies = new HashSet<>();

    // Helper methods for managing bidirectional relationship
    public void addReply(RfiReply reply) {
        replies.add(reply);
        reply.setRfi(this);
    }

    public void removeReply(RfiReply reply) {
        replies.remove(reply);
        reply.setRfi(null);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rfi)) return false;
        Rfi rfi = (Rfi) o;
        return id != null && id.equals(rfi.getId());
    }

    @Override
    public String toString() {
        return "Rfi{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", status='" + status + '\'' +
            '}';
    }
} 