package com.diangraha_backend.diangraha_backend.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class FileStorageService {

    /**
     * Compress image, convert to Base64, return Base64 string with MIME type prefix.
     */
    public String storeFile(MultipartFile file, String subFolder) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String mimeType = file.getContentType();
        if (mimeType == null) mimeType = "image/jpeg";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails
                .of(file.getInputStream())
                .size(900, 600)            // compress resolution (ubah sesuai kebutuhan)
                .outputQuality(0.6)        // compress quality: 0.0 (kurang) - 1.0 (full quality)
                .outputFormat("jpg")       // pastikan konsisten ke JPG agar lebih kecil
                .toOutputStream(outputStream);

        byte[] compressedBytes = outputStream.toByteArray();

        String base64 = Base64.getEncoder().encodeToString(compressedBytes);

        return "data:" + mimeType + ";base64," + base64;
    }
}
