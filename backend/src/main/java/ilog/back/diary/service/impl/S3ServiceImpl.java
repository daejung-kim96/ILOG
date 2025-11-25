package ilog.back.diary.service.impl;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import ilog.back.diary.service.S3Service;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.public-endpoint}")
    private String publicEndpoint;

    public String uploadFile(MultipartFile file) throws IOException {
        // 1. 파일 이름 중복 방지 (UUID 사용)
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 2. 업로드 요청 객체 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType()) // 파일 타입(image/png 등) 자동 설정
                .build();

        // 3. S3(MinIO)로 업로드
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 4. 업로드된 파일의 URL 반환 (브라우저에서 바로 볼 수 있는 주소)
        String internalUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build()).toExternalForm();

        // 5. 내부 엔드포인트를 외부 접속 가능한 엔드포인트로 교체
        return internalUrl.replace(endpoint, publicEndpoint);
    }
}
