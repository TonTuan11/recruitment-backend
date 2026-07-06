package com.tihuz.media_service.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${app.media-url}")
    private String mediaUrl;

    // Upload ảnh (avatar, logo)
    public String uploadImage(MultipartFile file, String folder) {
        try {
            log.info("Uploading image to folder: {}", folder);

            if (file.isEmpty()) {
                throw new RuntimeException("File rỗng");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Chỉ chấp nhận file ảnh");
            }

            if (file.getSize() > 2 * 1024 * 1024) {
                throw new RuntimeException("Ảnh không được quá 2MB");
            }

            return uploadToMinio(file, folder);

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());
            throw new RuntimeException("Upload thất bại: " + e.getMessage());
        }
    }

    // Upload CV (file PDF)
    public String uploadCV(MultipartFile file, String folder) {
        try {
            log.info("Uploading CV to folder: {}", folder);

            if (file.isEmpty()) {
                throw new RuntimeException("File rỗng");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                throw new RuntimeException("Chỉ chấp nhận file PDF");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("File CV không được quá 5MB");
            }

            return uploadToMinio(file, folder);

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());
            throw new RuntimeException("Upload thất bại: " + e.getMessage());
        }
    }

    // Method chung upload lên MinIO
    private String uploadToMinio(MultipartFile file, String folder) {
        try {
            // Tạo bucket nếu chưa có
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Created bucket: {}", bucketName);
            }

            // Tạo tên file unique
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = folder + "/" + UUID.randomUUID() + extension;

            // Upload file
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // Trả về URL
            String url = mediaUrl +"/" + bucketName + "/" + fileName;
            log.info("File uploaded successfully: {}", url);

            return url;

        } catch (Exception e) {
            log.error("Upload to MinIO failed: {}", e.getMessage());
            throw new RuntimeException("Upload thất bại: " + e.getMessage());
        }
    }
}