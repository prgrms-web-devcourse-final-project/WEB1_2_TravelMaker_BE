package edu.example.wayfarer.config.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws.s3")
@Getter
@Setter
public class AwsS3Config {

    private String bucketName;
    private String region;
    private String accessKey;
    private String secretAccessKey;
}
