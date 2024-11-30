package dev.giovannemendonca.redirectUrlShortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.giovannemendonca.redirectUrlShortener.dto.UrlData;
import dev.giovannemendonca.redirectUrlShortener.exceptions.InvalidDataException;
import dev.giovannemendonca.redirectUrlShortener.exceptions.ResourceNotFoundException;
import dev.giovannemendonca.redirectUrlShortener.utils.ResponseUtil;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;


public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String BUCKET_NAME = "url-shortener-storage-gm";
    private final Logger logger = Logger.getLogger(Main.class.getName());
    private final S3Client s3Client = S3Client.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {
            String shortUrlCode = extractShortUrlCode(input);
            InputStream s3ObjectStream = getObjectFromS3(BUCKET_NAME, shortUrlCode + ".json");
            UrlData urlData = parseUrlData(s3ObjectStream);

            return generateResponse(urlData);
        } catch (IllegalArgumentException | ResourceNotFoundException | InvalidDataException e) {
            logger.warning(e.getMessage());
            return createResponse(400, null, e.getMessage());
        } catch (Exception e) {
            logger.severe("Unhandled exception: " + e.getMessage());
            return createResponse(500, null, "Internal Server Error");
        }
    }

    private String extractShortUrlCode(Map<String, Object> input) {
        String pathParameters = (String) input.get("rawPath");
        if (pathParameters == null || pathParameters.isEmpty()) {
            throw new IllegalArgumentException("Invalid path parameter");
        }
        return pathParameters.replace("/", "");
    }

    private InputStream getObjectFromS3(String bucketName, String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Short URL not found.");
        }
    }

    private UrlData parseUrlData(InputStream s3ObjectStream) {
        try {
            return objectMapper.readValue(s3ObjectStream, UrlData.class);
        } catch (Exception e) {
            throw new InvalidDataException("Malformed URL data.");
        }
    }

    private Map<String, Object> generateResponse(UrlData urlData) {
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        if (currentTimeInSeconds > urlData.getExpirationTime()) {
            return createResponse(410, null, "This URL has expired.");
        }

        return createResponse(302, urlData.getOriginalUrl(), null);
    }

    private Map<String, Object> createResponse(int statusCode, String location, String message) {
        return ResponseUtil.createResponse(statusCode, location, message);
    }
}
