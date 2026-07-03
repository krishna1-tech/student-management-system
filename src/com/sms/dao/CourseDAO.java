package com.sms.dao;

import com.sms.db.DBConnection;
import com.sms.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data-access layer for the COURSES table. Every method uses prepared statements. */
public class CourseDAO {

    public boolean isCourseCodeTaken(String courseCode, int excludeId) throws SQLException {
        String sql = "SELECT course_id FROM courses WHERE LOWER(course_code) = LOWER(?) AND course_id <> ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int addCourse(Course c) throws SQLException {
        String sql = "INSERT INTO courses (course_code, course_name, instructor) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setString(3, c.getInstructor());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateCourse(Course c) throws SQLException {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, instructor = ? WHERE course_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setString(3, c.getInstructor());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCourse(int courseId) throws SQLException {
        // ON DELETE SET NULL / CASCADE in the schema handles dependent students and attendance rows.
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_code";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Course> searchCourses(String keyword) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses " +
                "WHERE course_code LIKE ? OR course_name LIKE ? OR instructor LIKE ? " +
                "ORDER BY course_code";
        String like = "%" + keyword + "%";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Count of students currently enrolled in a course — used for display only. */
    public int countEnrolled(int courseId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE course_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("course_id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setCourseName(rs.getString("course_name"));
        c.setInstructor(rs.getString("instructor"));
        return c;
    }
}
