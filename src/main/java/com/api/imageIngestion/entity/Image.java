package com.api.imageIngestion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    //Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_id")
    private Long imgId;

    @Column(name = "img_name", nullable = false)
    private String imgName;

    //generated thumbnail
    @Column(name = "thumbnail", columnDefinition = "LONGBLOB")
    private byte[] thumbnail;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "aspect_ratio")
    private Double aspectRatio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private ImageSet imageSet;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
