package com.api.imageIngestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageSetCreateRequestDTO {

    private String imageSetName;

    private List<ImageCreateRequestDTO> images;
}
