package com.example.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CheckUtils {
    public void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.getWriter().write(message);
    }

    public boolean isAnyParameterMissing(String... params) {
        for(String i : params) {
            if(i == null || i.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
