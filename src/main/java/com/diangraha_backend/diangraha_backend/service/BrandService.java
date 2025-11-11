package com.diangraha_backend.diangraha_backend.service;

import com.diangraha_backend.diangraha_backend.dto.BrandRequest;
import com.diangraha_backend.diangraha_backend.dto.BrandResponse;
import com.diangraha_backend.diangraha_backend.entity.Brand;
import com.diangraha_backend.diangraha_backend.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final FileStorageService fileStorageService;

    public BrandResponse create(BrandRequest request, MultipartFile logoFile) throws IOException {
        Brand brand = new Brand();
        brand.setName(request.getName());

        if (logoFile != null && !logoFile.isEmpty()) {
            String logoPath = fileStorageService.storeFile(logoFile, "brands");
            brand.setLogoUrl(logoPath);
        }

        Brand saved = brandRepository.save(brand);
        return mapToResponse(saved);
    }

    public List<BrandResponse> findAll() {
        return brandRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BrandResponse findById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        return mapToResponse(brand);
    }

    public BrandResponse update(Long id, BrandRequest request, MultipartFile logoFile) throws IOException {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        
        brand.setName(request.getName());
        
        if (logoFile != null && !logoFile.isEmpty()) {
            fileStorageService.deleteFileByUrl(brand.getLogoUrl());
            String logoPath = fileStorageService.storeFile(logoFile, "brands");
            brand.setLogoUrl(logoPath);
        }
        
        Brand updated = brandRepository.save(brand);
        return mapToResponse(updated);
    }

    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Brand not found");
        }
        Brand brand = brandRepository.findById(id).get();
        fileStorageService.deleteFileByUrl(brand.getLogoUrl());
        brandRepository.deleteById(id);
    }

    private BrandResponse mapToResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logoUrl(brand.getLogoUrl())
                .build();
    }
}
