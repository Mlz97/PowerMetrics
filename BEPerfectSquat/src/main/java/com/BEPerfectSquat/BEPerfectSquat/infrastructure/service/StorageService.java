package com.BEPerfectSquat.BEPerfectSquat.infrastructure.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final Path rootLocation;

    public StorageService() {
        this.rootLocation = Paths.get("uploads");
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
            String uniqueName = UUID.randomUUID().toString() + extension;

            Path destinationFile = rootLocation.resolve(Paths.get(uniqueName))
                    .normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
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
