package edu.example.wayfarer.config.s3;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class S3Config {
    private final AwsS3Config awsS3Config;

    @Bean
    public S3Client s3Client(){
        // AWS 자격 증명 생성
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                awsS3Config.getAccessKey(), awsS3Config.getSecretAccessKey()
        );

        // S3 클라이언트 생성
        return S3Client.builder()
                .region(Region.of(awsS3Config.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
