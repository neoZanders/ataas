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
import com.chalmers.atas.domain.user.CurrentUser;
import com.chalmers.atas.domain.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementApplicationService {

    private final AnnouncementService announcementService;
    private final CourseService courseService;
    private final CRCourseAssignmentService crCourseAssignmentService;

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
                        .flatMap(ignored ->
                                announcementService.createAnnouncement(
                                        course,
                                        request.getTitle(),
                                        request.getBody(),
                                        request.getSendByEmail()
                                ).map(AnnouncementResponse::of)
                        ));
    }

    private Result<Void> assertUserCanViewAnnouncements(Course course, User user) {
        if (user.getUserType().equals(User.UserType.CR)) {
            if (crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                return Result.ok();
            }
            return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
        }

        // TODO: implement TA course assignment checks
        return Result.error(ErrorCode.FORBIDDEN.toError("TA access not implemented yet"));
    }

    private Result<Void> assertUserCanCreateAnnouncements(Course course, User user) {
        if (user.getUserType().equals(User.UserType.CR)) {
            if (crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                return Result.ok();
            }
            return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
        }

        // TODO: implement TA course assignment checks and canTACreateAnnouncements flag
        return Result.error(ErrorCode.FORBIDDEN.toError("TA access not implemented yet"));
    }

}
