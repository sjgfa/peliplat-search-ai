package com.peliplat.ai.service;

import com.peliplat.ai.config.AiModelProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AiModelFactory 测试用例
 */
@ExtendWith(MockitoExtension.class)
class AiModelFactoryTest {

    @Mock
    private AiModelProperties aiModelProperties;

    private AiModelFactory aiModelFactory;

    @BeforeEach
    void setUp() {
        aiModelFactory = new AiModelFactory(aiModelProperties);
    }

    @Test
    void testGetCurrentChatClient() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("deepseek", "openai");
        
        when(aiModelProperties.getActiveModel()).thenReturn("deepseek");
        when(aiModelProperties.getModelConfig("deepseek")).thenReturn(config);

        // 执行测试
        ChatClient chatClient = aiModelFactory.getCurrentChatClient();

        // 验证结果
        assertNotNull(chatClient);
        verify(aiModelProperties).getActiveModel();
    }

    @Test
    void testGetCurrentChatModel() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("deepseek", "openai");
        
        when(aiModelProperties.getActiveModel()).thenReturn("deepseek");
        when(aiModelProperties.getModelConfig("deepseek")).thenReturn(config);

        // 执行测试
        ChatModel chatModel = aiModelFactory.getCurrentChatModel();

        // 验证结果
        assertNotNull(chatModel);
        verify(aiModelProperties).getActiveModel();
    }

    @Test
    void testGetChatClientWithCaching() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("deepseek", "openai");
        
        when(aiModelProperties.getModelConfig("deepseek")).thenReturn(config);

        // 执行测试 - 第一次调用
        ChatClient chatClient1 = aiModelFactory.getChatClient("deepseek");
        assertNotNull(chatClient1);

        // 执行测试 - 第二次调用（应该从缓存获取）
        ChatClient chatClient2 = aiModelFactory.getChatClient("deepseek");
        assertNotNull(chatClient2);

        // 验证缓存机制
        assertSame(chatClient1, chatClient2);
    }

    @Test
    void testGetChatModelWithCaching() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("deepseek", "openai");
        
        when(aiModelProperties.getModelConfig("deepseek")).thenReturn(config);

        // 执行测试 - 第一次调用
        ChatModel chatModel1 = aiModelFactory.getChatModel("deepseek");
        assertNotNull(chatModel1);

        // 执行测试 - 第二次调用（应该从缓存获取）
        ChatModel chatModel2 = aiModelFactory.getChatModel("deepseek");
        assertNotNull(chatModel2);

        // 验证缓存机制
        assertSame(chatModel1, chatModel2);
    }

    @Test
    void testSwitchModelSuccess() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("gpt4", "openai");
        
        when(aiModelProperties.getModelConfig("gpt4")).thenReturn(config);

        // 执行测试
        boolean result = aiModelFactory.switchModel("gpt4");

        // 验证结果
        assertTrue(result);
        verify(aiModelProperties).setActiveModel("gpt4");
    }

    @Test
    void testSwitchModelFailure_ConfigNotFound() {
        // 准备测试数据
        when(aiModelProperties.getModelConfig("nonexistent")).thenReturn(null);

        // 执行测试
        boolean result = aiModelFactory.switchModel("nonexistent");

        // 验证结果
        assertFalse(result);
        verify(aiModelProperties, never()).setActiveModel(anyString());
    }

    @Test
    void testSwitchModelFailure_ModelDisabled() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("disabled-model", "openai");
        config.setEnabled(false);
        
        when(aiModelProperties.getModelConfig("disabled-model")).thenReturn(config);

        // 执行测试
        boolean result = aiModelFactory.switchModel("disabled-model");

        // 验证结果
        assertFalse(result);
        verify(aiModelProperties, never()).setActiveModel(anyString());
    }

    @Test
    void testClearModelCache() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("deepseek", "openai");
        
        when(aiModelProperties.getModelConfig("deepseek")).thenReturn(config);

        // 先创建一个模型实例
        ChatClient chatClient1 = aiModelFactory.getChatClient("deepseek");
        assertNotNull(chatClient1);

        // 清除缓存
        aiModelFactory.clearModelCache("deepseek");

        // 再次获取应该是新的实例
        ChatClient chatClient2 = aiModelFactory.getChatClient("deepseek");
        assertNotNull(chatClient2);

        // 验证不是同一个实例
        assertNotSame(chatClient1, chatClient2);
    }

    @Test
    void testClearAllCache() {
        // 准备测试数据
        AiModelProperties.ModelConfig config1 = createTestModelConfig("deepseek", "openai");
        AiModelProperties.ModelConfig config2 = createTestModelConfig("gpt4", "openai");
        
        when(aiModelProperties.getModelConfig("deepseek")).thenReturn(config1);
        when(aiModelProperties.getModelConfig("gpt4")).thenReturn(config2);

        // 先创建一些模型实例
        ChatClient chatClient1 = aiModelFactory.getChatClient("deepseek");
        ChatClient chatClient2 = aiModelFactory.getChatClient("gpt4");
        assertNotNull(chatClient1);
        assertNotNull(chatClient2);

        // 清除所有缓存
        aiModelFactory.clearAllCache();

        // 再次获取应该是新的实例
        ChatClient newChatClient1 = aiModelFactory.getChatClient("deepseek");
        ChatClient newChatClient2 = aiModelFactory.getChatClient("gpt4");
        assertNotNull(newChatClient1);
        assertNotNull(newChatClient2);

        // 验证不是同一个实例
        assertNotSame(chatClient1, newChatClient1);
        assertNotSame(chatClient2, newChatClient2);
    }

    @Test
    void testGetAvailableModels() {
        // 准备测试数据
        Map<String, AiModelProperties.ModelConfig> models = new HashMap<>();
        models.put("deepseek", createTestModelConfig("deepseek", "openai"));
        models.put("gpt4", createTestModelConfig("gpt4", "openai"));
        
        when(aiModelProperties.getModels()).thenReturn(models);
        when(aiModelProperties.getActiveModel()).thenReturn("deepseek");

        // 执行测试
        Map<String, Map<String, Object>> result = aiModelFactory.getAvailableModels();

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("deepseek"));
        assertTrue(result.containsKey("gpt4"));

        // 验证模型信息
        Map<String, Object> deepseekInfo = result.get("deepseek");
        assertEquals("deepseek", deepseekInfo.get("name"));
        assertEquals("openai", deepseekInfo.get("provider"));
        assertEquals("deepseek-chat", deepseekInfo.get("model"));
        assertEquals(true, deepseekInfo.get("active"));

        Map<String, Object> gpt4Info = result.get("gpt4");
        assertEquals("gpt4", gpt4Info.get("name"));
        assertEquals("openai", gpt4Info.get("provider"));
        assertEquals("gpt-4", gpt4Info.get("model"));
        assertEquals(false, gpt4Info.get("active"));
    }

    @Test
    void testCreateDoubaoModel() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("doubao", "bytedance");
        
        when(aiModelProperties.getModelConfig("doubao")).thenReturn(config);

        // 执行测试
        ChatModel chatModel = aiModelFactory.getChatModel("doubao");

        // 验证结果
        assertNotNull(chatModel);
        assertTrue(chatModel instanceof CustomDoubaoModel);
    }

    @Test
    void testCreateModelWithInvalidProvider() {
        // 准备测试数据
        AiModelProperties.ModelConfig config = createTestModelConfig("invalid", "invalid-provider");
        
        when(aiModelProperties.getModelConfig("invalid")).thenReturn(config);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aiModelFactory.getChatModel("invalid");
        });

        assertTrue(exception.getMessage().contains("Failed to create ChatModel"));
    }

    /**
     * 创建测试用的模型配置
     */
    private AiModelProperties.ModelConfig createTestModelConfig(String name, String provider) {
        AiModelProperties.ModelConfig config = new AiModelProperties.ModelConfig();
        config.setProvider(provider);
        config.setApiKey("test-api-key");
        config.setBaseUrl("https://api.test.com");
        config.setTemperature(0.3);
        config.setMaxTokens(1500);
        config.setTopP(1.0);
        config.setTimeout(60);
        config.setEnabled(true);
        config.setDescription("Test model for " + name);
        
        // 根据名称设置不同的模型
        switch (name) {
            case "deepseek":
                config.setModel("deepseek-chat");
                break;
            case "gpt4":
                config.setModel("gpt-4");
                break;
            case "doubao":
                config.setModel("doubao-1-5-pro-32k-250115");
                config.setBaseUrl("https://ark.cn-beijing.volces.com/api/v3");
                break;
            default:
                config.setModel(name + "-model");
        }
        
        return config;
    }
}