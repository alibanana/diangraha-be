package com.diangraha_backend.diangraha_backend.util;

import com.diangraha_backend.diangraha_backend.dto.MultipartImageDto;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ImageUtil {

  static final double BYTE_SIZE_MULTIPLIER = 0.00095367432;
  static final float MAX_IMAGE_COMPRESSION_SIZE_IN_KB = 1024f;

  public MultipartFile compressImage(MultipartFile file) throws IOException {
    if (MAX_IMAGE_COMPRESSION_SIZE_IN_KB < file.getSize() * BYTE_SIZE_MULTIPLIER) {
      BufferedImage originalBufferedImage = ImageIO.read(file.getInputStream());
      BufferedImage resultBufferedImage = Scalr.resize(originalBufferedImage,
          Math.round(MAX_IMAGE_COMPRESSION_SIZE_IN_KB));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(resultBufferedImage,
          FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase(), baos);
      baos.flush();

      byte[] resultBytes = baos.toByteArray();
      float kbSize = resultBytes.length / 1024f;
      double finalFileSize = (kbSize / BYTE_SIZE_MULTIPLIER);

      MultipartFile finalFile = MultipartImageDto.builder()
          .bytes(resultBytes)
          .name(file.getName())
          .originalFilename(file.getOriginalFilename())
          .contentType(file.getContentType())
          .isEmpty(file.isEmpty())
          .size((long) finalFileSize)
          .inputStream(new ByteArrayInputStream(resultBytes))
          .build();

      return compressImage(finalFile);
    }
    return file;
  }
}
