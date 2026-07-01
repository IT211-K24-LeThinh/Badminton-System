package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileUploadService {

    FileUploadResponse upload(MultipartFile file);

    List<FileUploadResponse> uploadMultiple(List<MultipartFile> files);

    void delete(String publicId);
}
