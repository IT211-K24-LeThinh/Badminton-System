package com.re.badmintonsystem.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.re.badmintonsystem.dto.response.FileUploadResponse;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    private final Cloudinary cloudinary;

    public FileUploadServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "badminton-system/uploads"));

            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            log.info("File uploaded: {} -> {}", file.getOriginalFilename(), url);

            return FileUploadResponse.builder()
                    .url(url)
                    .publicId(publicId)
                    .originalFilename(file.getOriginalFilename())
                    .size(file.getSize())
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public List<FileUploadResponse> uploadMultiple(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one file is required");
        }

        List<FileUploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(upload(file));
        }

        log.info("Uploaded {} files", responses.size());
        return responses;
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new BadRequestException("Public ID is required");
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted file: {}", publicId);
        } catch (IOException e) {
            log.warn("Failed to delete file from Cloudinary: {}", publicId);
            throw new RuntimeException("Failed to delete file: " + publicId, e);
        }
    }
}
