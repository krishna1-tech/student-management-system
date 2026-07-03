package com.sms.model;

import java.time.LocalDate;

/** Plain data holder for a row in the STUDENTS table. */
public class Student {

    private int id;
    private String rollNumber;
    private String fullName;
    private String email;
    private String phone;
    private Integer courseId;     // nullable — student may not be assigned yet
    private String courseName;    // populated by joins, for display only
    private LocalDate dateJoined;

    public Student() {
    }

    public Student(String rollNumber, String fullName, String email, String phone,
                    Integer courseId, LocalDate dateJoined) {
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.courseId = courseId;
        this.dateJoined = dateJoined;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public LocalDate getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDate dateJoined) { this.dateJoined = dateJoined; }

    @Override
    public String toString() {
        return rollNumber + " — " + fullName;
    }
}
