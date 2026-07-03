package com.sms.model;

import java.time.LocalDate;

/** Plain data holder for a row in the ATTENDANCE table, with display-only joins. */
public class AttendanceRecord {

    public enum Status { PRESENT, ABSENT }

    private int id;
    private int studentId;
    private int courseId;
    private LocalDate attendanceDate;
    private Status status;

    // Populated by joins for display in tables — not persisted directly.
    private String studentName;
    private String rollNumber;
    private String courseName;

    public AttendanceRecord() {
    }

    public AttendanceRecord(int studentId, int courseId, LocalDate attendanceDate, Status status) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
