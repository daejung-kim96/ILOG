package ilog.back.diary.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    public String uploadFile(MultipartFile file) throws IOException;
}
