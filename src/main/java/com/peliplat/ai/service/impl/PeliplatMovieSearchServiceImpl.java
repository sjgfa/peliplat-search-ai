package com.peliplat.ai.service.impl;

import com.peliplat.ai.model.MediaBaseVo;
import com.peliplat.ai.model.MediaDetailVo;
import com.peliplat.ai.model.MovieDetailVo;
import com.peliplat.ai.model.MovieListResponseVo;
import com.peliplat.ai.model.SearchResultVo;
import com.peliplat.ai.service.MovieSearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.peliplat.ai.util.MovieGenreConstants;

/**
 * PeliPlat电影搜索服务实现
 * 通过调用PeliPlat API搜索电影
 */
@Service("peliplatMovieSearchService")
public class PeliplatMovieSearchServiceImpl implements MovieSearchService {

    private static final Logger logger = LoggerFactory.getLogger(PeliplatMovieSearchServiceImpl.class);
    private static final String PELIPLAT_API_BASE_URL = "https://www.peliplat.com/api/web/search/detailSearch/v2";
    private static final String PELIPLAT_LIBRARY_API_URL = "https://www.peliplat.com/api/web/mediaList/library/listMedias";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String POPULAR_MOVIES_FILTER_ID = "1901930552914399274";

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> searchMoviesByQuery(String query, String language) {
        try {
            // 构建API请求URL
            String url = buildSearchUrl(query, 1, language);
            logger.info("Searching movies with URL: {}", url);

            // 发送请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应
                return parseMovieResults(response.getBody());
            } else {
                logger.error("Failed to search movies, status: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Error searching movies by query: {}", query, e);
            return Collections.emptyList();
        }
    }

    @Override
    public MovieListResponseVo searchMoviesByQueryForVo(String query, String language) {
        try {
            // 构建API请求URL
            String url = buildSearchUrl(query, 1, language);
            logger.info("Searching movies with URL: {}", url);

            // 直接将响应转换为SearchResultVo对象
            ResponseEntity<SearchResultVo> response = restTemplate.getForEntity(url, SearchResultVo.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 将SearchResultVo转换为MovieListResponseVo
                return convertSearchResultToMovieList(response.getBody());
            } else {
                logger.error("Failed to search movies, status: {}", response.getStatusCode());
                return createEmptyResponse();
            }
        } catch (Exception e) {
            logger.error("Error searching movies by query: {}", query, e);
            return createEmptyResponse();
        }
    }

    @Override
    public MovieListResponseVo searchMoviesByYearAndGenreForVo(String year, String genre, String language) {
        try {
            // 构建库列表API的URL
            String url = PELIPLAT_LIBRARY_API_URL;
            logger.info("Searching movies by year and genre with URL: {}", url);

            // 构建请求头
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            // 构建请求参数
            MultiValueMap<String, String> requestParams = new org.springframework.util.LinkedMultiValueMap<>();

            // 添加类型筛选 - 支持逗号分隔的多个genre
            if (genre != null && !genre.isEmpty()) {
                // 按逗号分隔多个genre
                String[] genres = genre.split(",");
                List<String> genreIds = new ArrayList<>();

                // 获取每个genre的ID
                for (String singleGenre : genres) {
                    String trimmedGenre = singleGenre.trim();
                    if (!trimmedGenre.isEmpty()) {
                        String genreId = MovieGenreConstants.getGenreIdByDisplayName(trimmedGenre);
                        if (genreId != null && !genreId.isEmpty()) {
                            genreIds.add(genreId);
                            logger.info("添加电影类型筛选: {} -> ID: {}", trimmedGenre, genreId);
                        } else {
                            logger.warn("未找到电影类型: {} 对应的ID", trimmedGenre);
                        }
                    }
                }

                // 将所有ID用逗号连接
                if (!genreIds.isEmpty()) {
                    String filterIds = String.join(",", genreIds);
                    requestParams.add("filterIds", filterIds);
                    logger.info("最终电影类型筛选IDs: {}", filterIds);
                } else {
                    requestParams.add("filterIds", "");
                    logger.warn("所有提供的电影类型均未找到对应ID: {}", genre);
                }
            } else {
                requestParams.add("filterIds", "");
            }

            requestParams.add("language", language);
            requestParams.add("client", "web");
            requestParams.add("watched", "false");
            requestParams.add("whereToWatch", "false");
            requestParams.add("targetUid", "");
            requestParams.add("filterMediaType", "1"); // 1表示电影
            requestParams.add("sort", "3");
            requestParams.add("sortMode", "1");
            requestParams.add("queryMode", "2");
            requestParams.add("pageId", "1");
            requestParams.add("pageSize", String.valueOf(DEFAULT_PAGE_SIZE));

            // 添加年份筛选
            if (year != null && !year.isEmpty()) {
                requestParams.add("filterYear", "[" + year + ":" + year + "]");
            } else {
                requestParams.add("filterYear", "");
            }

            requestParams.add("filterRegion", "");
            requestParams.add("filterRuntime", "");

            // 创建HttpEntity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestParams, headers);

            // 直接将响应转换为MovieListResponseVo对象
            ResponseEntity<MovieListResponseVo> response = restTemplate.postForEntity(url, requestEntity,
                    MovieListResponseVo.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                logger.error("Failed to search movies by year and genre, status: {}", response.getStatusCode());
                return createEmptyResponse();
            }
        } catch (Exception e) {
            logger.error("Error searching movies by year: {} and genre: {}", year, genre, e);
            return createEmptyResponse();
        }
    }

