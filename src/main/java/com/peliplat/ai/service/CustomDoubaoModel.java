package com.peliplat.ai.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义豆包模型实现
 * 直接调用豆包API，绕过Spring AI的OpenAI兼容性问题
 */
public class CustomDoubaoModel implements ChatModel {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomDoubaoModel.class);
    
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public CustomDoubaoModel(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public ChatResponse call(Prompt prompt) {
        try {
            logger.info("调用豆包API: {} ({})", model, baseUrl);
            
            // 构建请求
            DoubaoRequest request = new DoubaoRequest();
            request.model = this.model;
            request.messages = prompt.getInstructions().stream()
                    .map(this::convertMessage)
                    .collect(Collectors.toList());
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<DoubaoRequest> entity = new HttpEntity<>(request, headers);
            
            // 发送请求
            String url = baseUrl.endsWith("/chat/completions") ? baseUrl : baseUrl + "/chat/completions";
            ResponseEntity<DoubaoResponse> response = restTemplate.postForEntity(url, entity, DoubaoResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DoubaoResponse doubaoResponse = response.getBody();
                logger.info("豆包API调用成功，返回 {} 个选择", doubaoResponse.choices.size());
                
                // 转换响应
                List<Generation> generations = doubaoResponse.choices.stream()
                        .map(choice -> new Generation(new AssistantMessage(choice.message.content)))
                        .collect(Collectors.toList());
                
                return new ChatResponse(generations);
            } else {
                throw new RuntimeException("豆包API调用失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("豆包API调用异常", e);
            throw new RuntimeException("豆包API调用失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // 简单实现：将普通调用结果转换为Flux
        return Flux.just(call(prompt));
    }
    
    private DoubaoMessage convertMessage(Message message) {
        DoubaoMessage doubaoMessage = new DoubaoMessage();
        
        switch (message.getMessageType()) {
            case SYSTEM:
                doubaoMessage.role = "system";
                break;
            case USER:
                doubaoMessage.role = "user";
                break;
            case ASSISTANT:
                doubaoMessage.role = "assistant";
                break;
            default:
                doubaoMessage.role = "user";
        }
        
        // 根据不同的消息类型获取内容 - 兼容Spring AI 1.0.0-M6
        if (message instanceof org.springframework.ai.chat.messages.SystemMessage) {
            org.springframework.ai.chat.messages.SystemMessage systemMessage = 
                (org.springframework.ai.chat.messages.SystemMessage) message;
            doubaoMessage.content = systemMessage.getText();
        } else if (message instanceof org.springframework.ai.chat.messages.UserMessage) {
            org.springframework.ai.chat.messages.UserMessage userMessage = 
                (org.springframework.ai.chat.messages.UserMessage) message;
            doubaoMessage.content = userMessage.getText();
        } else if (message instanceof org.springframework.ai.chat.messages.AssistantMessage) {
            org.springframework.ai.chat.messages.AssistantMessage assistantMessage = 
                (org.springframework.ai.chat.messages.AssistantMessage) message;
            doubaoMessage.content = assistantMessage.getText();
        } else {
            // 备用方案：尝试toString()
            doubaoMessage.content = message.toString();
        }
        
        return doubaoMessage;
    }
    
    // 豆包API请求数据结构
    public static class DoubaoRequest {
        public String model;
        public List<DoubaoMessage> messages;
        public Double temperature = 0.3;
        @JsonProperty("max_tokens")
        public Integer maxTokens = 1500;
        @JsonProperty("top_p")
        public Double topP = 1.0;
    }
    
    public static class DoubaoMessage {
        public String role;
        public String content;
    }
    
    // 豆包API响应数据结构
    public static class DoubaoResponse {
        public String id;
        public String object;
        public Long created;
        public String model;
        public List<DoubaoChoice> choices;
        public DoubaoUsage usage;
    }
    
    public static class DoubaoChoice {
        public Integer index;
        public DoubaoMessage message;
        @JsonProperty("finish_reason")
        public String finishReason;
    }
    
    public static class DoubaoUsage {
        @JsonProperty("prompt_tokens")
        public Integer promptTokens;
        @JsonProperty("completion_tokens")
        public Integer completionTokens;
        @JsonProperty("total_tokens")
        public Integer totalTokens;
    }
}