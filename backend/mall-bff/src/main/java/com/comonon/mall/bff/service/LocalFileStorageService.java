package com.comonon.mall.bff.service;

import com.comonon.mall.common.web.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    @Value("${mall.upload.dir:./data/uploads}")
    private String uploadDir;

    @Value("${mall.upload.public-prefix:/files}")
    private String publicPrefix;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40000, "请选择文件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException(40000, "文件不能超过 5MB");
        }
        String ext = resolveExt(file);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(40000, "仅支持 jpg/png/gif/webp 图片");
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            String prefix = publicPrefix.endsWith("/")
                    ? publicPrefix.substring(0, publicPrefix.length() - 1)
                    : publicPrefix;
            return prefix + "/" + filename;
        } catch (IOException e) {
            log.error("upload failed", e);
            throw new BusinessException(50000, "文件保存失败");
        }
    }

    public Path resolveRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private static String resolveExt(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (StringUtils.hasText(original) && original.contains(".")) {
            return original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        String contentType = file.getContentType();
        if (contentType != null) {
            return switch (contentType.toLowerCase(Locale.ROOT)) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/gif" -> "gif";
                case "image/webp" -> "webp";
                default -> "";
            };
        }
        return "";
    }
}
