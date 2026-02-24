package com.api.imageIngestion.mapper;

import com.api.imageIngestion.dto.*;
import com.api.imageIngestion.entity.Image;
import com.api.imageIngestion.entity.ImageSet;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageSetMapperTest {

    private final ImageSetMapper mapper = new ImageSetMapper();

    @Test
    void toImageSetEntity_mapsFieldsAndSetsBackReference() {
        DimensionsDTO mockDim = DimensionsDTO.builder().width(100).height(200).aspectRatio(0.5).build();
        ImageCreateRequestDTO imgReq = ImageCreateRequestDTO.builder()
                .imgName("pic.jpg")
                .dimensions(mockDim)
                .img(null)
                .build();
        ImageSetCreateRequestDTO mockSetReq = ImageSetCreateRequestDTO.builder()
                .imageSetName("vacation")
                .images(List.of(imgReq))
                .build();

        ImageSet entity = mapper.toImageSetEntity(mockSetReq);
        assertNotNull(entity);
        assertEquals("vacation", entity.getSetName());
        assertNotNull(entity.getImages());
        assertEquals(1, entity.getImages().size());

        Image img = entity.getImages().get(0);
        assertEquals("pic.jpg", img.getImgName());
        assertSame(entity, img.getImageSet());
        assertEquals(100, img.getWidth());
        assertEquals(200, img.getHeight());
        assertEquals(0.5, img.getAspectRatio());
    }

    @Test
    void toResponseDTO_mapsEntityToDto() {
        ImageSet mockSet = ImageSet.builder()
                .setId(10L)
                .setName("album")
                .createdAt(LocalDateTime.of(2026,1,1,0,0))
                .build();

        Image mockImage = Image.builder()
                .imgId(20L)
                .imgName("photo.png")
                .thumbnail(new byte[]{1,2,3})
                .timestamp(LocalDateTime.of(2026,1,2,0,0))
                .width(50)
                .height(25)
                .aspectRatio(2.0)
                .imageSet(mockSet)
                .build();

        mockSet.setImages(List.of(mockImage));

        ImageSetResponseDTO dto = mapper.toResponseDTO(mockSet);
        assertNotNull(dto);
        assertEquals(10L, dto.getImageSetId());
        assertEquals("album", dto.getImageSetName());
        assertEquals(1, dto.getImages().size());

        ImageResponseDTO idto = dto.getImages().get(0);
        assertEquals(20L, idto.getImgId());
        assertArrayEquals(new byte[]{1,2,3}, idto.getThumbnail());
        assertNotNull(idto.getDimensions());
        assertEquals(50, idto.getDimensions().getWidth());
    }
}
