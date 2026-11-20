package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.FileResponse;
import org.oplearn.project.exception.FileEmptyException;
import org.oplearn.project.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3StorageService {

  private final S3Client s3Client;

  @Value("${aws.s3.bucket-name:qrcode-media}")
  private String bucketName;

  @Value("${aws.s3.endpoint:}")
  private String endpoint;

  @Value("${server.port:8089}")
  private String serverPort;

  @Value("${app.public-api-url:}")
  private String publicApiUrl;

  public String uploadImage(MultipartFile file) {
    return this.uploadFile(file).getUrl();
  }

  public FileResponse uploadFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new FileEmptyException();
    }

    String extension = this.getFileExtension(file.getOriginalFilename());
    String fileName = UUID.randomUUID() + extension;

    // Nếu endpoint chưa được cấu hình trong .env -> lưu ngay cục bộ
    if (endpoint == null || endpoint.isBlank()) {
      return uploadToLocalStorage(file, fileName);
    }

    // Thử upload lên S3 nếu có endpoint hợp lệ
    try {
      this.createBucketIfNotExists();

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .contentType(file.getContentType())
        .build();

      try (InputStream inputStream = file.getInputStream()) {
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
      }

      log.info("(uploadFile) Upload file lên S3 thành công: {}", fileName);

      String cleanEndpoint = endpoint != null && endpoint.endsWith("/")
        ? endpoint.substring(0, endpoint.length() - 1)
        : endpoint;
      String fileUrl = String.format("%s/%s/%s", cleanEndpoint, bucketName, fileName);

      return FileResponse.builder()
        .url(fileUrl)
        .fileName(fileName)
        .size(file.getSize())
        .contentType(file.getContentType())
        .build();

    } catch (Exception ex) {
      log.warn("(uploadFile) S3 không khả dụng ({}), tự động lưu vào local storage: {}", ex.getMessage(), fileName);
      return uploadToLocalStorage(file, fileName);
    }
  }

  private FileResponse uploadToLocalStorage(MultipartFile file, String fileName) {
    try {
      Path uploadDir = Paths.get("uploads");
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }
      Path targetPath = uploadDir.resolve(fileName);
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
      }
      log.info("(uploadToLocalStorage) Đã lưu file cục bộ thành công: {}", targetPath.toAbsolutePath());

      String baseApi = (publicApiUrl != null && !publicApiUrl.isBlank())
          ? publicApiUrl.replaceAll("/+$", "")
          : ("http://localhost:" + serverPort);
      String localFileUrl = baseApi + "/api/v1/files/view/" + fileName;
      return FileResponse.builder()
        .url(localFileUrl)
        .fileName(fileName)
        .size(file.getSize())
        .contentType(file.getContentType())
        .build();
    } catch (Exception e) {
      log.error("(uploadToLocalStorage) Lỗi khi lưu file cục bộ", e);
      throw new FileUploadException(e.getMessage());
    }
  }

  public void deleteImage(String fileName) {
    this.deleteFile(fileName);
  }

  public void clearAllBucketFiles() {
    try {
      Path uploadDir = Paths.get("uploads");
      if (Files.exists(uploadDir)) {
        try (var stream = Files.list(uploadDir)) {
          stream.forEach(p -> {
            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
          });
        }
      }
    } catch (Exception ignored) {
    }

    try {
      ListObjectsV2Request listRequest = ListObjectsV2Request.builder().bucket(bucketName).build();
      ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
      for (S3Object s3Object : listResponse.contents()) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(s3Object.key()).build());
        log.info("(clearAllBucketFiles) Đã xóa file trên RustFS: {}", s3Object.key());
      }
      log.info("(clearAllBucketFiles) Đã dọn sạch toàn bộ file trong bucket: {}", bucketName);
    } catch (Exception ex) {
      log.warn("(clearAllBucketFiles) Lỗi khi xóa file trên RustFS: {}", ex.getMessage());
    }
  }

  public void deleteFile(String fileName) {
    try {
      Path localFile = Paths.get("uploads").resolve(fileName);
      if (Files.exists(localFile)) {
        Files.delete(localFile);
        log.info("(deleteFile) Đã xóa file cục bộ: {}", fileName);
      }
    } catch (Exception ignored) {
    }

    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build();

      s3Client.deleteObject(deleteObjectRequest);
      log.info("(deleteFile) Xóa file thành công khỏi S3: {}", fileName);
    } catch (Exception ex) {
      log.warn("(deleteFile) Bỏ qua lỗi S3 khi xóa: {}", ex.getMessage());
    }
  }

  public List<FileResponse> uploadMultipleFiles(MultipartFile[] files) {
    List<FileResponse> responses = new ArrayList<>();
    for (MultipartFile file : files) {
      if (!file.isEmpty()) {
        responses.add(this.uploadFile(file)); // Gọi lại hàm upload lẻ từng file
      }
    }
    return responses;
  }

  private void createBucketIfNotExists() {
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
    } catch (Exception e) {
      log.warn("(createBucketIfNotExists) HeadBucket không thành công (hoặc bucket chưa tồn tại): {}", e.getMessage());
      try {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        log.info("(createBucket) Đã tạo mới Bucket: {}", bucketName);
      } catch (Exception ex) {
        log.warn("(createBucket) Không thể tạo mới bucket (có thể bucket đã tồn tại): {}", ex.getMessage());
      }

      try {
        String publicPolicy = """
              {
                "Version": "2012-10-17",
                "Statement": [
                  {
                    "Effect": "Allow",
                    "Principal": "*",
                    "Action": ["s3:GetObject"],
                    "Resource": ["arn:aws:s3:::%s/*"]
                  }
                ]
              }
              """.formatted(bucketName);

        s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
          .bucket(bucketName)
          .policy(publicPolicy)
          .build());
        log.info("(createBucket) Đã cấu hình Public Access Policy cho Bucket: {}", bucketName);
      } catch (Exception ex) {
        log.warn("(createBucket) Bỏ qua putBucketPolicy (storage provider không hỗ trợ hoặc quyền hạn chế): {}", ex.getMessage());
      }
    }
  }

  private String getFileExtension(String fileName) {
    if (fileName != null && fileName.contains(".")) {
      return fileName.substring(fileName.lastIndexOf("."));
    }
    return ".jpg";
  }
}

