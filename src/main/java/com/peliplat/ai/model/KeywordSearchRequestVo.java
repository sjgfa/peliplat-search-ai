package com.peliplat.ai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 关键词搜索API请求VO
 */
public class KeywordSearchRequestVo {

    @JsonProperty("language")
    private String language;

    @JsonProperty("client")
    private String client;

    @JsonProperty("filterMediaType")
    private String filterMediaType;

    @JsonProperty("title")
    private String title;

    @JsonProperty("sortMode")
    private Integer sortMode;

    @JsonProperty("keywords")
    private List<String> keywords;

    @JsonProperty("pageId")
    private Integer pageId;

    @JsonProperty("pageSize")
    private Integer pageSize;

    @JsonProperty("watched")
    private Boolean watched;

    @JsonProperty("targetUid")
    private Long targetUid;

    public KeywordSearchRequestVo() {
        // 设置默认值
        this.language = "en";
        this.client = "web";
        this.filterMediaType = "1";
        this.title = "";
        this.sortMode = 1;
        this.pageId = 1;
        this.pageSize = 24;
        this.watched = false;
        this.targetUid = 11006353L;
    }

    // Getters and Setters
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getFilterMediaType() {
        return filterMediaType;
    }

    public void setFilterMediaType(String filterMediaType) {
        this.filterMediaType = filterMediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSortMode() {
        return sortMode;
    }

    public void setSortMode(Integer sortMode) {
        this.sortMode = sortMode;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getWatched() {
        return watched;
    }

    public void setWatched(Boolean watched) {
        this.watched = watched;
    }

    public Long getTargetUid() {
        return targetUid;
    }

    public void setTargetUid(Long targetUid) {
        this.targetUid = targetUid;
    }
}
