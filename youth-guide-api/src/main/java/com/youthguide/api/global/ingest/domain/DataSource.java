package com.youthguide.api.global.ingest.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_source")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "source_id")
    private Long id;

    @Column(name = "source_key", nullable = false, unique = true, length = 128)
    private String sourceKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "official_url", nullable = false, columnDefinition = "text")
    private String officialUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public static DataSource of(String sourceKey, String displayName, String officialUrl) {
        DataSource s = new DataSource();
        s.sourceKey = sourceKey;
        s.displayName = displayName;
        s.officialUrl = officialUrl;
        return s;
    }
}