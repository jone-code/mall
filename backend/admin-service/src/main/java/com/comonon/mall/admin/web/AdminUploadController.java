package com.comonon.mall.admin.web;

import com.comonon.mall.admin.security.RequirePermission;
import com.comonon.mall.admin.service.LocalFileStorageService;
import com.comonon.mall.common.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUploadController {

    private final LocalFileStorageService fileStorageService;

    @PostMapping("/upload")
    @RequirePermission("product:write")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.store(file);
        return Result.ok(Map.of("url", url));
    }
}
