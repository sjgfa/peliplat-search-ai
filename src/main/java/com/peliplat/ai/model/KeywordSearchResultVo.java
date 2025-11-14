package com.peliplat.ai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 关键词搜索API响应VO
 */
public class KeywordSearchResultVo {

    @JsonProperty("result")
    private List<MovieDetailVo> result;

    @JsonProperty("retCode")
    private Integer retCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("totalCount")
    private Integer totalCount;

    public List<MovieDetailVo> getResult() {
        return result;
    }

    public void setResult(List<MovieDetailVo> result) {
        this.result = result;
    }

    public Integer getRetCode() {
        return retCode;
    }

    public void setRetCode(Integer retCode) {
        this.retCode = retCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
