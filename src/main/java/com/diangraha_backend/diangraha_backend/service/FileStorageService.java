package com.diangraha_backend.diangraha_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class FileStorageService {

    /**
     * Konversi file upload menjadi Base64 tanpa menyimpannya ke folder.
     * @param file file yang diupload dari API (MultipartFile)
     * @param subFolder tidak dipakai lagi (hanya untuk kompatibilitas)
     * @return string Base64 lengkap dengan prefix MIME (data:image/png;base64,....)
     */
    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Ambil byte array dari file
        byte[] bytes = file.getBytes();

        // Dapatkan tipe MIME file (contoh: image/png)
        String mimeType = file.getContentType();
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        // Encode ke Base64
        String base64 = Base64.getEncoder().encodeToString(bytes);

        // Return dengan prefix MIME biar bisa langsung dipakai di <img src="...">
        return "data:" + mimeType + ";base64," + base64;
    }
}
