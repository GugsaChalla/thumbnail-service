package com.api.imageIngestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageSetMetadataResponseDTO {

    private Long imageSetId;

    private String imageSetName;

    private LocalDateTime createdAt;

    private List<ImageMetadataDTO> images;
}
