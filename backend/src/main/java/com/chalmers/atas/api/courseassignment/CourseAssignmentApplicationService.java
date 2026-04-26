package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.courseassignment.CourseAuthorizationService;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentRepository;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentService;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentRepository;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentService;
import com.chalmers.atas.domain.user.CurrentUser;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseAssignmentApplicationService {

    private final CourseRepository courseRepository;
    private final CourseAuthorizationService courseAuthorizationService;
    private final CRCourseAssignmentService crCourseAssignmentService;
    private final TACourseAssignmentService taCourseAssignmentService;
    private final CRCourseAssignmentRepository crCourseAssignmentRepository;
    private final TACourseAssignmentRepository taCourseAssignmentRepository;
    private final UserRepository userRepository;

    public Result<Void> joinCourse(UUID courseId, CurrentUser currentUser) {
        if (currentUser.getUser().getUserType().equals(User.UserType.CR)) {
                return resolveCourse(courseId)
                        .flatMap(course ->
                                        Result.ofOptional(
                                                crCourseAssignmentRepository.findByCrAndCourse(currentUser.getUser(), course),
                                                ErrorCode.COURSE_INVITE_NOT_FOUND.toError())
                        ).flatMap(crCourseAssignmentService::join);           
        }
        if (currentUser.getUser().getUserType().equals(User.UserType.TA)) {
                return resolveCourse(courseId)
                        .flatMap(course ->
                                        Result.ofOptional(
                                                taCourseAssignmentRepository.findByTaAndCourse(currentUser.getUser(), course),
                                                ErrorCode.COURSE_INVITE_NOT_FOUND.toError())
                        ).flatMap(taCourseAssignmentService::join); 
        }

        return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());

    }

    public Result<Void> inviteCR(UUID courseId, InviteCRRequest request, CurrentUser currentUser) {
        return courseAuthorizationService.assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                                Result.ofOptional(
                                        userRepository.findByEmail(request.getCrEmail()),
                                        ErrorCode.USER_NOT_FOUND.toError()
                                ).flatMap(user ->
                                        crCourseAssignmentService.createInviteAssignment(user, course)));
    }

    public Result<Void> inviteTA(UUID courseId, InviteTARequest request, CurrentUser currentUser){
        return courseAuthorizationService.assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                                Result.ofOptional(
                                        userRepository.findByEmail(request.getTaEmail()),
                                        ErrorCode.USER_NOT_FOUND.toError()
                                ).flatMap(user ->
                                        taCourseAssignmentService.createInviteAssignment(user, course)
                                )
                );
    }

    public Result<CourseAssignmentsResponse> getAssignments(UUID courseId, String username, CurrentUser currentUser, Sort sort) {
        Result<Course> courseResult;

        if (currentUser.getUser().getUserType().equals(User.UserType.CR)) {
            courseResult = courseAuthorizationService.assertUserIsCrOfCourse(courseId, currentUser.getUser());
        } else if (currentUser.getUser().getUserType().equals(User.UserType.TA)) {
            courseResult = courseAuthorizationService.assertUserIsTaOfCourse(courseId, currentUser.getUser());
        } else {
            courseResult = Result.error(ErrorCode.USER_NOT_ALLOWED_TO_VIEW_COURSE.toError());
        }

        return courseResult.flatMap(course ->
                        crCourseAssignmentService.getCourseAssignments(
                                course
                        ).flatMap(crCourseAssignments ->
                                taCourseAssignmentService.getCourseAssignments(
                                        course,
                                        Optional.ofNullable(username),
                                        sort
                                ).map(taCourseAssignments ->
                                                CourseAssignmentsResponse.of(
                                                        courseId,
                                                        crCourseAssignments,
                                                        taCourseAssignments
                                                )
                                )
                        )
                );
    }

    public Result<TACourseAssignmentResponse> getTAAssignment(UUID courseId, UUID taId, CurrentUser currentUser) {
        return Result.ofOptional(userRepository.findById(taId), ErrorCode.USER_NOT_FOUND.toError())
                .flatMap(ta -> {
                    if (currentUser.getUser().getUserType().equals(User.UserType.CR)) {
                        return courseAuthorizationService.assertUserIsCrOfCourse(courseId, currentUser.getUser())
                                .flatMap(course -> taCourseAssignmentService.getAssignment(ta, course));
                    } else if (taId.equals(currentUser.getUserId())) {
                        return courseAuthorizationService.assertUserIsTaOfCourse(courseId, ta)
                                .flatMap(course -> taCourseAssignmentService.getAssignment(ta, course));
                    }
                    return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
                }).map(TACourseAssignmentResponse::of);
    }

    public Result<TACourseAssignmentResponse> updateTAAssignment(
            UUID courseId,
            UUID taId,
            CurrentUser currentUser,
            UpdateTAAssignmentRequest request
    ){
        if (!currentUser.getUser().getUserType().equals(User.UserType.TA)) {
            return Result.error(ErrorCode.USER_NOT_TEACHING_ASSISTANT.toError());
        }

        if (!currentUser.getUser().getUserId().equals(taId)) {
            return Result.error(ErrorCode.USER_NOT_ALLOWED_TO_UPDATE_ASSIGNMENT.toError());
        }

        return resolveCourse(courseId).flatMap(course ->
                Result.ofOptional(
                        taCourseAssignmentRepository.findByTaAndCourse(currentUser.getUser(), course),
                        ErrorCode.USER_HAS_NOT_JOINED_COURSE.toError()
                ).flatMap(taCourseAssignment ->
                        taCourseAssignmentService.updateAssignment(
                                taCourseAssignment,
                                request.getMinHours(),
                                request.getMaxHours(),
                                request.getSessionTypePreference1(),
                                request.getSessionTypePreference2(),
                                request.getSessionTypePreference3(),
                                request.getSessionTypePreference4(),
                                request.getIsCompactSchedule()
                        ).map(TACourseAssignmentResponse::of)
                )
        );
    }
    
    public Result<Void> deleteCRAssignment(UUID courseId, UUID userId, CurrentUser currentUser) {
        return courseAuthorizationService.assertUserIsCrOfCourse(courseId, currentUser.getUser())
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

    public Result<Void> deleteTAAssignment(UUID courseId, UUID userId, CurrentUser currentUser){
        return courseAuthorizationService.assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                                Result.ofOptional(
                                        userRepository.findById(userId),
                                        ErrorCode.USER_NOT_FOUND.toError()
                                ).flatMap(ta ->
                                        Result.ofOptional(
                                                taCourseAssignmentRepository.findByTaAndCourse(ta, course), 
                                        ErrorCode.USER_HAS_NOT_JOINED_COURSE.toError())
                                )
                ).flatMap(taCourseAssignmentService::deleteAssignment);
    }


    private Result<Course> resolveCourse(UUID courseId) {
        return Result.ofOptional(
                courseRepository.findById(courseId),
                ErrorCode.COURSE_NOT_FOUND.toError()
        );
    }
}
