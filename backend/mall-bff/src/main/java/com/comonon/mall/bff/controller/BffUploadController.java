package com.comonon.mall.bff.controller;

import com.comonon.mall.bff.service.LocalFileStorageService;
import com.comonon.mall.common.api.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BffUploadController {

    private final LocalFileStorageService fileStorageService;

    @PostMapping("/api/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.store(file);
        return Result.ok(Map.of("url", url));
    }
}
