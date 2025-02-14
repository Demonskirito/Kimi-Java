package com.example.openai.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KimiWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(KimiWebSocketHandler.class);
    private static final String KIMI_API_URL = "https://api.moonshot.cn/v1/chat/completions"; // 确保 URL 是正确的
    private static final String KIMI_API_KEY = "你的KIMI API KEY";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Client connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received message: " + message.getPayload());

        // 解析 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        // 获取 content 的值
        String content = jsonNode.path("content").asText();

        // 打印 content 的值
        logger.info("Content: " + content);

        // 调用 Kimi API 并返回响应
        String response = callKimiAPI(content);
        session.sendMessage(new TextMessage(response));
    }

    private String callKimiAPI(String userMessage) {
        try {
            URL url = new URL(KIMI_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + KIMI_API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);



            // 请求体
            String requestBody = "{"
                    + "\"model\": \"moonshot-v1-8k\", "
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + userMessage + "\"}], "
                    + "\"max_tokens\": 150"
                    + "}";


            logger.info("Request body: " + requestBody); // 打印请求体

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            logger.info("Response code: " + responseCode); // 打印响应代码

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    JSONObject jsonObject = new JSONObject(content.toString());
                    logger.info("Response body: " + jsonObject.toString()); // 打印响应体
                    return jsonObject.getJSONArray("choices").getJSONObject(0).getString("message");
                }
            } else {
                logger.warn("Failed to call Kimi API. Response code: " + responseCode);
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = in.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    logger.error("Error response: " + errorResponse.toString()); // 打印错误响应
                }
                return "Error: Failed to get response from Kimi AI. Please check the API URL and your network connection.";
            }
        } catch (Exception e) {
            logger.error("Error calling Kimi API: " + e.getMessage());
            return "Error: Failed to call Kimi AI. Please check the API URL and your network connection.";
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Client disconnected: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error: " + exception.getMessage());
    }
}
