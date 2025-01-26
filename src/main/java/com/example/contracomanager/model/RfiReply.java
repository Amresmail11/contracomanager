package com.example.contracomanager.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rfi_replies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfiReply {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfi_id", nullable = false)
    @ToString.Exclude
    private Rfi rfi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RfiReply)) return false;
        RfiReply that = (RfiReply) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public String toString() {
        return "RfiReply{" +
            "id=" + id +
            ", message='" + message + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
} 