package com.kerem.phinance.service;

import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.security.JwtTokenProvider;
import com.kerem.phinance.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "pdf", "gif");

    public List<String> uploadFiles(MultipartFile[] files) {
        String userId = SecurityUtils.getCurrentUserId();
        List<String> fileUrls = new ArrayList<>();

        try {
            // Create user-specific directory
            Path userUploadPath = Paths.get(uploadDir, userId);
            if (!Files.exists(userUploadPath)) {
                Files.createDirectories(userUploadPath);
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // Validate file size
                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new BadRequestException("File size exceeds maximum limit of 5MB");
                }

                // Validate file extension
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    throw new BadRequestException("Invalid file name");
                }

                String extension = getFileExtension(originalFilename);
                if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                    throw new BadRequestException("File type not allowed. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
                }

                // Generate unique filename
                String filename = UUID.randomUUID().toString() + "." + extension;
                Path filePath = userUploadPath.resolve(filename);

                // Save file
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Generate temporary access token for this file
                String fileToken = jwtTokenProvider.generateToken(userId);

                // Return relative URL with token
                fileUrls.add("/api/files/" + userId + "/" + filename + "?token=" + fileToken);
            }

            return fileUrls;

        } catch (IOException e) {
            log.error("Failed to upload files", e);
            throw new BadRequestException("Failed to upload files: " + e.getMessage());
        }
    }

    public Resource getFile(String userId, String filename) {
        try {
            Path filePath = Paths.get(uploadDir, userId, filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return null;
            }

            return resource;
        } catch (MalformedURLException e) {
            log.error("Invalid file path", e);
            throw new BadRequestException("Invalid file path");
        }
    }

    public Resource getFileWithAuth(String userId, String filename, String token, String authenticatedUser) {
        // Check authentication - either from authenticated user or token
        String validatedUser = null;

        if (authenticatedUser != null && !authenticatedUser.isEmpty()) {
            validatedUser = authenticatedUser;
        } else if (token != null && !token.isEmpty()) {
            try {
                validatedUser = validateFileToken(token);
            } catch (Exception e) {
                log.error("Invalid file token", e);
                throw new BadRequestException("Invalid or expired token");
            }
        }

        // Security: Ensure user can only access their own files
        if (validatedUser == null || !userId.equals(validatedUser)) {
            throw new BadRequestException("Unauthorized access to file");
        }

        return getFile(userId, filename);
    }

    public String getContentType(String userId, String filename) {
        try {
            Path filePath = Paths.get(uploadDir, userId, filename);
            String contentType = Files.probeContentType(filePath);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            log.error("Failed to determine content type", e);
            return "application/octet-stream";
        }
    }

    public void deleteFile(String userId, String filename) {
        try {
            Path filePath = Paths.get(uploadDir, userId, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file", e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    public String validateFileToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