    @Override
    public MovieListResponseVo getPopularMoviesThisWeekForVo(String language) {
        try {
            // 构建库列表API的URL
            String url = PELIPLAT_LIBRARY_API_URL;
            logger.info("Fetching popular movies this week with URL: {}", url);

            // 构建请求头
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

            // 构建请求参数
            MultiValueMap<String, String> requestParams = new org.springframework.util.LinkedMultiValueMap<>();
            requestParams.add("filterIds", POPULAR_MOVIES_FILTER_ID); // 本周热门电影的特定ID
            requestParams.add("language", language);
            requestParams.add("client", "web");
            requestParams.add("watched", "false");
            requestParams.add("whereToWatch", "false");
            requestParams.add("filterMediaType", "1"); // 1表示电影
            requestParams.add("filterYear", "");
            requestParams.add("filterRegion", "");
            requestParams.add("filterRuntime", "");
            requestParams.add("sort", "3");
            requestParams.add("sortMode", "1");
            requestParams.add("queryMode", "2");
            requestParams.add("pageId", "1"); // 默认第1页
            requestParams.add("pageSize", String.valueOf(DEFAULT_PAGE_SIZE)); // 默认20条

            // 创建HttpEntity
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestParams, headers);

            // 直接将响应转换为MovieListResponseVo对象
            ResponseEntity<MovieListResponseVo> response = restTemplate.postForEntity(url, requestEntity,
                    MovieListResponseVo.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                logger.error("Failed to fetch popular movies, status: {}", response.getStatusCode());
                return createEmptyResponse();
            }
        } catch (Exception e) {
            logger.error("Error fetching popular movies this week", e);
            return createEmptyResponse();
        }
    }

    @Override
    public MovieListResponseVo concurrentSearchMoviesForVo(List<String> queries, String language) {
        if (CollectionUtils.isEmpty(queries)) {
            return createEmptyResponse();
        }

        logger.info("并发搜索电影列表，查询条件：{}, 语言：{}", queries, language);

        try {
            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(queries.size(), 10));

            // 为每个查询创建异步任务，直接在API调用时指定只获取一部电影
            List<CompletableFuture<MovieDetailVo>> futures = queries.stream()
                    .map(query -> CompletableFuture.supplyAsync(() -> {
                        try {
                            // 查询时pageSize设为1，直接从源头减少传输量
                            String url = buildSearchUrl(query, 1, language, 1);
                            logger.info("并发搜索单部电影，URL: {}", url);

                            // 直接将响应转换为SearchResultVo对象
                            ResponseEntity<SearchResultVo> response = restTemplate.getForEntity(url,
                                    SearchResultVo.class);

                            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                SearchResultVo searchResult = response.getBody();
                                // 转换为MovieDetailVo
                                if (searchResult.getResult() != null &&
                                        searchResult.getResult().getList() != null &&
                                        !searchResult.getResult().getList().isEmpty()) {
                                    SearchResultVo.SearchItem item = searchResult.getResult().getList().get(0);
                                    if (item.getDetails() != null) {
                                        MovieDetailVo movie = convertMediaDetailToMovie(item.getDetails());
                                        movie.setRelateType(item.getRelateType());
                                        return movie;
                                    }
                                }
                            }
                            return null;
                        } catch (Exception e) {
                            logger.error("并发搜索电影出错，查询条件：{}, 语言：{}", query, language, e);
                            return null;
                        }
                    }, executor))
                    .collect(Collectors.toList());

            // 等待所有异步任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            // 收集所有结果
            allFutures.join();

            // 合并结果
            MovieListResponseVo mergedResult = new MovieListResponseVo();
            mergedResult.setRetCode(200);
            mergedResult.setMessage("并发搜索结果合并 - 每个查询获取一部电影");

            List<MovieDetailVo> allMovies = new ArrayList<>();

            for (CompletableFuture<MovieDetailVo> future : futures) {
                MovieDetailVo movie = future.get();
                if (movie != null) {
                    allMovies.add(movie);
                }
            }

            // 去重（按ppId去重）
            Map<String, MovieDetailVo> uniqueMoviesMap = new HashMap<>();
            for (MovieDetailVo movie : allMovies) {
                if (movie.getPpId() != null && !uniqueMoviesMap.containsKey(movie.getPpId())) {
                    uniqueMoviesMap.put(movie.getPpId(), movie);
                }
            }

            List<MovieDetailVo> uniqueMovies = new ArrayList<>(uniqueMoviesMap.values());

            mergedResult.setResult(uniqueMovies);
            mergedResult.setCount(uniqueMovies.size());
            mergedResult.setTotalCount(uniqueMovies.size());
            mergedResult.setTotalPage(1);

            // 关闭线程池
            executor.shutdown();

            logger.info("并发搜索电影完成，共获取到 {} 部电影", mergedResult.getCount());
            return mergedResult;

        } catch (Exception e) {
            logger.error("并发搜索电影出现异常，查询条件：{}, 语言：{}", queries, language, e);
            return createEmptyResponse();
        }
    }

    /**
     * 构建PeliPlat搜索API的URL
     */
    private String buildSearchUrl(String query, int pageId, String language) {
        return buildSearchUrl(query, pageId, language, DEFAULT_PAGE_SIZE);
    }

    /**
     * 构建PeliPlat搜索API的URL，支持指定pageSize
     */
    private String buildSearchUrl(String query, int pageId, String language, int pageSize) {
        try {
            return UriComponentsBuilder.fromHttpUrl(PELIPLAT_API_BASE_URL)
                    .queryParam("languageCode", language)
                    .queryParam("pageSize", pageSize)
                    .queryParam("client", "web")
                    .queryParam("keyword", URLEncoder.encode(query, StandardCharsets.UTF_8.name()))
                    .queryParam("pageId", pageId)
                    .queryParam("mark", "movies,series")
                    .build()
                    .toUriString();
        } catch (Exception e) {
            logger.error("Error building search URL", e);
            return PELIPLAT_API_BASE_URL;
        }
    }

    /**
     * 解析API响应为电影ID列表
     */
    private List<String> parseMovieResults(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resultList = rootNode.path("result").path("list");

            return StreamSupport.stream(resultList.spliterator(), false)
                    .map(item -> {
                        JsonNode details = item.path("details");
                        String title = details.path("title").asText();
                        String ppId = details.path("ppId").asText();
                        return title + " (" + ppId + ")";
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error parsing search results", e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析API响应为MediaBaseVo对象列表
     */
    private List<MediaBaseVo> parseMovieDetailsToMediaBaseVo(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resultList = rootNode.path("result").path("list");

            List<MediaBaseVo> mediaList = new ArrayList<>();

            for (JsonNode item : resultList) {
                JsonNode details = item.path("details");

                // 创建一个简化版的MediaBaseVo对象
                MediaBaseVo media = new MediaBaseVo();
                media.setPpId(details.path("ppId").asText(""));
                media.setTitle(details.path("title").asText(""));
                media.setOriginalTitle(details.path("originalTitle").asText(""));
                media.setPublicYear(details.path("publicYear").asText(""));
                media.setGenres(details.path("genres").asText(""));
                media.setPlot(details.path("plot").asText(""));

                mediaList.add(media);
            }

            return mediaList;
        } catch (IOException e) {
            logger.error("Error parsing movie details", e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析库API响应为电影列表
     */
    private List<String> parseLibraryResults(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resultArray = rootNode.path("result");

            if (resultArray.isMissingNode() || !resultArray.isArray()) {
                logger.error("Invalid response format: result array not found");
                return Collections.emptyList();
            }

            return StreamSupport.stream(resultArray.spliterator(), false)
                    .map(item -> {
                        String title = item.path("title").asText("");
                        String ppId = item.path("ppId").asText("");
                        int year = item.path("publicationYear").asInt(0);
                        String genres = item.path("genres").asText("");

                        StringBuilder sb = new StringBuilder();
                        sb.append(title);
                        if (year > 0) {
                            sb.append(" (").append(year).append(")");
                        }
                        if (!genres.isEmpty()) {
                            sb.append(" - ").append(genres);
                        }
                        sb.append(" [").append(ppId).append("]");

                        return sb.toString();
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error parsing library search results", e);
            return Collections.emptyList();
        }
    }

    /**
     * 创建空的响应对象
     */
    private MovieListResponseVo createEmptyResponse() {
        MovieListResponseVo responseVo = new MovieListResponseVo();
        responseVo.setRetCode(200);
        responseVo.setMessage(null);
        responseVo.setShow(null);
        responseVo.setTotalPage(0);
        responseVo.setTotalCount(0);
        responseVo.setCount(0);
        responseVo.setResult(new ArrayList<>());
        return responseVo;
    }

    /**
     * 将SearchResultVo转换为MovieListResponseVo
     */
    private MovieListResponseVo convertSearchResultToMovieList(SearchResultVo searchResult) {
        MovieListResponseVo responseVo = new MovieListResponseVo();
        responseVo.setRetCode(200);
        responseVo.setMessage(null);
        responseVo.setShow(null);

        List<MovieDetailVo> movieList = new ArrayList<>();

        if (searchResult.getResult() != null && searchResult.getResult().getList() != null) {
            List<SearchResultVo.SearchItem> items = searchResult.getResult().getList();

            for (SearchResultVo.SearchItem item : items) {
                if (item.getDetails() != null) {
                    // 转换MediaDetailVo为MovieDetailVo
                    MovieDetailVo movie = convertMediaDetailToMovie(item.getDetails());
                    movie.setRelateType(item.getRelateType());
                    movieList.add(movie);
                }
            }
        }

        responseVo.setResult(movieList);
        responseVo.setCount(movieList.size());
        responseVo.setTotalCount(movieList.size());
        responseVo.setTotalPage(1);

        return responseVo;
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

        // 设置投票数（如果MediaDetailVo中没有这个属性，设为0）
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

        // 设置类型ID（MediaDetailVo中可能没有这个属性）
        movie.setGenreIds(new ArrayList<>());

        movie.setSeason(0);
        movie.setEpisode(0);
        movie.setReleaseDate(mediaDetail.getReleaseDate());
        movie.setReleaseDateInt(0); // 可以通过解析releaseDate生成
        movie.setSummary(mediaDetail.getPlot());
        movie.setInWatchlist(false);
        movie.setTotalGross(null);
        movie.setPlotKeywords(mediaDetail.getPlotKeywords());
        movie.setLanguageCodes(mediaDetail.getLanguages());

        // 复制导演和演员信息
        movie.setDirectors(mediaDetail.getDirectors());
        movie.setActors(mediaDetail.getActors());

        return movie;
    }
}