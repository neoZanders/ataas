package com.chalmers.atas.api.course;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionHandler;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.course.CourseService;
import com.chalmers.atas.domain.coursesession.CourseSessionService;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentService;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentService;
import com.chalmers.atas.domain.user.CurrentUser;
import com.chalmers.atas.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseApplicationService {

    private final CourseSessionService courseSessionService;
    private final CourseService courseService;
    private final CRCourseAssignmentService crCourseAssignmentService;
    private final TACourseAssignmentService taCourseAssignmentService;
    private final TransactionHandler transactionHandler;
    private final CourseRepository courseRepository;

    public Result<CourseResponse> createCourse(CreateCourseRequest request, CurrentUser currentUser) {
        return transactionHandler.executeInTransaction(() ->
                courseService.createCourse(
                        currentUser.getUser(),
                        request.getCourseCode(),
                        request.getDescription(),
                        request.getCanTASeeAllSchedules(),
                        request.getCanTACreateAnnouncements(),
                        request.getStartDate(),
                        request.getEndDate()
                ).flatMap(course ->
                        crCourseAssignmentService.createOwnerAssignment(
                                currentUser.getUser(),
                                course
                        ).map(ignored -> course)
                ).map(CourseResponse::of)
        );
    }

    public Result<List<CourseWithAssignmentStatusResponse>> getCourses(CurrentUser currentUser) {
        return courseService.getCourseAssignments(currentUser.getUser())
                .map(assignments ->
                        assignments.stream()
                                .map(assignment -> CourseWithAssignmentStatusResponse.of(
                                        assignment.getCourse(),
                                        assignment.getStatus()
                                ))
                                .toList()
                );
    }

    public Result<CourseResponse> archiveCourse(UUID courseId, CurrentUser currentUser) {
        return courseService.archiveCourse(courseId, currentUser.getUser()).map(CourseResponse::of);
    }

    public Result<Void> deleteCourse(UUID courseId, CurrentUser currentUser) {
        return courseService.deleteCourse(courseId, currentUser.getUser());
    }

    public Result<CourseResponse> updateCourse(UUID courseId, UpdateCourseRequest request, CurrentUser currentUser) {
        return courseService.updateCourse(
                courseId,
                currentUser.getUser(),
                request.getDescription(),
                request.getCanTASeeAllSchedules(),
                request.getCanTACreateAnnouncements()
        ).map(CourseResponse::of);
    }

    public Result<List<CourseSessionResponse>> getCourseSessions(UUID courseId, CurrentUser currentUser) {
        return courseService.getCourse(courseId).flatMap(course -> {
            User user = currentUser.getUser();
            if (user.getUserType().equals(User.UserType.CR)) {
                if (!crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                    return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
                }
                return courseSessionService.getCourseSessions(courseId).map(courseSessions ->
                        courseSessions.stream().map(CourseSessionResponse::of).toList());
            }

            if (user.getUserType().equals(User.UserType.TA)) {
                if (!taCourseAssignmentService.isUserTaOfCourse(user, course)) {
                    return Result.error(ErrorCode.USER_HAS_NOT_JOINED_COURSE.toError());
                }
                return courseSessionService.getCourseSessions(courseId).map(courseSessions ->
                        courseSessions.stream().map(CourseSessionResponse::of).toList());
            }

            return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
        });
    }


    public Result<CourseSessionResponse> createCourseSession(UUID courseId, CreateCourseSessionRequest request, CurrentUser currentUser) {
        return Result.ofOptional(courseRepository.findById(courseId), ErrorCode.COURSE_NOT_FOUND.toError()).flatMap(course -> {
            if (crCourseAssignmentService.isUserCrOfCourse(currentUser.getUser(), course)) {
                return courseSessionService.createCourseSession(
                        course,
                        request.getStartDateTime(),
                        request.getEndDateTime(),
                        request.getCourseSessionType(),
                        request.getMinTAs(),
                        request.getMaxTAs(),
                        request.getIsWeeklyRecurring()
                ).map(CourseSessionResponse::of);
            } else {
                return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
            }
        });
    }

    public Result<Void> deleteCourseSession(UUID courseId, UUID courseSessionId, CurrentUser currentUser) {
        return Result.ofOptional(courseRepository.findById(courseId), ErrorCode.COURSE_NOT_FOUND.toError()).flatMap(course -> {
            if (crCourseAssignmentService.isUserCrOfCourse(currentUser.getUser(), course)) {
                return courseSessionService.deleteCourseSession(courseSessionId);
            } else {
                return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
            }
        });
    }
}
