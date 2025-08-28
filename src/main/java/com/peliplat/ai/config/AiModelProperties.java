package com.peliplat.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AI模型配置类
 * 支持多种AI模型的配置管理
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiModelProperties {
    
    /**
     * 当前激活的模型名称
     */
    private String activeModel = "deepseek";
    
    /**
     * 模型配置映射
     */
    private Map<String, ModelConfig> models = new HashMap<>();
    
    public String getActiveModel() {
        return activeModel;
    }
    
    public void setActiveModel(String activeModel) {
        this.activeModel = activeModel;
    }
    
    public Map<String, ModelConfig> getModels() {
        return models;
    }
    
    public void setModels(Map<String, ModelConfig> models) {
        this.models = models;
    }
    
    /**
     * 获取当前激活模型的配置
     */
    public ModelConfig getActiveModelConfig() {
        return models.get(activeModel);
    }
    
    /**
     * 获取指定模型的配置
     */
    public ModelConfig getModelConfig(String modelName) {
        return models.get(modelName);
    }
    
    /**
     * 单个模型配置类
     */
    public static class ModelConfig {
        private String provider;           // 提供商：openai, deepseek, claude, ollama
        private String apiKey;            // API密钥
        private String baseUrl;           // API基础URL
        private String model;             // 模型名称
        private Double temperature;       // 温度参数
        private Integer maxTokens;        // 最大token数
        private Double topP;              // TopP参数
        private Integer timeout;          // 超时时间(秒)
        private Boolean enabled;          // 是否启用
        private String description;       // 模型描述
        
        // 构造函数
        public ModelConfig() {}
        
        public ModelConfig(String provider, String apiKey, String baseUrl, String model, 
                          Double temperature, Integer maxTokens) {
            this.provider = provider;
            this.apiKey = apiKey;
            this.baseUrl = baseUrl;
            this.model = model;
            this.temperature = temperature;
            this.maxTokens = maxTokens;
            this.enabled = true;
        }
        
        // Getters and Setters
        public String getProvider() {
            return provider;
        }
        
        public void setProvider(String provider) {
            this.provider = provider;
        }
        
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getModel() {
            return model;
        }
        
        public void setModel(String model) {
            this.model = model;
        }
        
        public Double getTemperature() {
            return temperature != null ? temperature : 0.3;
        }
        
        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
        
        public Integer getMaxTokens() {
            return maxTokens != null ? maxTokens : 1500;
        }
        
        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }
        
        public Double getTopP() {
            return topP != null ? topP : 1.0;
        }
        
        public void setTopP(Double topP) {
            this.topP = topP;
        }
        
        public Integer getTimeout() {
            return timeout != null ? timeout : 60;
        }
        
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }
        
        public Boolean getEnabled() {
            return enabled != null ? enabled : true;
        }
        
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "ModelConfig{" +
                    "provider='" + provider + '\'' +
                    ", model='" + model + '\'' +
                    ", temperature=" + temperature +
                    ", maxTokens=" + maxTokens +
                    ", enabled=" + enabled +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}