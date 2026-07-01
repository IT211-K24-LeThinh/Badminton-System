package com.re.badmintonsystem.service;

import com.re.badmintonsystem.dto.response.CourtImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourtImageService {

    List<CourtImageResponse> getImages(Long courtId);

    List<CourtImageResponse> uploadImages(Long courtId, List<MultipartFile> files);

    void deleteImage(Long courtId, Long imageId);

    void reorderImages(Long courtId, List<Long> imageIds);
}
