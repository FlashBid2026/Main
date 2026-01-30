package com.FlashBid_Main.FlashBid_Main.Gcs.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("GcsService 통합 테스트")
class GcsServiceIntegrationTest {

    @Autowired
    private GcsService gcsService;

    private Storage storage;
    private final String bucketName = "junyeong_buket";
    private String uploadedFileName;

    @BeforeEach
    void setUp() {
        storage = StorageOptions.getDefaultInstance().getService();
        uploadedFileName = null;
    }

    @AfterEach
    void tearDown() {
        if (uploadedFileName != null) {
            storage.delete(bucketName, uploadedFileName);
        }
    }

    @Test
    @DisplayName("이미지 업로드 성공 - 실제 GCS에 파일이 업로드되는지 확인")
    void uploadImage_실제_GCS_업로드_성공() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "test-image.png",
            "image/png",
            "test image content".getBytes()
        );

        uploadedFileName = gcsService.uploadImage(file);

        assertThat(uploadedFileName).isNotNull();
        assertThat(uploadedFileName).endsWith("_test-image.png");

        Blob blob = storage.get(bucketName, uploadedFileName);
        assertThat(blob).isNotNull();
        assertThat(blob.exists()).isTrue();
    }

    @Test
    @DisplayName("업로드된 파일명이 UUID_원본파일명 형식인지 확인")
    void uploadImage_파일명_형식_검증() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "myPhoto.jpg",
            "image/jpeg",
            "jpeg content".getBytes()
        );

        uploadedFileName = gcsService.uploadImage(file);

        assertThat(uploadedFileName).matches(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_myPhoto\\.jpg$"
        );
    }

    @Test
    @DisplayName("다른 ContentType으로 업로드 테스트")
    void uploadImage_다른_ContentType_테스트() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "photo.jpeg",
            "image/jpeg",
            "jpeg content".getBytes()
        );

        uploadedFileName = gcsService.uploadImage(file);

        Blob blob = storage.get(bucketName, uploadedFileName);
        assertThat(blob).isNotNull();
        assertThat(blob.getContentType()).isEqualTo("image/jpeg");
    }
}
