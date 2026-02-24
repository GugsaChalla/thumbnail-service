package com.api.imageIngestion.mapper;

import com.api.imageIngestion.dto.*;
import com.api.imageIngestion.entity.Image;
import com.api.imageIngestion.entity.ImageSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImageSetMapper {

    /**
     * Convert ImageSet entity to ImageSetResponseDTO
     */
    public ImageSetResponseDTO toResponseDTO(ImageSet imageSet) {
        if (imageSet == null) {
            return null;
        }

        //If images exist, convert them to DTOs; otherwise, set to null
        List<ImageResponseDTO> imageDTOs = imageSet.getImages() != null
                ? imageSet.getImages().stream()
                .map(this::toImageResponseDTO)
                .collect(Collectors.toList())
                : null;

        return ImageSetResponseDTO.builder()
                .imageSetId(imageSet.getSetId())
                .imageSetName(imageSet.getSetName())
                .createdAt(imageSet.getCreatedAt())
                .images(imageDTOs)
                .build();
    }

    /**
     * Convert Image entity to ImageResponseDTO
     */
    public ImageResponseDTO toImageResponseDTO(Image image) {
        if (image == null) {
            return null;
        }

        DimensionsDTO dimensionsDTO = null;
        //if user provided any dimesnions, then set the dimensionsDTO; otherwise, leave it as null
        if (image.getWidth() != null || image.getHeight() != null || image.getAspectRatio() != null) {
            dimensionsDTO = DimensionsDTO.builder()
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .aspectRatio(image.getAspectRatio())
                    .build();
        }

        return ImageResponseDTO.builder()
                .imgId(image.getImgId())
                .imgName(image.getImgName())
                .thumbnail(image.getThumbnail())
                .timestamp(image.getTimestamp())
                .dimensions(dimensionsDTO)
                .build();
    }

    /**
     * Convert ImageSetCreateRequestDTO to ImageSet entity
     */
    public ImageSet toImageSetEntity(ImageSetCreateRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        //If images exist in the request, convert them to entities; otherwise, set to null
        List<Image> images = requestDTO.getImages() != null
                ? requestDTO.getImages().stream()
                .map(this::toImageEntity)
                .collect(Collectors.toList())
                : null;

        ImageSet imageSet = ImageSet.builder()
                .setName(requestDTO.getImageSetName())
                .build();

        if (images != null) {
            images.forEach(image -> image.setImageSet(imageSet));
            imageSet.setImages(images);
        }

        return imageSet;
    }

    /**
     * Convert ImageCreateRequestDTO to Image entity
     */
    public Image toImageEntity(ImageCreateRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        Integer width = null;
        Integer height = null;
        Double aspectRatio = null;

        if (requestDTO.getDimensions() != null) {
            width = requestDTO.getDimensions().getWidth();
            height = requestDTO.getDimensions().getHeight();
            aspectRatio = requestDTO.getDimensions().getAspectRatio();
        }

        return Image.builder()
                .imgName(requestDTO.getImgName())
                .width(width)
                .height(height)
                .aspectRatio(aspectRatio)
                .build();
    }

    /**
     * Convert ImageSet entity to ImageSetMetadataResponseDTO (without thumbnail bytes for performance)
     */
    public ImageSetMetadataResponseDTO toImageSetMetadataResponseDTO(ImageSet imageSet) {
        if (imageSet == null) {
            return null;
        }

        List<ImageMetadataDTO> imageDTOs = imageSet.getImages() != null
                ? imageSet.getImages().stream()
                .map(this::toImageMetadataDTO)
                .collect(Collectors.toList())
                : null;

        return ImageSetMetadataResponseDTO.builder()
                .imageSetId(imageSet.getSetId())
                .imageSetName(imageSet.getSetName())
                .createdAt(imageSet.getCreatedAt())
                .images(imageDTOs)
                .build();
    }

    /**
     * Convert Image entity to ImageMetadataDTO (metadata only, no thumbnail bytes)
     */
    public ImageMetadataDTO toImageMetadataDTO(Image image) {
        if (image == null) {
            return null;
        }

        DimensionsDTO dimensionsDTO = null;
        if (image.getWidth() != null || image.getHeight() != null || image.getAspectRatio() != null) {
            dimensionsDTO = DimensionsDTO.builder()
                    .width(image.getWidth())
                    .height(image.getHeight())
                    .aspectRatio(image.getAspectRatio())
                    .build();
        }

        return ImageMetadataDTO.builder()
                .imgId(image.getImgId())
                .imgName(image.getImgName())
                .timestamp(image.getTimestamp())
                .dimensions(dimensionsDTO)
                .build();
    }
}
