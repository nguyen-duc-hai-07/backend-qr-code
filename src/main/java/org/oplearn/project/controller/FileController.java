package org.oplearn.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.FileResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.service.impl.S3StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.CREATED_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@RequestMapping("/api/v1/files")
@Slf4j
@RequiredArgsConstructor
public class FileController {

  private final S3StorageService s3StorageService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
    log.info("(uploadFile) filename: {}, size: {}", file.getOriginalFilename(), file.getSize());
    FileResponse fileResponse = s3StorageService.uploadFile(file);
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, fileResponse);
  }

  @GetMapping("/view/{fileName}")
  public ResponseEntity<Resource> viewFile(@PathVariable String fileName) {
    try {
      Path filePath = Paths.get("uploads").resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists() && resource.isReadable()) {
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
          contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
      }
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("(viewFile) Error reading local file: {}", fileName, e);
      return ResponseEntity.internalServerError().build();
    }
  }


  @DeleteMapping
  public ResponseGeneral<Void> deleteFile(@RequestParam("file_name") String fileName) {
    log.info("(deleteFile) fileName: {}", fileName);
    s3StorageService.deleteImage(fileName);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @DeleteMapping("/clear-all")
  public ResponseGeneral<Void> clearAllFiles() {
    log.info("(clearAllFiles) Xóa sạch toàn bộ file trong storage bucket");
    s3StorageService.clearAllBucketFiles();
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }


  @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<List<FileResponse>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
    log.info("(uploadMultipleFiles) số lượng files: {}", files.length);
    List<FileResponse> fileResponses = s3StorageService.uploadMultipleFiles(files);
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, fileResponses);
  }
}

