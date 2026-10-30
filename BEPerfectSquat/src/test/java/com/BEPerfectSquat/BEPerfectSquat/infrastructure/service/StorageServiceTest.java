package com.BEPerfectSquat.BEPerfectSquat.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class StorageServiceTest {
    @TempDir
    Path storageDir;

    private StorageService storageService() {
        return new StorageService(storageDir.toString());
    }

    @Test
    void emptyFileIsRejectedAsBadArgument() {
        MockMultipartFile empty = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storageService().store(empty));

        assertEquals("El archivo de vídeo está vacío", ex.getMessage());
    }

    @Test
    void missingFileRaisesStorageException() {
        StorageException ex = assertThrows(StorageException.class,
                () -> storageService().loadAsResource("uploads/does-not-exist.mp4"));

        assertEquals("No se pudo leer el archivo: uploads/does-not-exist.mp4", ex.getMessage());
    }

    @Test
    void storesIphoneMovInTheConfiguredDirectory() throws Exception {
        MockMultipartFile mov = new MockMultipartFile("file", "IMG_0001.MOV", "video/quicktime", "content".getBytes());

        String stored = storageService().store(mov);

        Path storedPath = Path.of(stored);
        assertEquals(storageDir.toAbsolutePath(), storedPath.getParent());
        assertTrue(storedPath.getFileName().toString().endsWith(".MOV"));
        assertEquals("content", Files.readString(storedPath));
    }

    @Test
    void unsupportedExtensionIsRejectedAsBadArgument() {
        MockMultipartFile exe = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "content".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storageService().store(exe));

        assertEquals("Formato de vídeo no soportado: usa .mp4, .mov, .m4v o .webm", ex.getMessage());
    }

    @Test
    void fileWithoutExtensionIsRejectedAsBadArgument() {
        MockMultipartFile noExt = new MockMultipartFile("file", "video", "video/mp4", "content".getBytes());

        assertThrows(IllegalArgumentException.class, () -> storageService().store(noExt));
    }
}
