package com.sms.dao;

import com.sms.db.DBConnection;
import com.sms.model.AttendanceRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data-access layer for the ATTENDANCE table. */
public class AttendanceDAO {

    /**
     * Inserts a new attendance entry, or updates the status if one already
     * exists for this student/course/date (enforced by the schema's unique key).
     */
    public void markAttendance(AttendanceRecord r) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, course_id, attendance_date, status) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getCourseId());
            ps.setDate(3, Date.valueOf(r.getAttendanceDate()));
            ps.setString(4, r.getStatus().name());
            ps.setString(5, r.getStatus().name());
            ps.executeUpdate();
        }
    }

    /** Returns existing attendance for a course on a given date, keyed by student. */
    public List<AttendanceRecord> getByCourseAndDate(int courseId, LocalDate date) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name, s.roll_number FROM attendance a " +
                "JOIN students s ON a.student_id = s.student_id " +
                "WHERE a.course_id = ? AND a.attendance_date = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, false));
                }
            }
        }
        return list;
    }

    /** Full attendance log, most recent first, joined with student and course names. */
    public List<AttendanceRecord> getLog(int limit) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name, s.roll_number, c.course_name FROM attendance a " +
                "JOIN students s ON a.student_id = s.student_id " +
                "JOIN courses c ON a.course_id = c.course_id " +
                "ORDER BY a.attendance_date DESC, s.roll_number ASC " +
                "LIMIT ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, true));
                }
            }
        }
        return list;
    }

    /** Percentage of PRESENT days for one student in one course, for quick reporting. */
    public double getAttendancePercentage(int studentId, int courseId) throws SQLException {
        String sql = "SELECT " +
                "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count, " +
                "COUNT(*) AS total_count " +
                "FROM attendance WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_count");
                    if (total == 0) return 0.0;
                    return (rs.getInt("present_count") * 100.0) / total;
                }
            }
        }
        return 0.0;
    }

    private AttendanceRecord mapRow(ResultSet rs, boolean withCourseName) throws SQLException {
        AttendanceRecord r = new AttendanceRecord();
        r.setId(rs.getInt("attendance_id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setCourseId(rs.getInt("course_id"));
        r.setAttendanceDate(rs.getDate("attendance_date").toLocalDate());
        r.setStatus(AttendanceRecord.Status.valueOf(rs.getString("status")));
        r.setStudentName(rs.getString("full_name"));
        r.setRollNumber(rs.getString("roll_number"));
        if (withCourseName) {
            r.setCourseName(rs.getString("course_name"));
        }
        return r;
    }
}
