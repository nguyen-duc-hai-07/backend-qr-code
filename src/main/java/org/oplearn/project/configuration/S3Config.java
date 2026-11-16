package org.oplearn.project.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

  @Value("${aws.s3.endpoint:}")
  private String endpoint;

  @Value("${aws.s3.region:us-east-1}")
  private String region;

  @Value("${aws.s3.access-key:}")
  private String accessKey;

  @Value("${aws.s3.secret-key:}")
  private String secretKey;

  @Bean
  public S3Client s3Client() {
    String resolvedEndpoint = (endpoint != null && !endpoint.isBlank())
        ? endpoint
        : "http://localhost:9000";

    String resolvedAccessKey = (accessKey != null && !accessKey.isBlank())
        ? accessKey
        : "default";

    String resolvedSecretKey = (secretKey != null && !secretKey.isBlank())
        ? secretKey
        : "default";

    return S3Client.builder()
      .endpointOverride(URI.create(resolvedEndpoint))
      .region(Region.of(region))
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(resolvedAccessKey, resolvedSecretKey)
      ))
      .serviceConfiguration(S3Configuration.builder()
        .pathStyleAccessEnabled(true)
        .build())
      .build();
  }
}
