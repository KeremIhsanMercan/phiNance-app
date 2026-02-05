package com.kerem.phinance.controller;

import com.kerem.phinance.security.SecurityUtils;
import com.kerem.phinance.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(
            @RequestParam("files") MultipartFile[] files) {

        List<String> fileUrls = fileService.uploadFiles(files);
        return ResponseEntity.ok(fileUrls);
    }

    @GetMapping("/{userId}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String userId,
            @PathVariable String filename,
            @RequestParam(required = false) String token,
            Authentication authentication) {

        // Security: Get userId from authenticated session or validate token
        String currentUserId = null;

        // Try to get from authenticated session first
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                currentUserId = SecurityUtils.getCurrentUserId();
            } catch (Exception e) {
                // Not authenticated via session, will try token
            }
        }

        Resource resource = fileService.getFileWithAuth(userId, filename, token, currentUserId);

        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = fileService.getContentType(userId, filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{userId}/{filename:.+}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String userId,
            @PathVariable String filename) {

        // Security: Always use the authenticated user's ID, ignore userId from path
        String currentUserId = SecurityUtils.getCurrentUserId();

        fileService.deleteFile(currentUserId, filename);
        return ResponseEntity.ok().build();
    }
}
