package com.peliplat.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peliplat.ai.model.AutoCompleteResultVo;
import com.peliplat.ai.model.KeywordSearchRequestVo;
import com.peliplat.ai.model.KeywordSearchResultVo;
import com.peliplat.ai.model.MovieDetailVo;
import com.peliplat.ai.model.SearchResultVo;
import com.peliplat.ai.model.MediaDetailVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static final String TITLE_SEARCH_URL = "https://www.peliplat.com/api/web/search/detailSearch/v2";

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
     * 从自动完成结果中提取媒体标题类型的结果
     *
     * @param autoCompleteResult 自动完成结果
     * @return 媒体标题列表
     */
    public List<String> extractMediaTitles(AutoCompleteResultVo autoCompleteResult) {
        if (autoCompleteResult == null || autoCompleteResult.getResult() == null) {
            return new ArrayList<>();
        }

        List<String> titles = autoCompleteResult.getResult().stream()
                .filter(AutoCompleteResultVo.AutoCompleteItem::isMedia)
                .map(AutoCompleteResultVo.AutoCompleteItem::getTitle)
                .limit(3)  // 最多取前3个最高分的媒体标题
                .collect(Collectors.toList());

        logger.info("🎬 从自动完成结果中提取到 {} 个媒体标题: {}", titles.size(), titles);
        return titles;
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
     * 根据标题搜索电影
     *
     * @param titles 标题列表
     * @param language 语言代码
     * @return 搜索结果
     */
    public List<MovieDetailVo> searchByTitles(List<String> titles, String language) {
        try {
            logger.info("🎯 调用标题搜索API: titles={}, language={}", titles, language);

            List<MovieDetailVo> allMovies = new ArrayList<>();

            // 对每个标题进行搜索
            for (String title : titles) {
                try {
                    // 构建URL
                    String url = UriComponentsBuilder.fromHttpUrl(TITLE_SEARCH_URL)
                            .queryParam("languageCode", language)
                            .queryParam("pageSize", 20)
                            .queryParam("client", "web")
                            .queryParam("keyword", URLEncoder.encode(title, StandardCharsets.UTF_8.name()))
                            .queryParam("pageId", 1)
                            .queryParam("mark", "movies,series")
                            .build()
                            .toUriString();

                    logger.info("🔍 搜索标题: {} - URL: {}", title, url);

                    // 发送GET请求
                    ResponseEntity<SearchResultVo> response = restTemplate.getForEntity(url, SearchResultVo.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        SearchResultVo searchResult = response.getBody();

                        if (searchResult.getResult() != null && searchResult.getResult().getList() != null) {
                            List<SearchResultVo.SearchItem> items = searchResult.getResult().getList();

                            for (SearchResultVo.SearchItem item : items) {
                                if (item.getDetails() != null) {
                                    MovieDetailVo movie = convertMediaDetailToMovie(item.getDetails());
                                    movie.setRelateType(item.getRelateType());
                                    allMovies.add(movie);
                                }
                            }

                            logger.info("✅ 标题 \"{}\" 搜索到 {} 部电影", title, items.size());
                        }
                    }
                } catch (Exception e) {
                    logger.error("❌ 搜索标题 \"{}\" 失败", title, e);
                }
            }

            logger.info("✅ 标题搜索完成，共找到 {} 部电影", allMovies.size());
            return allMovies;

        } catch (Exception e) {
            logger.error("❌ 调用标题搜索API失败: {}", titles, e);
            return new ArrayList<>();
        }
    }

    /**
     * 将MediaDetailVo转换为MovieDetailVo
     */
    private MovieDetailVo convertMediaDetailToMovie(MediaDetailVo mediaDetail) {
        MovieDetailVo movie = new MovieDetailVo();

        // 复制属性
        movie.setMediaId(mediaDetail.getMediaId());
        movie.setPpId(mediaDetail.getPpId());
        movie.setMmId(mediaDetail.getPpId()); // 使用相同的ppId
        movie.setTitle(mediaDetail.getTitle());
        movie.setPhotoUrl(mediaDetail.getPoster());
        movie.setPublicationYear(mediaDetail.getPublicYear());
        movie.setRating(mediaDetail.getRating());
        movie.setVoteCount(0);
        movie.setMediaType(mediaDetail.getMediaType());
        movie.setRank(0);
        movie.setPopularity(9999900.0);
        movie.setWatchStatus(mediaDetail.getWatchStatus());
        movie.setWatchListStatus(mediaDetail.getWatchListStatus());
        movie.setCertificate(mediaDetail.getCertificate());
        movie.setRunTime(mediaDetail.getRunTime());
        movie.setGenres(mediaDetail.getGenres());
        movie.setGeners(mediaDetail.getGeners());
        movie.setGenreIds(new ArrayList<>());
        movie.setSeason(0);
        movie.setEpisode(0);
        movie.setReleaseDate(mediaDetail.getReleaseDate());

        return movie;
    }

    /**
     * 完整的搜索流程（AI智能意图识别版）
     * 1. 使用AI识别用户搜索意图（作品名称 vs 类型关键词）
     * 2. 根据AI判断的意图直接选择搜索方式：
     *    - TITLE意图 → 直接使用标题搜索API
     *    - GENRE意图 → 使用自动完成API + 关键词搜索API
     *
     * @param query 用户查询（可以是中文或英文）
     * @param language 语言代码
     * @return 搜索到的电影列表，如果没有找到则返回null
     */
    public List<MovieDetailVo> searchMoviesByQuery(String query, String language) {
        logger.info("🚀 开始AI智能搜索流程: query={}, language={}", query, language);

        // Step 1: 使用AI识别意图并提取关键词
        KeywordExtractionService.IntentResult intentResult = keywordExtractionService.extractKeywordWithIntent(query);
        if (intentResult == null) {
            logger.info("⚠️ AI意图识别失败，跳过搜索");
            return null;
        }

        String keyword = intentResult.getKeyword();
        KeywordExtractionService.SearchIntent intent = intentResult.getIntent();

        logger.info("🎯 AI识别意图: {} → 意图类型={}, 关键词={}", query, intent, keyword);

        // Step 2: 根据意图类型选择搜索策略
        if (intent == KeywordExtractionService.SearchIntent.TITLE) {
            // 作品名称 → 直接使用标题搜索
            logger.info("📺 识别为作品名称，使用标题搜索API: {}", keyword);
            List<MovieDetailVo> movies = searchByTitles(List.of(keyword), language);
            if (!movies.isEmpty()) {
                logger.info("✅ 标题搜索完成，找到 {} 部电影", movies.size());
                return movies;
            } else {
                logger.info("⚠️ 标题搜索未找到结果");
                return null;
            }
        } else {
            // 类型关键词 → 使用自动完成API + 关键词搜索
            logger.info("🏷️ 识别为类型关键词，使用关键词搜索流程: {}", keyword);

            // 调用自动完成API
            AutoCompleteResultVo autoCompleteResult = autoComplete(keyword, language);
            if (autoCompleteResult == null) {
                logger.info("⚠️ 自动完成API未返回结果");
                return null;
            }

            // 提取关键词docId
            List<String> keywords = extractKeywords(autoCompleteResult);
            if (keywords.isEmpty()) {
                logger.info("⚠️ 未找到关键词类型的结果");
                return null;
            }

            // 使用关键词搜索
            List<MovieDetailVo> movies = searchByKeywords(keywords, language);
            if (!movies.isEmpty()) {
                logger.info("✅ 关键词搜索完成，找到 {} 部电影", movies.size());
                return movies;
            } else {
                logger.info("⚠️ 关键词搜索未返回结果");
                return null;
            }
        }
    }
}
