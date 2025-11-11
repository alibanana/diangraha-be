package com.diangraha_backend.diangraha_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultipartImageDto implements MultipartFile {

    private byte[] bytes;
    private String name;
    private String originalFilename;
    private String contentType;
    private boolean isEmpty;
    private long size;
    private InputStream inputStream;

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        // TODO Auto-generated method stub
    }
}
