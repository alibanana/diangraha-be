package com.diangraha_backend.diangraha_backend.controller;

import com.diangraha_backend.diangraha_backend.dto.*;
import com.diangraha_backend.diangraha_backend.entity.SubServiceWork;
import com.diangraha_backend.diangraha_backend.service.ServiceService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/services")


public class ServiceController {

    private final ServiceService serviceService;
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }
    // Post for services
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ServiceResponse> createService(
            @RequestParam("name") String name,
            @RequestParam("shortDesc") String shortDesc,
            @RequestParam("longDesc") String longDesc,
            @RequestParam(value = "imageFile") MultipartFile imageFile
    ) throws IOException {
        ServiceRequest request = new ServiceRequest(name, shortDesc, longDesc, imageFile);
        return ResponseEntity.ok(serviceService.createService(request));
    }

    // Post for features
    @PostMapping("/{id}/features")
    public ResponseEntity<ServiceFeatureResponse> addFeature(
            @PathVariable Long id,
            @RequestBody ServiceFeatureRequest request
    ) {
        return ResponseEntity.ok(serviceService.addFeature(id, request));
    }
    @PostMapping("{id}/sub-services")
    public  ResponseEntity<SubServiceResponse> addSubservice (
            @PathVariable Long id,
            @RequestBody  SubServiceRequest request
    ){
        return  ResponseEntity.ok(serviceService.addSubService(id, request));
    }
    @PostMapping("sub-services/{subServiceId}/works")
    public  ResponseEntity<SubServiceWorkResponse> addSubServiceWork(
            @PathVariable Long subServiceId,
            @RequestBody SubServiceWorkRequest request
    ){
        return ResponseEntity.ok(serviceService.addSubServiceWork(subServiceId, request));
    }


    @GetMapping("/lite")
    public ResponseEntity<List<ServiceListResponse>> getLiteServices() {
        return ResponseEntity.ok(serviceService.getAllServicesLite());
    }

    @GetMapping("/menu")
    public ResponseEntity<List<ServiceMenuResponse>> getServiceMenuLite() {
        return ResponseEntity.ok(serviceService.getAllServicesMenu());
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getById(id));
    }

    @GetMapping("/sub-services/{id}")
    public ResponseEntity<SubServiceResponse> getSubServiceById(@PathVariable Long id) {
        return serviceService.getSubServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sub-services/works/{id}")
    public ResponseEntity<SubServiceWorkResponse> getWorkById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getWorkById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("shortDesc") String shortDesc,
            @RequestParam("longDesc") String longDesc,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        ServiceRequest request = new ServiceRequest();
        request.setName(name);
        request.setShortDesc(shortDesc);
        request.setLongDesc(longDesc);
        request.setImageFile(imageFile);

        ServiceResponse response = serviceService.updateService(id, request);
        return ResponseEntity.ok(response);
    }



    @PutMapping("/{serviceId}/features/{featureId}")
    public ResponseEntity<ServiceFeatureResponse> updateFeature(
            @PathVariable Long serviceId,
            @PathVariable Long featureId,
            @RequestBody ServiceFeatureRequest request
    ) {
        return ResponseEntity.ok(serviceService.updateFeature(serviceId, featureId, request));
    }

    @PutMapping("/{serviceId}/sub-services/{subServiceId}")
    public  ResponseEntity<SubServiceResponse> updateSubService(
            @PathVariable Long serviceId,
            @PathVariable Long subServiceId,
            @RequestBody SubServiceRequest request
    )
        {
            return  ResponseEntity.ok(serviceService.updateSubService(serviceId, subServiceId, request));
        }

    // Edit work

    @PutMapping("/sub-services/{subServiceId}/works/{subServiceWorkId}")
    public ResponseEntity<SubServiceWorkResponse> updateSubServicesWork(
            @PathVariable Long subServiceId,
            @PathVariable Long subServiceWorkId,
            @RequestBody SubServiceWorkRequest request
    ) {
        return ResponseEntity.ok(serviceService.updateSubServiceWork(subServiceId, subServiceWorkId, request));
    }


    // Delete service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    // Deleted feature
    @DeleteMapping("/{serviceId}/features/{featureId}")
    public ResponseEntity<Void> deleteFeature(
            @PathVariable Long serviceId,
            @PathVariable Long featureId
    ) {
        serviceService.deleteFeature(serviceId, featureId);
        return ResponseEntity.noContent().build();
    }
    // Delete sub service
    @DeleteMapping("/{serviceId}/sub-services/{subServiceId}")
    public ResponseEntity<Void> deleteSubService(
            @PathVariable Long serviceId,
            @PathVariable Long subServiceId
    ) {
        serviceService.deleteSubService(serviceId, subServiceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sub-services/{subServiceId}/works/{subServiceWorkId}")
    public ResponseEntity<Void> deleteSubServiceWork(
            @PathVariable Long subServiceId,
            @PathVariable Long subServiceWorkId
    ) {
        serviceService.deleteSubServiceWork(subServiceId, subServiceWorkId);
        return ResponseEntity.noContent().build();
    }
}
