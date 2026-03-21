package com.chalmers.atas.domain.course;

import com.chalmers.atas.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course implements Serializable {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID courseId;

    @Column(nullable = false)
    private String courseCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus courseStatus;

    @Column(name = "can_ta_see_all_schedules", nullable = false)
    private boolean canTASeeAllSchedules;

    @Column(name = "can_ta_create_announcements", nullable = false)
    private boolean canTACreateAnnouncements;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    public enum CourseStatus {
        ACTIVE,
        ARCHIVED
    }

    public static Course of(
            String courseCode,
            User owner,
            String description,
            boolean canTASeeAllSchedules,
            boolean canTACreateAnnouncements,
            LocalDate startDate,
            LocalDate endDate) {
        Course course = new Course();
        course.courseCode = courseCode.toUpperCase();
        course.owner = owner;
        course.description = description;
        course.courseStatus = CourseStatus.ACTIVE;
        course.canTASeeAllSchedules = canTASeeAllSchedules;
        course.canTACreateAnnouncements = canTACreateAnnouncements;
        course.startDate = startDate;
        course.endDate = endDate;
        return course;
    }
}
