package com.chalmers.atas.api.courseassignment;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentRepository;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentService;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentRepository;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentService;
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
        return assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                                Result.ofOptional(
                                        userRepository.findByEmail(request.getCrEmail()),
                                        ErrorCode.USER_NOT_FOUND.toError()
                                ).flatMap(user ->
                                        crCourseAssignmentService.createInviteAssignment(user, course)));
    }

    public Result<Void> inviteTA(UUID courseId, InviteTARequest request, CurrentUser currentUser){
        return assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                                Result.ofOptional(
                                        userRepository.findByEmail(request.getTaEmail()),
                                        ErrorCode.USER_NOT_FOUND.toError()
                                ).flatMap(user ->
                                        taCourseAssignmentService.createInviteAssignment(user, course)
                                )
                );
    }

    public Result<CourseAssignmentsResponse> getAssignments(UUID courseId, CurrentUser currentUser) {
        return assertUserIsCrOfCourse(courseId, currentUser.getUser())
                .flatMap(course ->
                        crCourseAssignmentService.getCourseAssignments(course)
                                .flatMap(crCourseAssignments ->
                                        taCourseAssignmentService.getCourseAssignment(course)
                                                .map(taCourseAssignments ->
                                                        CourseAssignmentsResponse.of(
                                                                courseId,
                                                                crCourseAssignments,
                                                                taCourseAssignments
                                                        )
                                                )
                                )
                );
    }

    
    public Result<Void> updateTAAssignment(UUID courseId, UUID taId, CurrentUser currentUser, UpdateTAAssignmentRequest request){
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
                ).flatMap(taCourseAssignment -> {
                    if (request.getMinHours() != null) {
                        taCourseAssignment.setMinHours(request.getMinHours());
                    }
                    if (request.getMaxHours() != null) {
                        taCourseAssignment.setMaxHours(request.getMaxHours());
                    }
                    if (request.getSessionTypePreference() != null) {
                        taCourseAssignment.setSessionTypePreference(request.getSessionTypePreference());
                    }
                    if (request.getIsCompactSchedule() != null) {
                        taCourseAssignment.setIsCompactSchedule(request.getIsCompactSchedule());
                    }
                    return taCourseAssignmentService.updateAssignment(taCourseAssignment);
                })
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

    public Result<Void> deleteTAAssignment(UUID courseId, UUID userId, CurrentUser currentUser){
        return assertUserIsCrOfCourse(courseId, currentUser.getUser())
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

    private Result<Course> assertUserIsCrOfCourse(UUID courseId, User user) {
        return resolveCourse(courseId).flatMap(course -> {
            if (!crCourseAssignmentService.isUserCrOfCourse(user, course)) {
                return Result.error(ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError());
            }
            return Result.ok(course);
        });
    }
}
