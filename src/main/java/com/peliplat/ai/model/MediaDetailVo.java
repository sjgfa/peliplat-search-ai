package com.peliplat.ai.model;

import java.util.List;

/**
 * 媒体详情VO类，用于存储电影和电视剧的详细信息
 */
public class MediaDetailVo {
    private String typeForId;
    private Long mediaId;
    private Integer mediaType;
    private String imdbId;
    private String ppId;
    private String rating;
    private String title;
    private String poster;
    private Integer publicYear;
    private String certificate;
    private Integer runTime;
    private String geners;
    private String genres;
    private String plot;
    private String releaseDate;
    private String originalTitle;
    private Boolean canRating;
    private Integer approveStatus;
    private Integer watchStatus;
    private Integer watchListStatus;
    private List<String> plotKeywords;
    private String languages;
    private List<PersonVo> directors;
    private List<PersonVo> actors;
    private List<String> backdrops;
    private List<String> newBackdrops;
    private String popularity;
    private Integer status;

    public String getTypeForId() {
        return typeForId;
    }

    public void setTypeForId(String typeForId) {
        this.typeForId = typeForId;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public Integer getMediaType() {
        return mediaType;
    }

    public void setMediaType(Integer mediaType) {
        this.mediaType = mediaType;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getPpId() {
        return ppId;
    }

    public void setPpId(String ppId) {
        this.ppId = ppId;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public Integer getPublicYear() {
        return publicYear;
    }

    public void setPublicYear(Integer publicYear) {
        this.publicYear = publicYear;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public Integer getRunTime() {
        return runTime;
    }

    public void setRunTime(Integer runTime) {
        this.runTime = runTime;
    }

    public String getGeners() {
        return geners;
    }

    public void setGeners(String geners) {
        this.geners = geners;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public Boolean getCanRating() {
        return canRating;
    }

    public void setCanRating(Boolean canRating) {
        this.canRating = canRating;
    }

    public Integer getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(Integer approveStatus) {
        this.approveStatus = approveStatus;
    }

    public Integer getWatchStatus() {
        return watchStatus;
    }

    public void setWatchStatus(Integer watchStatus) {
        this.watchStatus = watchStatus;
    }

    public Integer getWatchListStatus() {
        return watchListStatus;
    }

    public void setWatchListStatus(Integer watchListStatus) {
        this.watchListStatus = watchListStatus;
    }

    public List<String> getPlotKeywords() {
        return plotKeywords;
    }

    public void setPlotKeywords(List<String> plotKeywords) {
        this.plotKeywords = plotKeywords;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public List<PersonVo> getDirectors() {
        return directors;
    }

    public void setDirectors(List<PersonVo> directors) {
        this.directors = directors;
    }

    public List<PersonVo> getActors() {
        return actors;
    }

    public void setActors(List<PersonVo> actors) {
        this.actors = actors;
    }

    public List<String> getBackdrops() {
        return backdrops;
    }

    public void setBackdrops(List<String> backdrops) {
        this.backdrops = backdrops;
    }

    public List<String> getNewBackdrops() {
        return newBackdrops;
    }

    public void setNewBackdrops(List<String> newBackdrops) {
        this.newBackdrops = newBackdrops;
    }

    public String getPopularity() {
        return popularity;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}