//package com.tihuz.application_service.controller;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/files")
//@RequiredArgsConstructor
//public class FileController {
//
//    private final Cloudinary cloudinary;
//
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadFile(
//            @RequestParam("file") MultipartFile file
//    ) throws IOException {
//
//        String originalFilename = file.getOriginalFilename();
//
//        String filename = "file";
//        String extension = ".pdf";
//
//        // lấy tên file + extension
//        if (originalFilename != null && originalFilename.contains(".")) {
//
//            filename = originalFilename.substring(
//                    0,
//                    originalFilename.lastIndexOf(".")
//            );
//
//            extension = originalFilename.substring(
//                    originalFilename.lastIndexOf(".")
//            );
//        }
//
//        // unique filename
//        String uniqueFilename =
//                filename + "_" + System.currentTimeMillis() + extension;
//
//        Map uploadResult = cloudinary.uploader().upload(
//                file.getBytes(),
//                ObjectUtils.asMap(
//                        "resource_type", "raw",
//
//                        // folder thật trên dashboard
//                        "asset_folder", "recruitment_cv",
//
//                        // tên file
//                        "public_id", uniqueFilename,
//
//                        "overwrite", false
//                )
//        );
//
//        return ResponseEntity.ok(uploadResult);
//    }
//}