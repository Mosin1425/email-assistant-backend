package com.mosin.email.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mosin.email.dto.EmailRequestDto;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class EmailGeneratorService {
	
	@Value("${gemini.api.url}")
	private String geminiApiUrl;
	
	@Value("${gemini.api.key}")
	private String geminiApiKey;

	public ResponseEntity<Object> generateEmailContent(EmailRequestDto emailRequestDto) {
		String prompt = buildPrompt(emailRequestDto);
		String responseText = callGemini(prompt);
		String extractedText = extractGeminiResponse(responseText);
//		log.info("[EmailGeneratorService] [generateEmailContent] Final text: " + extractedText);
		
		return ResponseEntity.ok().body(extractedText);
	
	}

	private String extractGeminiResponse(String responseText) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(responseText);
			
			return rootNode.path("candidates").get(0)
					.path("content")
					.path("parts").get(0)
					.path("text").asText();
		} catch (Exception e) {
			log.error("[EmailGeneratorService] [generateEmailContent] C*ud gye guru...");
			e.printStackTrace();
		}
		return null;
	}

	private String buildPrompt(EmailRequestDto emailRequestDto) {
	    StringBuilder prompt = new StringBuilder();

	    prompt.append("You are a professional email assistant. ")
	          .append("Generate a well-structured and contextually appropriate reply to the email provided below. ")
	          .append("Avoid generating a subject line. ");

	    if (StringUtils.isNotEmpty(emailRequestDto.getTone())) {
	        Map<String, String> toneInstructions = Map.of(
	            "Professional", "Use a concise, respectful, and formal tone suitable for workplace communication. ",
	            "Friendly", "Use a warm, conversational, and casual tone, like speaking to a colleague. ",
	            "Apologetic", "Use a polite and understanding tone that sincerely conveys an apology. ",
	            "Formal", "Use structured, business-like language with no contractions. ",
	            "Casual", "Use relaxed and informal language as if chatting informally. "
	        );

	        String tonePrompt = toneInstructions.getOrDefault(emailRequestDto.getTone(), 
	                "Use a polite and context-aware tone. ");
	        prompt.append(tonePrompt);
	    }

	    prompt.append("Respond directly to the message content. ")
	          .append("Ensure the reply sounds human, polite, and complete.\n\n");

	    prompt.append("Original Email:\n")
	          .append(emailRequestDto.getEmailContent());

//	    log.info("[EmailGeneratorService] [buildPrompt] Final prompt: " + prompt.toString());

	    return prompt.toString();
	}


	
	private String callGemini(String prompt) {
        try {
//            OkHttpClient client = new OkHttpClient().newBuilder().build();

        	OkHttpClient client = new OkHttpClient.Builder()
        		    .connectTimeout(10, TimeUnit.SECONDS)
        		    .readTimeout(15, TimeUnit.SECONDS)
        		    .writeTimeout(15, TimeUnit.SECONDS)
        		    .build();

            MediaType mediaType = MediaType.parse("application/json");
            String jsonBody = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"parts\": [\n" +
                    "        {\n" +
                    "          \"text\": \"" + escapeJson(prompt) + "\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            @SuppressWarnings("deprecation")
			RequestBody body = RequestBody.create(mediaType, jsonBody);
            String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

            Request request = new Request.Builder()
                    .url(fullUrl)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

//            log.info("Before calling...");
            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    return response.body().string();
                } else {
                    return "Empty response";
                }
            }

        } catch (IOException e) {
        	log.error("[EmailGeneratorService] [callGemini] C*ud gye guru...");
            e.printStackTrace();
            
            return "Error occurred while calling Gemini API";
        }
    }
	
	private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
