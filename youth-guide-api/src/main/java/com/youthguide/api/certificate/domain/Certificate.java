package com.youthguide.api.certificate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public static Certificate of(String displayName) {
        Certificate c = new Certificate();
        c.displayName = displayName;
        return c;
    }

    public boolean rename(String newName) {
        if (newName.equals(this.displayName)) return false;
        this.displayName = newName;
        return true;
    }
}