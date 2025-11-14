package com.peliplat.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peliplat.ai.model.AutoCompleteResultVo;
import com.peliplat.ai.model.KeywordSearchRequestVo;
import com.peliplat.ai.model.KeywordSearchResultVo;
import com.peliplat.ai.model.MovieDetailVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关键词搜索服务
 * 处理Peliplat的自动完成和关键词搜索API
 */
@Service
public class KeywordSearchService {

    private static final Logger logger = LoggerFactory.getLogger(KeywordSearchService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final KeywordExtractionService keywordExtractionService;

    private static final String AUTO_COMPLETE_URL = "https://www.peliplat.com/api/web/search/query/autoComplete/v1";
    private static final String KEYWORD_SEARCH_URL = "https://www.peliplat.com/api/web/search/query/searchByKeywordsOnly/v1";

    public KeywordSearchService(KeywordExtractionService keywordExtractionService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.keywordExtractionService = keywordExtractionService;
    }

    /**
     * 调用自动完成API
     *
     * @param keyword 搜索关键词
     * @param language 语言代码
     * @return 自动完成结果
     */
    public AutoCompleteResultVo autoComplete(String keyword, String language) {
        try {
            logger.info("🔍 调用自动完成API: keyword={}, language={}", keyword, language);

            // 构建URL
            String url = UriComponentsBuilder.fromUriString(AUTO_COMPLETE_URL)
                    .queryParam("language", language)
                    .queryParam("client", "web")
                    .queryParam("filterMediaType", "1")
                    .queryParam("keyword", keyword)
                    .toUriString();

            // 发送GET请求
            ResponseEntity<AutoCompleteResultVo> response = restTemplate.getForEntity(
                    url,
                    AutoCompleteResultVo.class
            );

            AutoCompleteResultVo result = response.getBody();
            if (result != null && result.getRetCode() == 200) {
                logger.info("✅ 自动完成API返回 {} 个结果", result.getResult() != null ? result.getResult().size() : 0);
                return result;
            } else {
                logger.warn("⚠️ 自动完成API返回错误: {}", result != null ? result.getMessage() : "empty response");
                return null;
            }

        } catch (Exception e) {
            logger.error("❌ 调用自动完成API失败: {}", keyword, e);
            return null;
        }
    }

    /**
     * 从自动完成结果中提取关键词类型的docId
     *
     * @param autoCompleteResult 自动完成结果
     * @return 关键词docId列表
     */
    public List<String> extractKeywords(AutoCompleteResultVo autoCompleteResult) {
        if (autoCompleteResult == null || autoCompleteResult.getResult() == null) {
            return new ArrayList<>();
        }

        List<String> keywords = autoCompleteResult.getResult().stream()
                .filter(AutoCompleteResultVo.AutoCompleteItem::isKeyword)
                .map(AutoCompleteResultVo.AutoCompleteItem::getDocId)
                .limit(1)  // 只使用第一个(最高分)关键词
                .collect(Collectors.toList());

        logger.info("📋 从自动完成结果中提取到 {} 个关键词: {}", keywords.size(), keywords);
        return keywords;
    }

    /**
     * 根据关键词搜索电影
     *
     * @param keywords 关键词docId列表
     * @param language 语言代码
     * @return 搜索结果
     */
    public List<MovieDetailVo> searchByKeywords(List<String> keywords, String language) {
        try {
            logger.info("🎬 调用关键词搜索API: keywords={}, language={}", keywords, language);

            // 构建请求体
            KeywordSearchRequestVo request = new KeywordSearchRequestVo();
            request.setLanguage(language);
            request.setKeywords(keywords);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<KeywordSearchRequestVo> entity = new HttpEntity<>(request, headers);

            // 发送POST请求
            ResponseEntity<KeywordSearchResultVo> response = restTemplate.postForEntity(
                    KEYWORD_SEARCH_URL,
                    entity,
                    KeywordSearchResultVo.class
            );

            KeywordSearchResultVo result = response.getBody();
            if (result != null && result.getRetCode() == 200) {
                List<MovieDetailVo> movies = result.getResult();
                logger.info("✅ 关键词搜索返回 {} 部电影", movies != null ? movies.size() : 0);
                return movies != null ? movies : new ArrayList<>();
            } else {
                logger.warn("⚠️ 关键词搜索API返回错误: {}", result != null ? result.getMessage() : "empty response");
                return new ArrayList<>();
            }

        } catch (Exception e) {
            logger.error("❌ 调用关键词搜索API失败: {}", keywords, e);
            return new ArrayList<>();
        }
    }

    /**
     * 完整的关键词搜索流程
     * 1. 使用AI提取关键词并翻译成英文
     * 2. 调用自动完成API
     * 3. 提取关键词docId
     * 4. 如果有关键词，使用关键词搜索；否则返回null
     *
     * @param query 用户查询（可以是中文或英文）
     * @param language 语言代码
     * @return 搜索到的电影列表，如果没有找到关键词则返回null
     */
    public List<MovieDetailVo> searchMoviesByQuery(String query, String language) {
        logger.info("🚀 开始关键词搜索流程: query={}, language={}", query, language);

        // Step 1: 使用AI提取关键词并翻译成英文
        String extractedKeyword = keywordExtractionService.extractKeyword(query);
        if (extractedKeyword == null || extractedKeyword.isEmpty()) {
            logger.info("⚠️ AI未能提取关键词，跳过关键词搜索");
            return null;
        }

        logger.info("📝 AI提取的关键词: {} → {}", query, extractedKeyword);

        // Step 2: 使用提取的英文关键词调用自动完成API
        AutoCompleteResultVo autoCompleteResult = autoComplete(extractedKeyword, language);
        if (autoCompleteResult == null) {
            logger.info("⚠️ 自动完成API未返回结果，跳过关键词搜索");
            return null;
        }

        // Step 3: 提取关键词docId
        List<String> keywords = extractKeywords(autoCompleteResult);
        if (keywords.isEmpty()) {
            logger.info("⚠️ 未找到关键词类型的结果，跳过关键词搜索");
            return null;
        }

        // Step 4: 使用关键词搜索电影
        List<MovieDetailVo> movies = searchByKeywords(keywords, language);
        if (movies.isEmpty()) {
            logger.info("⚠️ 关键词搜索未返回电影结果");
            return null;
        }

        logger.info("✅ 关键词搜索流程完成，找到 {} 部电影", movies.size());
        return movies;
    }
}
