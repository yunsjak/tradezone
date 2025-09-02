package com.shop.tradezone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsS3Config {

	@Value("${aws.access-key}")
	private String accessKey;

	@Value("${aws.secret-key}")
	private String secretKey;

	@Value("${aws.region}")
	private String region;

	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
		return S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
				.region(Region.of(region)).build();
	}
}