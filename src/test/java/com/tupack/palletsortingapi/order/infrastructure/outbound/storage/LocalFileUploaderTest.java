package com.tupack.palletsortingapi.order.infrastructure.outbound.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.common.config.StorageProperties;
import com.tupack.palletsortingapi.common.exception.FileUploadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CRITICAL SECURITY TEST: Verifies path traversal vulnerability is fixed.
 *
 * Tests ensure that LocalFileUploader:
 * 1. Rejects path traversal attempts (../../etc/passwd)
 * 2. Validates file extensions against whitelist
 * 3. Enforces file size limits
 * 4. Sanitizes filenames properly
 * 5. Only writes files within the configured base directory
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LocalFileUploader - Critical Security Tests")
class LocalFileUploaderTest {

    @TempDir
    Path tempDir;

    @Mock
    private StorageProperties storageProperties;

    private LocalFileUploader fileUploader;

    @BeforeEach
    void setUp() {
        when(storageProperties.getBasePath()).thenReturn(tempDir.toString());
        when(storageProperties.getAllowedExtensions()).thenReturn(
            List.of("pdf", "jpg", "jpeg", "png", "doc", "docx", "txt"));
        when(storageProperties.getMaxFileSize()).thenReturn(10485760L); // 10MB

        fileUploader = new LocalFileUploader(storageProperties);
    }

