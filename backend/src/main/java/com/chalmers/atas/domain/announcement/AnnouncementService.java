package com.chalmers.atas.domain.announcement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public Result<List<Announcement>> getAnnouncements(Course course) {
        return Result.ok(
                announcementRepository.findByCourseCourseIdOrderByCreatedAtDesc(course.getCourseId())
        );
    }

    @Transactional
    public TransactionalResult<Announcement> createAnnouncement(
            Course course,
            User owner,
            String title,
            String body,
            boolean sendByEmail
    ) {
        return TransactionalResult.ok(announcementRepository.save(Announcement.of(course, owner, title, body, sendByEmail)));
    }
}
