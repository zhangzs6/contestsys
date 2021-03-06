package com.code.model;

import java.math.BigDecimal;
import java.util.Map;

public class Problem {
    private Integer problemId;

    private String title;

    private String source;

    private String sampleProgram;

    private Integer timeLimit;

    private Integer memoryLimit;

    private Integer paperId;

    private BigDecimal score;

    private Integer pos;

    private Integer fkCourseId;

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getSampleProgram() {
        return sampleProgram;
    }

    public void setSampleProgram(String sampleProgram) {
        this.sampleProgram = sampleProgram == null ? null : sampleProgram.trim();
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Integer memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public Integer getFkCourseId() {
        return fkCourseId;
    }

    public void setFkCourseId(Integer fkCourseId) {
        this.fkCourseId = fkCourseId;
    }

    //格式是<1/in,url>  1表示组号  in表示输入输出  url是绝对路径
    private Map<String,String> fileMap;

    public Map<String, String> getFileMap() {
        return fileMap;
    }

    public void setFileMap(Map<String, String> fileMap) {
        this.fileMap = fileMap;
    }
}