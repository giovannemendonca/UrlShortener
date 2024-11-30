package dev.giovannemendonca.createUrlShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.giovannemendonca.createUrlShortner.dto.UrlData;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private static final String BUCKET_NAME = "url-shortener-storage-gm";
    private static final String BASE_URL = "https://q2w2c66pki.execute-api.us-east-1.amazonaws.com/";
    private final Logger logger = Logger.getLogger(Main.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        try {
            Map<String, String> bodyMap = parseRequestBody(input);
            validateRequestBody(bodyMap);

            String shortUrlCode = generateShortUrlCode();
            UrlData urlData = createUrlData(bodyMap);

            saveUrlDataToS3(shortUrlCode, urlData);

            return createResponse(shortUrlCode);

        } catch (IllegalArgumentException e) {
            logger.warning("Validation error: " + e.getMessage());
            return createErrorResponse("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            return createErrorResponse("Internal Server Error");
        }
    }

    private Map<String, String> parseRequestBody(Map<String, Object> input) {
        String body = (String) input.get("body");
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Request body is missing");
        }

        try {
            return objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error parsing JSON body: " + e.getMessage(), e);
        }
    }

    private void validateRequestBody(Map<String, String> bodyMap) {
        if (!bodyMap.containsKey("originalUrl") || bodyMap.get("originalUrl").isEmpty()) {
            throw new IllegalArgumentException("Missing or empty 'originalUrl'");
        }
        if (!bodyMap.containsKey("expirationTime") || bodyMap.get("expirationTime").isEmpty()) {
            throw new IllegalArgumentException("Missing or empty 'expirationTime'");
        }

        try {
            Long.parseLong(bodyMap.get("expirationTime"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid 'expirationTime', must be a valid number");
        }
    }

    private String generateShortUrlCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private UrlData createUrlData(Map<String, String> bodyMap) {
        String originalUrl = bodyMap.get("originalUrl");
        long expirationTime = Long.parseLong(bodyMap.get("expirationTime"));
        return new UrlData(originalUrl, expirationTime);
    }

    private void saveUrlDataToS3(String shortUrlCode, UrlData urlData) {
        try {
            String urlDataJson = objectMapper.writeValueAsString(urlData);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(generateS3Key(shortUrlCode))
                    .build();

            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch (Exception e) {
            throw new RuntimeException("Error saving data to S3: " + e.getMessage(), e);
        }
    }

    private static String generateS3Key(String shortUrlCode) {
        return shortUrlCode + ".json";
    }

    private Map<String, String> createResponse(String shortUrlCode) {
        Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);
        response.put("shortenedUrl", BASE_URL + shortUrlCode);
        return response;
    }

    private Map<String, String> createErrorResponse(String errorMessage) {
        Map<String, String> response = new HashMap<>();
        response.put("error", errorMessage);
        return response;
    }
}