package com.api.imageIngestion.service;

import com.api.imageIngestion.dto.ImageCreateRequestDTO;
import com.api.imageIngestion.dto.ImageSetCreateRequestDTO;
import com.api.imageIngestion.dto.ImageSetResponseDTO;
import com.api.imageIngestion.entity.Image;
import com.api.imageIngestion.entity.ImageSet;
import com.api.imageIngestion.exception.NotFoundException;
import com.api.imageIngestion.mapper.ImageSetMapper;
import com.api.imageIngestion.repository.ImageRepository;
import com.api.imageIngestion.repository.ImageSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageSetRepository imageSetRepository;
    private final ImageRepository imageRepository;
    private final ImageSetMapper mapper;

    public ImageServiceImpl(ImageSetRepository imageSetRepository,
                            ImageRepository imageRepository,
                            ImageSetMapper mapper) {
        this.imageSetRepository = imageSetRepository;
        this.imageRepository = imageRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    /**
     * Create ImageSet and associated Images from request DTO and files, handling optional dimensions and thumbnail generation.
     */
    public ImageSetResponseDTO createImageSet(ImageSetCreateRequestDTO requestDTO, List<MultipartFile> files) throws Exception {
        List<Image> images = new ArrayList<>();

        List<ImageCreateRequestDTO> imgDtos = requestDTO.getImages();

        if (imgDtos != null) {
            for (int i = 0; i < imgDtos.size(); i++) {
                ImageCreateRequestDTO imgDto = imgDtos.get(i);
                MultipartFile file = files.get(i);

                Image image = mapper.toImageEntity(imgDto);

                if (file != null) {
                    byte[] bytes = file.getBytes();
                    // set width/height if not provided
                    try {
                        BufferedImage orig = ImageIO.read(new ByteArrayInputStream(bytes));
                        if (orig != null) {
                            if (image.getWidth() == null) image.setWidth(orig.getWidth());
                            if (image.getHeight() == null) image.setHeight(orig.getHeight());
                            if (image.getAspectRatio() == null && image.getWidth() != null && image.getHeight() != null) {
                                image.setAspectRatio((double) image.getWidth() / image.getHeight());
                            }
                        } else {
                            throw new IllegalArgumentException("Unable to read image at index " + i + ": unsupported format or corrupted file");
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Failed to parse image at index " + i + ": " + e.getMessage(), e);
                    }

                    // create thumbnail
                    try {
                        byte[] thumb = createThumbnail(bytes, 150);
                        image.setThumbnail(thumb);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Failed to generate thumbnail for image at index " + i + ": " + e.getMessage(), e);
                    }
                }

                images.add(image);
            }
        }

        ImageSet imageSet = ImageSet.builder()
                .setName(requestDTO.getImageSetName())
                .build();

        // associate images with set
        if (!images.isEmpty()) {
            //associate each image with the image set
            images.forEach(img -> img.setImageSet(imageSet));
            //then set the list of images in the image set
            imageSet.setImages(images);
        }

        ImageSet saved = imageSetRepository.save(imageSet);
        return mapper.toResponseDTO(saved);
    }

    @Override
    /**
     * Fetch ImageSet by ID, throw NotFoundException if not found, and convert to DTO
     */
    public ImageSetResponseDTO getImageSet(Long id) {
        ImageSet set = imageSetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ImageSet not found: " + id));
        return mapper.toResponseDTO(set);
    }

    /**
     * Helper method to create a thumbnail from original image bytes, maintaining aspect ratio and fitting within maxWidth
     */
    private byte[] createThumbnail(byte[] original, int maxWidth) throws Exception {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
        if (img == null) return null;

        int width = img.getWidth();
        int height = img.getHeight();
        int newWidth = Math.min(maxWidth, width);
        int newHeight = (int) ((double) newWidth / width * height);

        java.awt.Image scaled = img.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = out.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(out, "jpg", baos);
            return baos.toByteArray();
        }
    }
}
