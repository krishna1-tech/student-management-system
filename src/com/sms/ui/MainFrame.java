package com.sms.ui;

import javax.swing.*;
import java.awt.*;

/** Top-level window: a tabbed interface over Students, Courses, and Attendance. */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);

        StudentPanel studentPanel = new StudentPanel();
        CoursePanel coursePanel = new CoursePanel();
        AttendancePanel attendancePanel = new AttendancePanel();

        // Keep the Students dropdown and Attendance dropdown in sync whenever
        // a course is added, renamed, or removed from the Courses tab.
        coursePanel.setOnChangedListener(v -> {
            studentPanel.onCoursesChanged();
            attendancePanel.onCoursesChanged();
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", studentPanel);
        tabs.addTab("Courses", coursePanel);
        tabs.addTab("Attendance", attendancePanel);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }
}
