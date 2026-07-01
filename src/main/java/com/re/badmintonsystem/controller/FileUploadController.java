package com.re.badmintonsystem.controller;

import com.re.badmintonsystem.dto.response.ApiResponse;
import com.re.badmintonsystem.dto.response.FileUploadResponse;
import com.re.badmintonsystem.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileUploadService.upload(file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files) {
        List<FileUploadResponse> response = fileUploadService.uploadMultiple(files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Files uploaded successfully", response));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String publicId) {
        fileUploadService.delete(publicId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
