package com.peliplat.ai.service;

import com.peliplat.ai.config.AiModelProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI模型工厂服务
 * 负责创建和管理不同的AI模型实例
 */
@Service
public class AiModelFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(AiModelFactory.class);
    
    private final AiModelProperties aiModelProperties;
    
    // 缓存已创建的ChatClient实例
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();
    
    // 缓存已创建的ChatModel实例
    private final Map<String, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    
    @Autowired
    public AiModelFactory(AiModelProperties aiModelProperties) {
        this.aiModelProperties = aiModelProperties;
    }
    
    /**
     * 获取当前激活的ChatClient
     */
    public ChatClient getCurrentChatClient() {
        String activeModel = aiModelProperties.getActiveModel();
        return getChatClient(activeModel);
    }
    
    /**
     * 获取指定模型的ChatClient
     */
    public ChatClient getChatClient(String modelName) {
        return chatClientCache.computeIfAbsent(modelName, this::createChatClient);
    }
    
    /**
     * 获取当前激活的ChatModel
     */
    public ChatModel getCurrentChatModel() {
        String activeModel = aiModelProperties.getActiveModel();
        return getChatModel(activeModel);
    }
    
    /**
     * 获取指定模型的ChatModel
     */
    public ChatModel getChatModel(String modelName) {
        return chatModelCache.computeIfAbsent(modelName, this::createChatModel);
    }
    
    /**
     * 切换当前激活的模型
     */
    public boolean switchModel(String modelName) {
        AiModelProperties.ModelConfig config = aiModelProperties.getModelConfig(modelName);
        if (config == null) {
            logger.error("模型配置不存在: {}", modelName);
            return false;
        }
        
        if (!config.getEnabled()) {
            logger.error("模型未启用: {}", modelName);
            return false;
        }
        
        try {
            // 预创建模型实例以验证配置
            createChatModel(modelName);
            
            // 切换激活模型
            aiModelProperties.setActiveModel(modelName);
            logger.info("成功切换到模型: {} ({})", modelName, config.getDescription());
            return true;
        } catch (Exception e) {
            logger.error("切换模型失败: {} - {}", modelName, e.getMessage());
            return false;
        }
    }
    
    /**
     * 清除指定模型的缓存
     */
    public void clearModelCache(String modelName) {
        chatClientCache.remove(modelName);
        chatModelCache.remove(modelName);
        logger.info("清除模型缓存: {}", modelName);
    }
    
    /**
     * 清除所有模型缓存
     */
    public void clearAllCache() {
        chatClientCache.clear();
        chatModelCache.clear();
        logger.info("清除所有模型缓存");
    }
    
    /**
     * 获取所有可用模型信息
     */
    public Map<String, Map<String, Object>> getAvailableModels() {
        Map<String, Map<String, Object>> models = new HashMap<>();
        
        for (Map.Entry<String, AiModelProperties.ModelConfig> entry : aiModelProperties.getModels().entrySet()) {
            String modelName = entry.getKey();
            AiModelProperties.ModelConfig config = entry.getValue();
            
            Map<String, Object> modelInfo = new HashMap<>();
            modelInfo.put("name", modelName);
            modelInfo.put("provider", config.getProvider());
            modelInfo.put("model", config.getModel());
            modelInfo.put("description", config.getDescription());
            modelInfo.put("enabled", config.getEnabled());
            modelInfo.put("temperature", config.getTemperature());
            modelInfo.put("maxTokens", config.getMaxTokens());
            modelInfo.put("active", modelName.equals(aiModelProperties.getActiveModel()));
            
            models.put(modelName, modelInfo);
        }
        
        return models;
    }
    
    /**
     * 创建ChatClient实例
     */
    private ChatClient createChatClient(String modelName) {
        try {
            ChatModel chatModel = createChatModel(modelName);
            AiModelProperties.ModelConfig config = aiModelProperties.getModelConfig(modelName);
            
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultOptions(createChatOptions(config))
                    .build();
            
            logger.info("创建ChatClient成功: {} ({})", modelName, config.getDescription());
            return chatClient;
            
        } catch (Exception e) {
            logger.error("创建ChatClient失败: {} - {}", modelName, e.getMessage());
            throw new RuntimeException("Failed to create ChatClient for model: " + modelName, e);
        }
    }
    
    /**
     * 创建ChatModel实例
     */
    private ChatModel createChatModel(String modelName) {
        AiModelProperties.ModelConfig config = aiModelProperties.getModelConfig(modelName);
        if (config == null) {
            throw new IllegalArgumentException("Model config not found: " + modelName);
        }
        
        if (!config.getEnabled()) {
            throw new IllegalArgumentException("Model is disabled: " + modelName);
        }
        
        try {
            return switch (config.getProvider().toLowerCase()) {
                case "openai", "deepseek" -> createOpenAiCompatibleModel(config);
                case "anthropic", "claude" -> createClaudeModel(config);
                case "ollama" -> createOllamaModel(config);
                case "alibaba", "qwen" -> createQwenModel(config);
                case "bytedance", "doubao" -> createDoubaoModel(config);
                default -> throw new IllegalArgumentException("Unsupported provider: " + config.getProvider());
            };
        } catch (Exception e) {
            logger.error("创建ChatModel失败: {} - {}", modelName, e.getMessage());
            throw new RuntimeException("Failed to create ChatModel for: " + modelName, e);
        }
    }
    
    /**
     * 创建OpenAI兼容的模型（包括DeepSeek）
     */
    private ChatModel createOpenAiCompatibleModel(AiModelProperties.ModelConfig config) {
        OpenAiApi openAiApi = new OpenAiApi(config.getBaseUrl(), config.getApiKey());
        
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
        
        return new OpenAiChatModel(openAiApi, options);
    }
    
    /**
     * 创建Claude模型
     */
    private ChatModel createClaudeModel(AiModelProperties.ModelConfig config) {
        // 注意：这里需要添加Claude的实际实现
        // 目前Spring AI可能还没有直接支持Claude，需要自定义实现
        logger.warn("Claude模型支持尚未完全实现，使用OpenAI兼容接口");
        return createOpenAiCompatibleModel(config);
    }
    
    /**
     * 创建Ollama模型
     */
    private ChatModel createOllamaModel(AiModelProperties.ModelConfig config) {
        // 注意：这里需要添加Ollama的实际实现
        logger.warn("Ollama模型支持尚未完全实现，使用OpenAI兼容接口");
        return createOpenAiCompatibleModel(config);
    }
    
    /**
     * 创建通义千问模型
     */
    private ChatModel createQwenModel(AiModelProperties.ModelConfig config) {
        // 注意：这里需要添加通义千问的实际实现
        logger.warn("通义千问模型支持尚未完全实现，使用OpenAI兼容接口");
        return createOpenAiCompatibleModel(config);
    }
    
    /**
     * 创建字节跳动豆包模型
     * 使用自定义实现来确保API兼容性
     */
    private ChatModel createDoubaoModel(AiModelProperties.ModelConfig config) {
        logger.info("创建字节跳动豆包模型: {} ({})", config.getModel(), config.getDescription());
        
        // 使用自定义豆包模型实现，避免Spring AI OpenAI兼容性问题
        String baseUrl = config.getBaseUrl();
        if (!baseUrl.endsWith("/chat/completions")) {
            baseUrl = baseUrl + "/chat/completions";
        }
        
        return new CustomDoubaoModel(config.getApiKey(), baseUrl, config.getModel());
    }
    
    /**
     * 创建ChatOptions
     */
    private OpenAiChatOptions createChatOptions(AiModelProperties.ModelConfig config) {
        return OpenAiChatOptions.builder()
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .topP(config.getTopP())
                .build();
    }
}