package com.diangraha_backend.diangraha_backend.service;
import com.diangraha_backend.diangraha_backend.dto.AchievementRequest;
import com.diangraha_backend.diangraha_backend.dto.AchievementResponse;
import com.diangraha_backend.diangraha_backend.entity.Achievement;
import com.diangraha_backend.diangraha_backend.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final AchievementRepository repository;
    private final FileStorageService fileStorageService;

    public List<AchievementResponse> getAllAchievements() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET by ID
    public AchievementResponse findById(Long id) {
        Achievement achievement = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));
        return mapToResponse(achievement);
    }

    public List<AchievementResponse> getLimitedAchievements(int limit) {
        return repository.findAll().stream()
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // CREATE with file upload
    public AchievementResponse create(AchievementRequest request, MultipartFile imageFile) throws IOException {
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileStorageService.storeFile(imageFile, "achievements");
        }

        Achievement achievement = Achievement.builder()
                .title(request.getTitle())
                .imageUrl(imageUrl)
                .build();

        return mapToResponse(repository.save(achievement));
    }

    // UPDATE with file upload
    public AchievementResponse update(Long id, AchievementRequest request, MultipartFile imageFile) throws IOException {
        Achievement achievement = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            achievement.setTitle(request.getTitle());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(imageFile, "achievements");
            achievement.setImageUrl(imageUrl);
        }

        return mapToResponse(repository.save(achievement));
    }


    public void deleteAchievement(Long id) {
        repository.deleteById(id);
    }

    private AchievementResponse mapToResponse(Achievement achievement) {
        return AchievementResponse.builder()
                .id(achievement.getId())
                .title(achievement.getTitle())
                .imageUrl(achievement.getImageUrl())
                .createdAt(achievement.getCreatedAt())
                .build();
    }
}