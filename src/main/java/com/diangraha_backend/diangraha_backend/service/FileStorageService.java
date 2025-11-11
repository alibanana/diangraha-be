package com.diangraha_backend.diangraha_backend.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private byte[] compressImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(900, 600)
                .outputQuality(0.6)
                .outputFormat("jpg")
                .toOutputStream(baos);
        return baos.toByteArray();
    }

    public String storeFile(MultipartFile file, String subFolder, String oldImageUrl) throws IOException {
        if (file == null || file.isEmpty()) return oldImageUrl;

        byte[] compressedImage = compressImage(file);

        String fileName = subFolder + "/" + UUID.randomUUID() + ".jpg";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType("image/jpeg")
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(compressedImage));

        if (oldImageUrl != null && !oldImageUrl.isBlank()) {
            deleteFileByUrl(oldImageUrl);
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        return storeFile(file, subFolder, null);
    }

    public void deleteFileByUrl(String fileUrl) {
        String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
}