    @AfterEach
    void cleanUp() throws IOException {
        // Clean up test uploads directory
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
        }
    }

    @Test
    @DisplayName("SECURITY: Should reject path traversal attempt with ../")
    void shouldRejectPathTraversalAttempt() {
        // Given: Malicious path traversal attempt
        String maliciousPath = "../../etc/passwd";
        byte[] content = "malicious content".getBytes();

        // When/Then: Should throw FileUploadException
        assertThatThrownBy(() -> fileUploader.upload(maliciousPath, content))
            .isInstanceOf(FileUploadException.class)
            .hasMessageContaining("Invalid file path");
    }

    @Test
    @DisplayName("SECURITY: Should reject absolute path attempt")
    void shouldRejectAbsolutePathAttempt() {
        // Given: Attempt to write to absolute path
        String absolutePath = "/etc/passwd";
        byte[] content = "malicious content".getBytes();

        // When/Then: Should throw FileUploadException
        assertThatThrownBy(() -> fileUploader.upload(absolutePath, content))
            .isInstanceOf(FileUploadException.class)
            .hasMessageContaining("Invalid file");
    }

    @Test
    @DisplayName("SECURITY: Should reject file with disallowed extension")
    void shouldRejectDisallowedExtension() {
        // Given: File with executable extension
        String filename = "malicious.exe";
        byte[] content = "malicious content".getBytes();

        // When/Then: Should throw FileUploadException
        assertThatThrownBy(() -> fileUploader.upload(filename, content))
            .isInstanceOf(FileUploadException.class)
            .hasMessageContaining("File type not allowed");
    }

    @Test
    @DisplayName("SECURITY: Should reject file without extension")
    void shouldRejectFileWithoutExtension() {
        // Given: File without extension
        String filename = "noextension";
        byte[] content = "content".getBytes();

        // When/Then: Should throw FileUploadException
        assertThatThrownBy(() -> fileUploader.upload(filename, content))
            .isInstanceOf(FileUploadException.class)
            .hasMessageContaining("File must have an extension");
    }

    @Test
    @DisplayName("SECURITY: Should reject file exceeding size limit")
    void shouldRejectOversizedFile() {
        // Given: File larger than max size (10MB)
        String filename = "large.pdf";
        byte[] content = new byte[11 * 1024 * 1024]; // 11MB

        // When/Then: Should throw FileUploadException
        assertThatThrownBy(() -> fileUploader.upload(filename, content))
            .isInstanceOf(FileUploadException.class)
            .hasMessageContaining("File size exceeds maximum");
    }

    @Test
    @DisplayName("SECURITY: Should sanitize filename with dangerous characters")
    void shouldSanitizeDangerousCharacters() throws Exception {
        // Given: Filename with dangerous characters
        String filename = "test<script>alert('xss')</script>.pdf";
        byte[] content = "test content".getBytes();

        // When: Upload file
        String uploadedPath = fileUploader.upload(filename, content);

        // Then: File should be uploaded with sanitized name
        assertThat(uploadedPath).isNotNull();
        assertThat(uploadedPath).contains(tempDir.toString());
        assertThat(uploadedPath).doesNotContain("<script>");
        assertThat(uploadedPath).doesNotContain("</script>");

        // Verify file was created within base directory
        Path uploadedFile = Paths.get(uploadedPath);
        assertThat(uploadedFile.normalize().startsWith(tempDir.normalize())).isTrue();
    }

    @Test
    @DisplayName("Should successfully upload valid file")
    void shouldUploadValidFile() throws Exception {
        // Given: Valid file
        String filename = "document.pdf";
        byte[] content = "PDF content".getBytes();

        // When: Upload file
        String uploadedPath = fileUploader.upload(filename, content);

        // Then: File should be uploaded successfully
        assertThat(uploadedPath).isNotNull();
        Path uploadedFile = Paths.get(uploadedPath);
        assertThat(Files.exists(uploadedFile)).isTrue();
        assertThat(Files.readAllBytes(uploadedFile)).isEqualTo(content);

        // Verify file is within base directory (no path traversal)
        assertThat(uploadedFile.normalize().startsWith(tempDir.normalize())).isTrue();
    }

    @Test
    @DisplayName("Should generate unique filenames to prevent collisions")
    void shouldGenerateUniqueFilenames() throws Exception {
        // Given: Same filename uploaded twice
        String filename = "document.pdf";
        byte[] content1 = "First upload".getBytes();
        byte[] content2 = "Second upload".getBytes();

        // When: Upload same filename twice
        String path1 = fileUploader.upload(filename, content1);
        String path2 = fileUploader.upload(filename, content2);

        // Then: Should generate different paths with UUID
        assertThat(path1).isNotEqualTo(path2);
        assertThat(Files.exists(Paths.get(path1))).isTrue();
        assertThat(Files.exists(Paths.get(path2))).isTrue();
    }

    @Test
    @DisplayName("Should handle various allowed extensions")
    void shouldHandleAllowedExtensions() throws Exception {
        // Given: Files with various allowed extensions
        String[] allowedFiles = {"doc.pdf", "image.jpg", "photo.png", "file.docx", "notes.txt"};

        // When/Then: All should upload successfully
        for (String filename : allowedFiles) {
            byte[] content = ("Content of " + filename).getBytes();
            String path = fileUploader.upload(filename, content);

            assertThat(path).isNotNull();
            assertThat(Files.exists(Paths.get(path))).isTrue();
        }
    }

    @Test
    @DisplayName("SECURITY: Should prevent hidden file creation")
    void shouldPreventHiddenFiles() throws Exception {
        // Given: Filename starting with dot (hidden file)
        String filename = ".hidden.pdf";
        byte[] content = "content".getBytes();

        // When: Upload file
        String uploadedPath = fileUploader.upload(filename, content);

        // Then: Filename should not start with dot
        Path uploadedFile = Paths.get(uploadedPath);
        String actualFilename = uploadedFile.getFileName().toString();
        assertThat(actualFilename).doesNotStartWith(".");
    }

    @Test
    @DisplayName("Should reject empty filename")
    void shouldRejectEmptyFilename() {
        // Given: Empty filename
        String filename = "";
        byte[] content = "content".getBytes();

        // When/Then: Should throw FileUploadException
        assertThatThrownBy(() -> fileUploader.upload(filename, content))
            .isInstanceOf(FileUploadException.class)
            .hasMessageContaining("Filename cannot be empty");
    }

    @Test
    @DisplayName("Should reject null filename")
    void shouldRejectNullFilename() {
        // Given: Null filename
        byte[] content = "content".getBytes();

        // When/Then: Should throw FileUploadException
        assertThatThrownBy(() -> fileUploader.upload(null, content))
            .isInstanceOf(FileUploadException.class)
            .hasMessageContaining("Filename cannot be empty");
    }
}
