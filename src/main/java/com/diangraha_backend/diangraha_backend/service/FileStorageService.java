package com.diangraha_backend.diangraha_backend.service;

import com.diangraha_backend.diangraha_backend.dto.MultipartImageDto;
import com.diangraha_backend.diangraha_backend.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final S3Client s3Client;

    @Autowired
    private ImageUtil imageUtil;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String storeFile(MultipartFile file, String subFolder, String oldImageUrl) throws IOException {
        if (file == null || file.isEmpty()) return oldImageUrl;

        MultipartFile compressedImage = imageUtil.compressImage(file);

        String fileName = subFolder + "/" + UUID.randomUUID() + "." +
                FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(compressedImage.getContentType())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(compressedImage.getBytes()));

        if (oldImageUrl != null && !oldImageUrl.isBlank()) {
            deleteFileByUrl(oldImageUrl);
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        return storeFile(file, subFolder, null);
    }

    public void deleteFileByUrl(String fileUrl) {
        try {
            String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (NullPointerException e) {
            log.error("Error deleting file as it does not exists");
        }
    }
}
