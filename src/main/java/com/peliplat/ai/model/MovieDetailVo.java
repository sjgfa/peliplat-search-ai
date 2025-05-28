package com.peliplat.ai.model;

import java.util.List;

/**
 * 电影详细信息VO类
 */
public class MovieDetailVo {
    private Long mediaId;
    private String mmId;
    private String ppId;
    private String title;
    private String photoUrl;
    private Integer publicationYear;
    private String rating;
    private Integer voteCount;
    private Integer mediaType;
    private Integer rank;
    private Double popularity;
    private Integer watchStatus;
    private Integer watchListStatus;
    private String certificate;
    private Integer runTime;
    private String geners;
    private String genres;
    private List<Integer> genreIds;
    private Integer season;
    private Integer episode;
    private String releaseDate;
    private Integer releaseDateInt;
    private String summary;
    private Boolean inWatchlist;
    private String totalGross;
    private List<String> plotKeywords;
    private String languageCodes;
    private List<PersonVo> directors;
    private List<PersonVo> actors;
    private String job;
    private String role;
    private String relateType;

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public String getMmId() {
        return mmId;
    }

    public void setMmId(String mmId) {
        this.mmId = mmId;
    }

    public String getPpId() {
        return ppId;
    }

    public void setPpId(String ppId) {
        this.ppId = ppId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Integer getMediaType() {
        return mediaType;
    }

    public void setMediaType(Integer mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
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

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getEpisode() {
        return episode;
    }

    public void setEpisode(Integer episode) {
        this.episode = episode;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getReleaseDateInt() {
        return releaseDateInt;
    }

    public void setReleaseDateInt(Integer releaseDateInt) {
        this.releaseDateInt = releaseDateInt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Boolean getInWatchlist() {
        return inWatchlist;
    }

    public void setInWatchlist(Boolean inWatchlist) {
        this.inWatchlist = inWatchlist;
    }

    public String getTotalGross() {
        return totalGross;
    }

    public void setTotalGross(String totalGross) {
        this.totalGross = totalGross;
    }

    public List<String> getPlotKeywords() {
        return plotKeywords;
    }

    public void setPlotKeywords(List<String> plotKeywords) {
        this.plotKeywords = plotKeywords;
    }

    public String getLanguageCodes() {
        return languageCodes;
    }

    public void setLanguageCodes(String languageCodes) {
        this.languageCodes = languageCodes;
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

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRelateType() {
        return relateType;
    }

    public void setRelateType(String relateType) {
        this.relateType = relateType;
    }
}