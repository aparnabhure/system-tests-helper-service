package com.example.systemtestshelper.domains;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class AwsConfig {
    @Value("${aws.access.key:AKIAURBCSD2XA2S36YFQ}")
    private String accessKey = "AKIAURBCSD2XA2S36YFQ";
    @Value("${aws.secret.key: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}")
    private String secretKey="EjqJJA7OYPiE01NzhRCR8Akul3ujw6ZGwzOSB3jT";
    @Value("${aws.bucket.key:apdemosttest1}")
    private String bucketName="apdemosttest1";
}
