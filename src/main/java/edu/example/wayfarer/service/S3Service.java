package edu.example.wayfarer.service;


import edu.example.wayfarer.config.s3.AwsS3Config;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final AwsS3Config awsS3Config;

    public String upload(String email, MultipartFile file) {
        String fileName = "profiles/" + email + "/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        try {

            Tika tika = new Tika();
            String contentType = tika.detect(file.getInputStream());

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(awsS3Config.getBucketName())
                            .key(fileName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    awsS3Config.getBucketName(),
                    awsS3Config.getRegion(),
                    fileName);
        }catch (IOException e) {
            throw new RuntimeException("Could not upload file " + fileName, e);
        }
    }

    public void deleteFileFromS3(String fileUrl){
        String fileKey = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(awsS3Config.getBucketName())
                        .key(fileKey)
                        .build()
        );
    }
}
