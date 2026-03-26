package com.chalmers.atas.domain.announcement;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Announcement implements Serializable{
    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID announcementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String announcement;

    @Column(nullable = false)
    private boolean sendByEmail;

    @Column(nullable = false)
    private Instant createdAt;

    public static Announcement of(Course course, User owner, String title, String body, boolean sendByEmail){
        Announcement announcement = new Announcement();
        announcement.course = course;
        announcement.owner = owner;
        announcement.title = title;
        announcement.announcement = body;
        announcement.sendByEmail = sendByEmail;
        announcement.createdAt = Instant.now();
        return announcement;
    }
}
