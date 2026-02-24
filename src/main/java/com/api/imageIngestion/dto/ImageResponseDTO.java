package com.api.imageIngestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponseDTO {

    private Long imgId;

    private String imgName;

    private byte[] thumbnail;

    private LocalDateTime timestamp;

    private DimensionsDTO dimensions;
}
