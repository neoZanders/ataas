package com.chalmers.atas.api.announcement;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.announcement.AnnouncementService;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseService;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentService;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentService;
import com.chalmers.atas.domain.user.CurrentUser;
import com.chalmers.atas.domain.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementApplicationService {

    private final AnnouncementService announcementService;
    private final CourseService courseService;
    private final CRCourseAssignmentService crCourseAssignmentService;
    private final TACourseAssignmentService taCourseAssignmentService;

    public Result<List<AnnouncementResponse>> getAnnouncements(UUID courseId, CurrentUser currentUser){
        return courseService.getCourse(courseId).flatMap(course ->
                assertUserCanViewAnnouncements(course, currentUser.getUser())
                        .flatMap(ignored ->
                                announcementService.getAnnouncements(course)
                                        .map(announcements ->
                                                announcements.stream()
                                                        .map(AnnouncementResponse::of)
                                                        .toList()
                                        )));
    }

    public Result<AnnouncementResponse> createAnnouncement(UUID courseId, CreateAnnouncementRequest request, CurrentUser currentUser){
        return courseService.getCourse(courseId).flatMap(course ->
                assertUserCanCreateAnnouncements(course, currentUser.getUser())
                        .then(() ->
                                announcementService.createAnnouncement(
                                        course,
                                        currentUser.getUser(),
                                        request.getTitle(),
                                        request.getBody(),
                                        request.getSendByEmail()
                                )
                        ).map(AnnouncementResponse::of));
    }

    private Result<Void> assertUserCanViewAnnouncements(Course course, User user) {
        if (user.getUserType().equals(User.UserType.CR)) {
            if (crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                return Result.ok();
            }
            return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE);
        }

        if (user.getUserType().equals(User.UserType.TA)) {
            if (!taCourseAssignmentService.isUserTaOfCourse(user, course)) {
                return Result.error(ErrorCode.USER_HAS_NOT_JOINED_COURSE);
            }
            return Result.ok();
        }

        return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION);
    }

    private Result<Void> assertUserCanCreateAnnouncements(Course course, User user) {
        if (user.getUserType().equals(User.UserType.CR)) {
            if (crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                return Result.ok();
            }
            return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE);
        }

        if (user.getUserType().equals(User.UserType.TA)) {
            if (!taCourseAssignmentService.isUserTaOfCourse(user, course)) {
                return Result.error(ErrorCode.USER_HAS_NOT_JOINED_COURSE);
            }
            if (!course.isCanTACreateAnnouncements()) {
                return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION);
            }
            return Result.ok();
        }

        return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION);
    }

}
