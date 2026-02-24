package com.api.imageIngestion.service;

import com.api.imageIngestion.dto.ImageSetCreateRequestDTO;
import com.api.imageIngestion.dto.ImageSetResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    ImageSetResponseDTO createImageSet(ImageSetCreateRequestDTO requestDTO, List<MultipartFile> files) throws Exception;

    ImageSetResponseDTO getImageSet(Long id);
}
