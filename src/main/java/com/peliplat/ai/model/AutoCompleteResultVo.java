package com.peliplat.ai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 自动完成API响应VO
 */
public class AutoCompleteResultVo {

    @JsonProperty("result")
    private List<AutoCompleteItem> result;

    @JsonProperty("retCode")
    private Integer retCode;

    @JsonProperty("message")
    private String message;

    public List<AutoCompleteItem> getResult() {
        return result;
    }

    public void setResult(List<AutoCompleteItem> result) {
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

    /**
     * 自动完成结果项
     */
    public static class AutoCompleteItem {

        @JsonProperty("docId")
        private String docId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("score")
        private Double score;

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        /**
         * 判断是否是keyword类型
         */
        public boolean isKeyword() {
            return docId != null && docId.contains("-keyword");
        }
    }
}
