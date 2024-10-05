package org.zerock.wantuproject.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class FileController {

    private final Path rootLocation = Paths.get("uploads"); // 파일이 저장될 로컬 경로

    // 생성자에서 폴더 생성
    public FileController() throws IOException {
        Files.createDirectories(rootLocation);
    }

    // 여러 개의 파일 업로드 처리
    @PostMapping("/uploadMultiple")
    public ResponseEntity<List<String>> handleMultipleFileUpload(@RequestParam("files") MultipartFile[] files) {
        List<String> uploadedFileNames = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String savedFileName = saveFile(file); // 파일 저장
                uploadedFileNames.add(savedFileName);  // 저장된 파일 이름 추가
            } catch (IOException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }
        return ResponseEntity.ok(uploadedFileNames); // 저장된 파일들의 이름 반환
    }

    // 파일 저장 메서드
    private String saveFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename(); // 고유한 파일 이름 생성
        Files.copy(file.getInputStream(), rootLocation.resolve(fileName)); // 파일 저장
        return fileName; // 저장된 파일 이름 반환
    }

    // 파일 불러오기 처리
    @GetMapping("/files")
    public ResponseEntity<Resource> serveFile(@RequestParam String fileName) {
        try {
            Path file = rootLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") // MIME 타입을 이미지로 설정
                        .body(resource); // 파일 반환
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}