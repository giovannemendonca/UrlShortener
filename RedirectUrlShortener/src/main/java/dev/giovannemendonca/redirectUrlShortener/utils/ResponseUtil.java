package dev.giovannemendonca.redirectUrlShortener.utils;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    public static Map<String, Object> createResponse(int statusCode, String location, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);

        if (location != null) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Location", location);
            response.put("headers", headers);
        }

        if (message != null) {
            response.put("body", message);
        }

        return response;
    }
}
