package com.angli.online.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("course_applications")
public class CourseApplication {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long teacherId;
  private String title;
  private String subject;
  private String grade;
  private Integer totalHours;
  private String description;
  private String chaptersJson;
  private String status;
  private String rejectReason;
  private LocalDateTime appliedAt;
  private LocalDateTime reviewedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getTeacherId() { return teacherId; }
  public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

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

  public String getChaptersJson() { return chaptersJson; }
  public void setChaptersJson(String chaptersJson) { this.chaptersJson = chaptersJson; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public String getRejectReason() { return rejectReason; }
  public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

  public LocalDateTime getAppliedAt() { return appliedAt; }
  public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

  public LocalDateTime getReviewedAt() { return reviewedAt; }
  public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}