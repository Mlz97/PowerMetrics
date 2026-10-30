package com.BEPerfectSquat.BEPerfectSquat.infrastructure.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {
    // ffmpeg normaliza cualquiera de estos contenedores antes del análisis
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".mp4", ".mov", ".m4v", ".webm");

    private final Path rootLocation;

    public StorageService(@Value("${pwr.storage.dir:uploads}") String storageDir) {
        this.rootLocation = Paths.get(storageDir).toAbsolutePath();
        init();
    }

    private void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new StorageException("No se pudo crear el directorio de subida de archivos", e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de vídeo está vacío");
        }
        try {
            // nombre de archivo único
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Formato de vídeo no soportado: usa .mp4, .mov, .m4v o .webm");
            }
            String uniqueName = UUID.randomUUID().toString() + extension;

            Path destinationFile = rootLocation.resolve(Paths.get(uniqueName))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(rootLocation)) {
                // Validación path traversal attacks
                throw new StorageException("No se puede guardar el archivo fuera del directorio actual.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.toString();
        } catch (IOException e) {
            throw new StorageException("Fallo al guardar el archivo.", e);
        }
    }

    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            throw new StorageException("No se pudo borrar el archivo: " + filePath, e);
        }
    }

    public Resource loadAsResource(String filePath) {
        Resource resource;
        try {
            resource = new UrlResource(Paths.get(filePath).toUri());
        } catch (Exception e) {
            throw new StorageException("No se pudo cargar el archivo: " + filePath, e);
        }
        if (!resource.exists() || !resource.isReadable()) {
            throw new StorageException("No se pudo leer el archivo: " + filePath);
        }
        return resource;
    }
}
