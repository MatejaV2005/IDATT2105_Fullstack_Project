// package com.grimni.controller;

// import java.io.IOException;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.util.StringUtils;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.server.ResponseStatusException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import com.grimni.service.FileStorageService;

// @RestController
// @RequestMapping("/files")
// public class SimpleUploaderController {

//     private static final Logger logger = LoggerFactory.getLogger(SimpleUploaderController.class);

//     private final FileStorageService fileStorageService;
//     private final String bucket;

//     public SimpleUploaderController(
//         FileStorageService fileStorageService,
//         @Value("${s3.bucket}") String bucket
//     ) {
//         this.fileStorageService = fileStorageService;
//         this.bucket = bucket;
//     }

//     @PostMapping("/upload")
//     public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
//         if (file.isEmpty()) {
//             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
//         }

//         String originalFilename = file.getOriginalFilename() == null ? "unnamed" : StringUtils.cleanPath(file.getOriginalFilename());
//         String key = "sanity/" + UUID.randomUUID() + "-" + originalFilename;

//         try {
//             fileStorageService.upload(
//                 bucket,
//                 key,
//                 file.getInputStream(),
//                 file.getSize(),
//                 file.getContentType()
//             );
//         } catch (IOException exception) {
//             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read uploaded file", exception);
//         } catch (RuntimeException exception) {
//             logger.error("S3 upload failed for key {}", key, exception);
//             throw new ResponseStatusException(
//                 HttpStatus.BAD_GATEWAY,
//                 "S3 upload failed: " + exception.getMessage(),
//                 exception
//             );
//         }

//         return ResponseEntity.ok("Uploaded to bucket '" + bucket + "' with key: " + key);
//     }
// }
