package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentRepository;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentService;
import com.chalmers.atas.domain.user.CurrentUser;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseAssignmentApplicationService {

    private final CourseRepository courseRepository;
    private final CRCourseAssignmentService crCourseAssignmentService;
    private final CRCourseAssignmentRepository crCourseAssignmentRepository;
    private final UserRepository userRepository;

    public Result<Void> joinCourse(UUID courseId, CurrentUser currentUser) {
        return resolveCourse(courseId)
                .flatMap(course ->
                                Result.ofOptional(
                                        crCourseAssignmentRepository.findByCrAndCourse(currentUser.getUser(), course),
                                        ErrorCode.COURSE_INVITE_NOT_FOUND.toError())
                ).flatMap(crCourseAssignmentService::join);
    }

    public Result<Void> inviteCR(UUID courseId, InviteCRRequest request, CurrentUser currentUser) {
        return assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                                Result.ofOptional(
                                        userRepository.findByEmail(request.getCrEmail()),
                                        ErrorCode.USER_NOT_FOUND.toError()
                                ).flatMap(user ->
                                        crCourseAssignmentService.createInviteAssignment(user, course)));
    }

    public Result<CourseAssignmentsResponse> getAssignments(UUID courseId, CurrentUser currentUser) {
        return assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                        crCourseAssignmentService.getCourseAssignments(course)
                                .map(crCourseAssignments ->
                                        CourseAssignmentsResponse.of(courseId, crCourseAssignments))
                );
    }

    public Result<Void> deleteCRAssignment(UUID courseId, UUID userId, CurrentUser currentUser) {
        return assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                                Result.ofOptional(
                                        userRepository.findById(userId),
                                        ErrorCode.USER_NOT_FOUND.toError()
                                ).flatMap(cr ->
                                        Result.ofOptional(
                                                crCourseAssignmentRepository.findByCrAndCourse(cr, course),
                                                ErrorCode.USER_HAS_NOT_JOINED_COURSE.toError()))
                ).flatMap(crCourseAssignmentService::deleteAssignment);
    }

    private Result<Course> resolveCourse(UUID courseId) {
        return Result.ofOptional(
                courseRepository.findById(courseId),
                ErrorCode.COURSE_NOT_FOUND.toError()
        );
    }

    private Result<Course> assertUserIsCrOfCourse(UUID courseId, User user) {
        return resolveCourse(courseId).flatMap(course -> {
            if (!crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
            }
            return Result.ok(course);
        });
    }
}
