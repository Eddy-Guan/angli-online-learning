package com.angli.online.dto.request;

import java.util.List;

public class CourseApplyRequest {

  private String title;
  private String subject;
  private String grade;
  private Integer totalHours;
  private String description;
  private List<Chapter> chapters;

  public static class Chapter {
    private String title;
    private String description;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getSubject() { return subject; }
  public void setSubject(String subject) { this.subject = subject; }

  public String getGrade() { return grade; }
  public void setGrade(String grade) { this.grade = grade; }

  public Integer getTotalHours() { return totalHours; }
  public void setTotalHours(Integer totalHours) { this.totalHours = totalHours; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public List<Chapter> getChapters() { return chapters; }
  public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }
}