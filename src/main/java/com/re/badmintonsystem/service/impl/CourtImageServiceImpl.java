package com.re.badmintonsystem.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.re.badmintonsystem.dto.response.CourtImageResponse;
import com.re.badmintonsystem.entity.Court;
import com.re.badmintonsystem.entity.CourtImage;
import com.re.badmintonsystem.exception.BadRequestException;
import com.re.badmintonsystem.exception.ResourceNotFoundException;
import com.re.badmintonsystem.repository.CourtImageRepository;
import com.re.badmintonsystem.repository.CourtRepository;
import com.re.badmintonsystem.service.CourtImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourtImageServiceImpl implements CourtImageService {

    private static final Logger log = LoggerFactory.getLogger(CourtImageServiceImpl.class);

    private final CourtImageRepository courtImageRepository;
    private final CourtRepository courtRepository;
    private final Cloudinary cloudinary;

    public CourtImageServiceImpl(CourtImageRepository courtImageRepository,
                                  CourtRepository courtRepository,
                                  Cloudinary cloudinary) {
        this.courtImageRepository = courtImageRepository;
        this.courtRepository = courtRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtImageResponse> getImages(Long courtId) {
        List<CourtImage> images = courtImageRepository.findByCourtIdOrderByDisplayOrderAsc(courtId);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CourtImageResponse> uploadImages(Long courtId, List<MultipartFile> files) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court", "id", courtId));

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one image file is required");
        }

        // Get current max display order
        List<CourtImage> existing = courtImageRepository.findByCourtIdOrderByDisplayOrderAsc(courtId);
        int nextOrder = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getDisplayOrder() + 1;

        List<CourtImageResponse> responses = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                        ObjectUtils.asMap("folder", "badminton-system/courts/" + courtId));

                CourtImage image = new CourtImage();
                image.setCourt(court);
                image.setImageUrl((String) uploadResult.get("secure_url"));
                image.setPublicId((String) uploadResult.get("public_id"));
                image.setDisplayOrder(nextOrder + i);
                image.setPrimary(existing.isEmpty() && i == 0); // First image is primary if no existing

                image = courtImageRepository.save(image);
                responses.add(toResponse(image));

                log.info("Uploaded image for court {}: {}", courtId, image.getImageUrl());
            } catch (IOException e) {
                log.error("Failed to upload image for court {}: {}", courtId, e.getMessage());
                throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename(), e);
            }
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteImage(Long courtId, Long imageId) {
        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("CourtImage", "id", imageId));

        if (!image.getCourt().getId().equals(courtId)) {
            throw new BadRequestException("Image does not belong to this court");
        }

        // Delete from Cloudinary
        try {
            cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.warn("Failed to delete image from Cloudinary: {}", image.getPublicId());
        }

        courtImageRepository.delete(image);
        log.info("Deleted court image: id={}, publicId={}", imageId, image.getPublicId());
    }

    @Override
    @Transactional
    public void reorderImages(Long courtId, List<Long> imageIds) {
        List<CourtImage> images = courtImageRepository.findByCourtId(courtId);

        if (imageIds.size() != images.size()) {
            throw new BadRequestException("Must provide all image IDs for this court");
        }

        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            CourtImage image = images.stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Image " + imageId + " not found in this court"));

            image.setDisplayOrder(i);
            image.setPrimary(i == 0);
            courtImageRepository.save(image);
        }

        log.info("Reordered images for court {}", courtId);
    }

    private CourtImageResponse toResponse(CourtImage image) {
        return CourtImageResponse.builder()
                .id(image.getId())
                .courtId(image.getCourt().getId())
                .imageUrl(image.getImageUrl())
                .publicId(image.getPublicId())
                .isPrimary(image.isPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }
}
