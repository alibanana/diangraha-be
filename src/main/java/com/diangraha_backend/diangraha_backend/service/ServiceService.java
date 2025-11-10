package com.diangraha_backend.diangraha_backend.service;

import com.diangraha_backend.diangraha_backend.dto.*;
import com.diangraha_backend.diangraha_backend.entity.ServiceEntity;
import com.diangraha_backend.diangraha_backend.entity.ServiceFeature;
import com.diangraha_backend.diangraha_backend.entity.SubService;
import com.diangraha_backend.diangraha_backend.entity.SubServiceWork;
import com.diangraha_backend.diangraha_backend.repository.ServiceFeatureRepository;
import com.diangraha_backend.diangraha_backend.repository.ServiceRepository;
import com.diangraha_backend.diangraha_backend.repository.SubServiceRepository;
import com.diangraha_backend.diangraha_backend.repository.SubServiceWorkRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceFeatureRepository featureRepository;
    private final SubServiceRepository subServiceRepository;
    private final SubServiceWorkRepository subServiceWorkRepository;
    private final FileStorageService fileStorageService;

    // CREATE Service
    public ServiceResponse createService(ServiceRequest request) throws IOException {
        ServiceEntity service = new ServiceEntity();
        service.setName(request.getName());
        service.setShortDesc(request.getShortDesc());
        service.setLongDesc(request.getLongDesc());

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String imagePath = fileStorageService.storeFile(request.getImageFile(), "services");
            service.setImageUrl(imagePath);
        }

        ServiceEntity saved = serviceRepository.save(service);
        return mapToResponse(saved);
    }

    // ADD Feature
    public ServiceFeatureResponse addFeature(Long serviceId, ServiceFeatureRequest request) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        ServiceFeature feature = new ServiceFeature();
        feature.setFeatureName(request.getFeatureName());
        feature.setFeatureDesc(request.getFeatureDesc());
        feature.setService(service);

        ServiceFeature saved = featureRepository.save(feature);

        return new ServiceFeatureResponse(
                saved.getId(),
                saved.getFeatureName(),
                saved.getFeatureDesc()
        );
    }

    public SubServiceResponse addSubService(Long serviceId, SubServiceRequest request) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        SubService subService = new SubService();
        subService.setName(request.getName());
        subService.setDescription(request.getDescription());
        subService.setService(service);

        SubService saved = subServiceRepository.save(subService);

        // Mapping dari entity Work ke DTO SubServiceWorkResponse
        List<SubServiceWorkResponse> workResponses = saved.getWorks() != null
                ? saved.getWorks().stream()
                .map(work -> new SubServiceWorkResponse(
                        work.getId(),
                        work.getDescription()
                ))
                .toList()
                : List.of();

        return new SubServiceResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                workResponses
        );
    }

    public SubServiceWorkResponse addSubServiceWork(Long subServiceId, SubServiceWorkRequest request){
        SubService subService = subServiceRepository.findById(subServiceId)
                .orElseThrow(() -> new RuntimeException("Work of Sub Services not found"));
        SubServiceWork subServiceWork = new SubServiceWork();
        subServiceWork.setDescription(request.getDescription());
        subServiceWork.setSubService(subService);

        SubServiceWork save = subServiceWorkRepository.save(subServiceWork);

        return  new SubServiceWorkResponse(
                save.getId(),
                save.getDescription()
        );
    }

    @Transactional
    public List<ServiceResponse> getAllServices() {
        List<ServiceEntity> services = serviceRepository.findAllWithFeatures();
        List<ServiceEntity> servicesWithSubs = serviceRepository.findAllWithSubServices();
        List<SubService> subServicesWithWorks = serviceRepository.findAllSubServicesWithWorks();
        Map<Long, List<SubService>> subServiceMap = subServicesWithWorks.stream()
                .collect(Collectors.groupingBy(SubService::getId));

        Map<Long, ServiceEntity> serviceMap = new HashMap<>();
        for (ServiceEntity s : services) {
            serviceMap.put(s.getId(), s);
        }

        for (ServiceEntity s : servicesWithSubs) {
            ServiceEntity existing = serviceMap.get(s.getId());
            if (existing != null) {
                existing.setSubServices(s.getSubServices());
                if (existing.getSubServices() != null) {
                    for (SubService sub : existing.getSubServices()) {
                        List<SubService> subs = subServiceMap.get(sub.getId());
                        if (subs != null && !subs.isEmpty()) {
                            sub.setWorks(subs.get(0).getWorks());
                        }
                    }
                }
            }
        }


        return serviceMap.values().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ServiceListResponse> getAllServicesLite() {
        return serviceRepository.findAll().stream()
                .map(s -> new ServiceListResponse(
                        s.getId(),
                        s.getName(),
                        s.getLongDesc(),
                        s.getImageUrl()
                ))
                .toList();
    }

    // GET BY ID
    public ServiceResponse getById(Long id) {

        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        List<ServiceEntity> serviceWithFeatures = serviceRepository.findAllWithFeatures();
        List<ServiceEntity> serviceWithSubs = serviceRepository.findAllWithSubServices();
        List<SubService> subServicesWithWorks = serviceRepository.findAllSubServicesWithWorks();

        ServiceEntity target = serviceWithFeatures.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(service);

        Optional<ServiceEntity> subs = serviceWithSubs.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();

        if (subs.isPresent()) {
            target.setSubServices(subs.get().getSubServices());
        }

        if (target.getSubServices() != null) {
            Map<Long, List<SubService>> subServiceMap = subServicesWithWorks.stream()
                    .collect(Collectors.groupingBy(SubService::getId));

            for (SubService sub : target.getSubServices()) {
                List<SubService> found = subServiceMap.get(sub.getId());
                if (found != null && !found.isEmpty()) {
                    sub.setWorks(found.get(0).getWorks());
                }
            }
        }

        return mapToResponse(target);
    }


    // Get By ID for Sub Services

    @Transactional(readOnly = true)
    public Optional<SubServiceResponse> getSubServiceById(Long id) {
        return subServiceRepository.findById(id)
                .map(this::mapToDto);
    }

    private SubServiceResponse mapToDto(SubService entity) {
        // pastikan works diinisialisasi (karena LAZY), @Transactional pada method menjamin session aktif
        var works = (entity.getWorks() == null) ? java.util.List.<SubServiceWorkResponse>of()
                : entity.getWorks().stream()
                .map(this::mapWorkToDto)
                .collect(Collectors.toList());

        return SubServiceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .works(works)
                .build();
    }

    private SubServiceWorkResponse mapWorkToDto(SubServiceWork work) {
        return SubServiceWorkResponse.builder()
                .id(work.getId())
                .description(work.getDescription())
                .build();
    }

    // Get by id work
    public SubServiceWorkResponse getWorkById(Long id) {
        SubServiceWork subServiceWork = subServiceWorkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        return mapToResponseWork(subServiceWork);
    }

    private SubServiceWorkResponse mapToResponseWork(SubServiceWork work) {
        return SubServiceWorkResponse.builder()
                .id(work.getId())
                .description(work.getDescription())
                .build();
    }
   // @Transactional(readOnly = true)

    // UPDATE Service
    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) throws IOException {
        // 🔹 Ambil data service dari DB
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // 🔹 Update field utama (tidak sentuh relasi anak)
        service.setName(request.getName());
        service.setShortDesc(request.getShortDesc());
        service.setLongDesc(request.getLongDesc());

        // Update image hanya jika user upload file baru
        MultipartFile imageFile = request.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.storeFile(imageFile, "services");
            service.setImageUrl(imagePath);
        }

        // Simpan perubahan parent saja
        ServiceEntity updated = serviceRepository.save(service);

        // Force load relasi anak agar tidak LazyInitializationException
        // (biar mapToResponse() bisa akses tanpa error)
        if (updated.getFeatures() != null) updated.getFeatures().size();
        if (updated.getSubServices() != null) {
            updated.getSubServices().forEach(sub -> {
                if (sub.getWorks() != null) sub.getWorks().size();
            });
        }

        //  Kembalikan response aman
        return mapToResponse(updated);
    }

    // DELETE Service
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new RuntimeException("Service not found");
        }
        serviceRepository.deleteById(id);
    }

    // UPDATE Feature
    public ServiceFeatureResponse updateFeature(Long serviceId, Long featureId, ServiceFeatureRequest request) {
        ServiceFeature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new RuntimeException("Feature not found"));

        if (!feature.getService().getId().equals(serviceId)) {
            throw new RuntimeException("Feature does not belong to this service");
        }

        feature.setFeatureName(request.getFeatureName());
        feature.setFeatureDesc(request.getFeatureDesc());

        ServiceFeature updated = featureRepository.save(feature);

        return new ServiceFeatureResponse(
                updated.getId(),
                updated.getFeatureName(),
                updated.getFeatureDesc()
        );
    }


    public SubServiceResponse updateSubService(Long serviceId, Long subServiceId, SubServiceRequest request) {
        // Cari sub-service berdasarkan ID
        SubService subService = subServiceRepository.findById(subServiceId)
                .orElseThrow(() -> new RuntimeException("Sub Service Not Found"));

        // Pastikan sub-service memang milik service yang sesuai
        if (!subService.getService().getId().equals(serviceId)) {
            throw new RuntimeException("Sub Service does not belong to this service");
        }

        // Update field dasar
        subService.setName(request.getName());
        subService.setDescription(request.getDescription());

        // Simpan perubahan
        SubService updated = subServiceRepository.save(subService);

        // Mapping dari entity Work ke DTO SubServiceWorkResponse
        List<SubServiceWorkResponse> workResponses = updated.getWorks() != null
                ? updated.getWorks().stream()
                .map(work -> new SubServiceWorkResponse(
                        work.getId(),
                        work.getDescription()
                ))
                .toList()
                : List.of();

        // Return DTO response
        return new SubServiceResponse(
                updated.getId(),
                updated.getName(),
                updated.getDescription(),
                workResponses
        );
    }

    // Update service
    public SubServiceWorkResponse updateSubServiceWork(
            Long subServiceId,
            Long subServiceWorkId,
            SubServiceWorkRequest request
    ) {
        // Ambil data dari database
        SubServiceWork subServiceWork = subServiceWorkRepository.findById(subServiceWorkId)
                .orElseThrow(() -> new RuntimeException("Sub Service Work not found"));

        // Validasi relasi subService
        if (!subServiceWork.getSubService().getId().equals(subServiceId)) {
            throw new RuntimeException("List work does not belong to this service");
        }

        // Update field dari request
        subServiceWork.setDescription(request.getDescription());

        // Simpan perubahan ke database
        SubServiceWork updated = subServiceWorkRepository.save(subServiceWork);

        // Kembalikan response dengan data terbaru
        return new SubServiceWorkResponse(
                updated.getId(),
                updated.getDescription()
        );
    }


    // DELETE Feature
    public void deleteFeature(Long serviceId, Long featureId) {
        ServiceFeature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new RuntimeException("Feature not found"));

        if (!feature.getService().getId().equals(serviceId)) {
            throw new RuntimeException("Feature does not belong to this service");
        }

        featureRepository.deleteById(featureId);
    }

    // Delete Sub Service
    public void deleteSubService(Long serviceId, Long subServiceId){
        SubService subService = subServiceRepository.findById(subServiceId)
                .orElseThrow(() -> new RuntimeException("Sub Service not Found"));
        if (!subService.getService().getId().equals(serviceId)){
            throw new RuntimeException("Sub service nto belong to this services");
        }

        subServiceRepository.deleteById(subServiceId);
    }

    public void deleteSubServiceWork(Long subServiceId, Long subServiceWorkId){
        SubServiceWork subServiceWork = subServiceWorkRepository.findById(subServiceWorkId)
                .orElseThrow(() -> new RuntimeException("Sub Service not Found"));
        if (!subServiceWork.getSubService().getId().equals(subServiceId)){
            throw new RuntimeException("Sub service nto belong to this services");
        }

        subServiceWorkRepository.deleteById(subServiceWorkId);
    }
    private ServiceResponse mapToResponse(ServiceEntity service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .shortDesc(service.getShortDesc())
                .longDesc(service.getLongDesc())
                .imageUrl(service.getImageUrl())
                .features(service.getFeatures() != null ?
                        service.getFeatures().stream()
                                .map(f -> new ServiceFeatureResponse(f.getId(), f.getFeatureName(), f.getFeatureDesc()))
                                .toList()
                        : List.of())
                .subServices(service.getSubServices() != null ?
                        service.getSubServices().stream()
                                .map(s -> SubServiceResponse.builder()
                                        .id(s.getId())
                                        .name(s.getName())
                                        .description(s.getDescription())
                                        .works(s.getWorks() != null ?
                                                s.getWorks().stream()
                                                        .map(w -> new SubServiceWorkResponse(w.getId(), w.getDescription()))
                                                        .toList()
                                                : List.of())
                                        .build())
                                .toList()
                        : List.of())
                .createdAt(service.getCreatedAt())
                .build();
    }

}
