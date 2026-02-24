package com.api.imageIngestion.controller;

import com.api.imageIngestion.repository.ImageSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImageSetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImageSetRepository imageSetRepository;

    private MockMultipartFile validImageFile;
    private String validMetadataJson;

    @BeforeEach
    void setUp() throws Exception {
        // Create a valid test image (100x50 JPEG)
        BufferedImage testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        validImageFile = new MockMultipartFile(
                "files",
                "test-image.jpg",
                "image/jpeg",
                imageBytes
        );

        // Metadata JSON for valid request
        validMetadataJson = """
                {
                    "imageSetName": "test-album",
                    "images": [
                        {
                            "imgName": "test-image.jpg",
                            "dimensions": {
                                "width": 100,
                                "height": 50,
                                "aspectRatio": 2.0
                            }
                        }
                    ]
                }
                """;
    }

    @Test
    void createImageSet_withValidFilesAndMetadata_shouldReturnCreatedWithImageSetId() throws Exception {
        MockMultipartFile metadataPart = new MockMultipartFile(
                "metadata",
                "",
                "application/json",
                validMetadataJson.getBytes()
        );

        mockMvc.perform(multipart("/image-sets")
                .file(validImageFile)
                .file(metadataPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageSetId").isNumber())
                .andExpect(jsonPath("$.imageSetName").value("test-album"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.images", hasSize(1)))
                .andExpect(jsonPath("$.images[0].imgId").isNumber())
                .andExpect(jsonPath("$.images[0].imgName").value("test-image.jpg"))
                .andExpect(jsonPath("$.images[0].thumbnail").isArray())
                .andExpect(jsonPath("$.images[0].timestamp").isNotEmpty());
    }

    @Test
    void createImageSet_withMultipleFiles_shouldCreateSetWithAllImages() throws Exception {
        BufferedImage img2 = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ImageIO.write(img2, "jpg", baos2);

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "image2.jpg",
                "image/jpeg",
                baos2.toByteArray()
        );

        String multiImageMetadata = """
                {
                    "imageSetName": "multi-image-set",
                    "images": [
                        {
                            "imgName": "image1.jpg"
                        },
                        {
                            "imgName": "image2.jpg"
                        }
                    ]
                }
                """;

        mockMvc.perform(multipart("/image-sets")
                .file(validImageFile)
                .file(file2)
                .param("metadata", multiImageMetadata)
                .contentType("multipart/form-data"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.images", hasSize(2)))
                .andExpect(jsonPath("$.images[0].imgName").value("image1.jpg"))
                .andExpect(jsonPath("$.images[1].imgName").value("image2.jpg"));
    }

    @Test
    void createImageSet_withoutFilesParam_shouldReturn400() throws Exception {
        mockMvc.perform(multipart("/image-sets")
                .param("metadata", validMetadataJson)
                .contentType("multipart/form-data"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createImageSet_withoutMetadataParam_shouldReturn400() throws Exception {
        mockMvc.perform(multipart("/image-sets")
                .file(validImageFile)
                .contentType("multipart/form-data"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createImageSet_withEmptyImagesList_shouldReturn400() throws Exception {
        String emptyImagesMetadata = """
                {
                    "imageSetName": "empty-set",
                    "images": []
                }
                """;

        mockMvc.perform(multipart("/image-sets")
                .file(validImageFile)
                .param("metadata", emptyImagesMetadata)
                .contentType("multipart/form-data"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one image entry must be provided"));
    }

    @Test
    void createImageSet_withFileCountMismatch_shouldReturn400() throws Exception {
        String twoImagesMetadata = """
                {
                    "imageSetName": "mismatch-set",
                    "images": [
                        {"imgName": "image1.jpg"},
                        {"imgName": "image2.jpg"}
                    ]
                }
                """;

        mockMvc.perform(multipart("/image-sets")
                .file(validImageFile)
                .param("metadata", twoImagesMetadata)
                .contentType("multipart/form-data"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Number of files must match number of image metadata entries"));
    }

    @Test
    void createImageSet_withEmptyFile_shouldReturn400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "files",
                "empty.jpg",
                "image/jpeg",
                new byte[]{}
        );

        mockMvc.perform(multipart("/image-sets")
                .file(emptyFile)
                .param("metadata", validMetadataJson)
                .contentType("multipart/form-data"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createImageSet_withInvalidImageFormat_shouldReturn400() throws Exception {
        MockMultipartFile invalidImage = new MockMultipartFile(
                "files",
                "invalid.jpg",
                "image/jpeg",
                new byte[]{0x00, 0x01, 0x02, 0x03}
        );

        mockMvc.perform(multipart("/image-sets")
                .file(invalidImage)
                .param("metadata", validMetadataJson)
                .contentType("multipart/form-data"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getImageSet_withValidId_shouldReturnImageSetData() throws Exception {
        // Create an image set first
        var createResponse = mockMvc.perform(multipart("/image-sets")
                .file(validImageFile)
                .param("metadata", validMetadataJson)
                .contentType("multipart/form-data"))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract the ID from response (assuming it's in the JSON)
        String responseContent = createResponse.getResponse().getContentAsString();
        long setId = extractImageSetId(responseContent);

        // Fetch the created set
        mockMvc.perform(get("/image-sets/{id}", setId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageSetId").value(setId))
                .andExpect(jsonPath("$.imageSetName").value("test-album"))
                .andExpect(jsonPath("$.images", hasSize(1)));
    }

    @Test
    void getImageSet_withInvalidId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/image-sets/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private long extractImageSetId(String jsonResponse) {
        // Simple extraction of imageSetId from JSON response
        int startIdx = jsonResponse.indexOf("\"imageSetId\":") + 13;
        int endIdx = jsonResponse.indexOf(",", startIdx);
        if (endIdx == -1) {
            endIdx = jsonResponse.indexOf("}", startIdx);
        }
        return Long.parseLong(jsonResponse.substring(startIdx, endIdx).trim());
    }
}
