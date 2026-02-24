package com.api.imageIngestion.service;

import com.api.imageIngestion.dto.*;
import com.api.imageIngestion.entity.Image;
import com.api.imageIngestion.entity.ImageSet;
import com.api.imageIngestion.exception.NotFoundException;
import com.api.imageIngestion.mapper.ImageSetMapper;
import com.api.imageIngestion.repository.ImageRepository;
import com.api.imageIngestion.repository.ImageSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageSetRepository imageSetRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageSetMapper mapper;

    @InjectMocks
    private ImageServiceImpl imageService;

    private MultipartFile mockFile;
    private ImageSetCreateRequestDTO requestDTO;
    private ImageSet savedImageSet;
    private ImageSetResponseDTO responseDTO;

    @BeforeEach
    void setUp() throws Exception {
        // Create a valid JPEG from a BufferedImage
        BufferedImage testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(testImage, "jpg", baos);
        byte[] validJpeg = baos.toByteArray();

        mockFile = mock(MultipartFile.class);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        lenient().when(mockFile.getBytes()).thenReturn(validJpeg);

        DimensionsDTO dim = DimensionsDTO.builder().width(100).height(50).aspectRatio(2.0).build();
        ImageCreateRequestDTO imgReq = ImageCreateRequestDTO.builder()
                .imgName("test.jpg")
                .img(null)
                .dimensions(dim)
                .build();

        requestDTO = ImageSetCreateRequestDTO.builder()
                .imageSetName("test-album")
                .images(List.of(imgReq))
                .build();

        Image image = Image.builder()
                .imgId(1L)
                .imgName("test.jpg")
                .width(100)
                .height(50)
                .aspectRatio(2.0)
                .timestamp(LocalDateTime.now())
                .thumbnail(new byte[]{1, 2, 3})
                .build();

        savedImageSet = ImageSet.builder()
                .setId(1L)
                .setName("test-album")
                .createdAt(LocalDateTime.now())
                .images(List.of(image))
                .build();
        image.setImageSet(savedImageSet);

        responseDTO = ImageSetResponseDTO.builder()
                .imageSetId(1L)
                .imageSetName("test-album")
                .createdAt(LocalDateTime.now())
                .images(List.of(ImageResponseDTO.builder()
                        .imgId(1L)
                        .imgName("test.jpg")
                        .dimensions(dim)
                        .timestamp(LocalDateTime.now())
                        .thumbnail(new byte[]{1, 2, 3})
                        .build()))
                .build();
    }

    @Test
    void createImageSet_withValidFiles_shouldSaveAndReturnDTO() throws Exception {
        when(mapper.toImageEntity(any())).thenReturn(
                Image.builder()
                        .imgName("test.jpg")
                        .width(100)
                        .height(50)
                        .aspectRatio(2.0)
                        .build()
        );
        when(imageSetRepository.save(any())).thenReturn(savedImageSet);
        when(mapper.toResponseDTO(savedImageSet)).thenReturn(responseDTO);

        ImageSetResponseDTO result = imageService.createImageSet(requestDTO, List.of(mockFile));

        assertNotNull(result);
        assertEquals("test-album", result.getImageSetName());
        assertEquals(1, result.getImages().size());
        assertEquals("test.jpg", result.getImages().get(0).getImgName());
        assertNotNull(result.getImages().get(0).getThumbnail());
        verify(imageSetRepository, times(1)).save(any());
    }

    @Test
    void createImageSet_withNullImageDimensions_shouldAutoDetectFromFile() throws Exception {
        ImageCreateRequestDTO imgReq = ImageCreateRequestDTO.builder()
                .imgName("test.jpg")
                .img(null)
                .dimensions(null)
                .build();

        ImageSetCreateRequestDTO req = ImageSetCreateRequestDTO.builder()
                .imageSetName("test-album")
                .images(List.of(imgReq))
                .build();

        Image imageWithoutDim = Image.builder()
                .imgName("test.jpg")
                .width(null)
                .height(null)
                .aspectRatio(null)
                .build();

        when(mapper.toImageEntity(any())).thenReturn(imageWithoutDim);
        when(imageSetRepository.save(any())).thenReturn(savedImageSet);
        when(mapper.toResponseDTO(savedImageSet)).thenReturn(responseDTO);

        ImageSetResponseDTO result = imageService.createImageSet(req, List.of(mockFile));

        assertNotNull(result);
        verify(imageSetRepository, times(1)).save(argThat(set ->
                set.getImages().get(0).getWidth() != null &&
                set.getImages().get(0).getHeight() != null &&
                set.getImages().get(0).getAspectRatio() != null
        ));
    }

    @Test
    void createImageSet_withInvalidImageFile_shouldThrowIllegalArgumentException() throws Exception {
        MultipartFile invalidFile = mock(MultipartFile.class);
        lenient().when(invalidFile.isEmpty()).thenReturn(false);
        when(invalidFile.getBytes()).thenReturn(new byte[]{0x00, 0x01, 0x02});

        when(mapper.toImageEntity(any())).thenReturn(
                Image.builder().imgName("invalid.jpg").build()
        );

        assertThrows(IllegalArgumentException.class,
                () -> imageService.createImageSet(requestDTO, List.of(invalidFile))
        );
    }



    @Test
    /**
     * Test that getImageSet returns the correct DTO when a valid ID is provided, and verify repository interaction
     */
    void getImageSet_withValidId_shouldReturnDTO() {
        when(imageSetRepository.findById(1L)).thenReturn(Optional.of(savedImageSet));
        when(mapper.toResponseDTO(savedImageSet)).thenReturn(responseDTO);

        ImageSetResponseDTO result = imageService.getImageSet(1L);

        assertNotNull(result);
        assertEquals(1L, result.getImageSetId());
        assertEquals("test-album", result.getImageSetName());
        verify(imageSetRepository, times(1)).findById(1L);
    }

    @Test
    /**
     * Test that getImageSet throws NotFoundException when an invalid ID is provided, and verify repository interaction
     */
    void getImageSet_withInvalidId_shouldThrowNotFoundException() {
        when(imageSetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> imageService.getImageSet(999L)
        );
        verify(imageSetRepository, times(1)).findById(999L);
    }
}
