package com.careerpilot.ai.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    public static String cleanJsonString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "{}";
        }
        
        String cleaned = input.trim();
        
        // Remove markdown fenced code blocks if present
        if (cleaned.startsWith("```")) {
            // Find the end of the opening block (e.g. ```json)
            int firstNewLine = cleaned.indexOf('\n');
            if (firstNewLine != -1) {
                cleaned = cleaned.substring(firstNewLine + 1);
            } else {
                cleaned = cleaned.substring(3);
            }
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        cleaned = cleaned.trim();
        
        // Sometimes LLMs return json wrapped in brackets or with pre/post text. 
        // We find the first '{' or '[' and the last '}' or ']'
        int firstBrace = cleaned.indexOf('{');
        int firstBracket = cleaned.indexOf('[');
        
        int startIdx = -1;
        int endIdx = -1;
        
        if (firstBrace != -1 && (firstBracket == -1 || firstBrace < firstBracket)) {
            startIdx = firstBrace;
            endIdx = cleaned.lastIndexOf('}');
        } else if (firstBracket != -1) {
            startIdx = firstBracket;
            endIdx = cleaned.lastIndexOf(']');
        }
        
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            cleaned = cleaned.substring(startIdx, endIdx + 1);
        }
        
        return cleaned.trim();
    }
}
