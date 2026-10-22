package com.BEPerfectSquat.BEPerfectSquat.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class StorageServiceTest {
    private final StorageService storageService = new StorageService();

    @Test
    void emptyFileIsRejectedAsBadArgument() {
        MockMultipartFile empty = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storageService.store(empty));

        assertEquals("El archivo de vídeo está vacío", ex.getMessage());
    }

    @Test
    void missingFileRaisesStorageException() {
        StorageException ex = assertThrows(StorageException.class,
                () -> storageService.loadAsResource("uploads/does-not-exist.mp4"));

        assertEquals("No se pudo leer el archivo: uploads/does-not-exist.mp4", ex.getMessage());
    }
}
