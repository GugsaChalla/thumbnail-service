package com.api.imageIngestion.controller;

import com.api.imageIngestion.dto.ImageSetCreateRequestDTO;
import com.api.imageIngestion.dto.ImageSetMetadataResponseDTO;
import com.api.imageIngestion.dto.ImageSetResponseDTO;
import com.api.imageIngestion.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/image-sets")
public class ImageSetController {

    private final ImageService imageService;

    public ImageSetController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Endpoint to create a new ImageSet, accepting multipart/form-data with a JSON part for metadata and file parts for images. Returns 201 Created with the created ImageSetResponseDTO on success, or appropriate error responses for validation failures.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageSetResponseDTO> createImageSet(
            @RequestPart("metadata") ImageSetCreateRequestDTO metadata,
            @RequestPart(value = "files") List<MultipartFile> files
    ) throws Exception {
        // Validate files are provided and not empty
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one image file must be provided");
        }
        
        if (metadata.getImages() == null || metadata.getImages().isEmpty()) {
            throw new IllegalArgumentException("At least one image entry must be provided");
        }
        
        if (files.size() != metadata.getImages().size()) {
            throw new IllegalArgumentException("Number of files must match number of image metadata entries");
        }
        
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).isEmpty()) {
                throw new IllegalArgumentException("File at index " + i + " is empty");
            }
        }
        
        ImageSetResponseDTO dto = imageService.createImageSet(metadata, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Endpoint to retrieve an ImageSet by ID, returning 200 OK with the ImageSetResponseDTO if found, or 404 Not Found if the ID does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImageSetResponseDTO> getImageSet(@PathVariable Long id) {
        ImageSetResponseDTO dto = imageService.getImageSet(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Endpoint to retrieve ImageSet metadata (without thumbnail bytes) by ID for lightweight queries. Returns 200 OK with ImageSetMetadataResponseDTO if found, or 404 Not Found if the ID does not exist.
     */
    @GetMapping("/{id}/metadata")
    public ResponseEntity<ImageSetMetadataResponseDTO> getImageSetMetadata(@PathVariable Long id) {
        ImageSetMetadataResponseDTO dto = imageService.getImageSetMetadata(id);
        return ResponseEntity.ok(dto);
    }
}
