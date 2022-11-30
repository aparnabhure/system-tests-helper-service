package com.example.systemtestshelper.domains;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class AwsConfig {
    @Value("${aws.access.key:}")
    private String accessKey = "";
    @Value("${aws.secret.key:}")
    private String secretKey="";
    @Value("${aws.bucket.key:}")
    private String bucketName="";
}
