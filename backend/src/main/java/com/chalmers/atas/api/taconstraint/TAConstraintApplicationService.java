package com.chalmers.atas.api.taconstraint;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.courseassignment.CourseAuthorizationService;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentService;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraintService;
import com.chalmers.atas.domain.user.CurrentUser;
import com.chalmers.atas.domain.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TAConstraintApplicationService {

    private final TACourseSessionConstraintService taCourseSessionConstraintService;
    private final TACourseAssignmentService taCourseAssignmentService;
    private final CourseAuthorizationService courseAuthorizationService;

    public Result<List<TAConstraintResponse>> getCourseConstraints(UUID courseId, CurrentUser currentUser){
        User user = currentUser.getUser();
        if (!user.getUserType().equals(User.UserType.CR)) {
            return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
        }

        return courseAuthorizationService.assertUserIsCrOfCourse(courseId, user)
                .flatMap(course ->
                        taCourseSessionConstraintService.getCourseConstraints(courseId)
                                .map(constraints ->
                                        constraints.stream().map(TAConstraintResponse::of).toList()
                                ));
    }

    public Result<List<TAConstraintResponse>> getTAConstraints(UUID courseId, UUID taId, CurrentUser currentUser){
        User user = currentUser.getUser();
        if (!user.getUserType().equals(User.UserType.TA)) {
            return Result.error(ErrorCode.USER_NOT_TEACHING_ASSISTANT.toError());
        }

        if (!user.getUserId().equals(taId)) {
            return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
        }

        return courseAuthorizationService.assertUserIsTaOfCourse(courseId, user)
                .flatMap(course ->
                        taCourseSessionConstraintService.getTAConstraints(courseId, taId)
                                .map(constraints ->
                                        constraints.stream().map(TAConstraintResponse::of).toList()
                                ));
    }

    public Result<Void> createTAConstraint(UUID courseId, CreateTAConstraintRequest request, CurrentUser currentUser){
        User user = currentUser.getUser();
        if (!user.getUserType().equals(User.UserType.TA)) {
            return Result.error(ErrorCode.USER_NOT_TEACHING_ASSISTANT.toError());
        }

        return courseAuthorizationService.assertUserIsTaOfCourse(courseId, user)
                .flatMap(course ->
                        taCourseAssignmentService.getAssignment(user, course)
                                .flatMap(taCourseAssignment ->
                                        taCourseSessionConstraintService.createConstraint(
                                                taCourseAssignment,
                                                request.getConstraintType(),
                                                request.getStartDateTime(),
                                                request.getEndDateTime(),
                                                request.getIsWeeklyRecurring()
                                        ).map(ignored -> null)
                                ));
    }

    //TODO move the logic of setting fileds to domain service and return a response
    public Result<Void> updateTAConstraint(UUID courseId, UUID taCourseSessionConstraintId, UpdateTAConstraintRequest request, CurrentUser currentUser){
        User user = currentUser.getUser();
        if (!user.getUserType().equals(User.UserType.TA)) {
            return Result.error(ErrorCode.USER_NOT_TEACHING_ASSISTANT.toError());
        }

        return taCourseSessionConstraintService.getConstraint(taCourseSessionConstraintId)
                .flatMap(constraint -> {
                    if (!constraint.getTaCourseAssignment().getCourse().getCourseId().equals(courseId)) {
                        return Result.error(ErrorCode.TA_CONSTRAINT_NOT_FOUND.toError());
                    }
                    if (!constraint.getTaCourseAssignment().getTa().getUserId().equals(user.getUserId())) {
                        return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
                    }
                    if (request.getConstraintType() != null) {
                        constraint.setConstraintType(request.getConstraintType());
                    }
                    if (request.getStartDateTime() != null) {
                        constraint.setStartDateTime(request.getStartDateTime());
                    }
                    if (request.getEndDateTime() != null) {
                        constraint.setEndDateTime(request.getEndDateTime());
                    }
                    if (request.getIsWeeklyRecurring() != null) {
                        constraint.setWeeklyRecurring(request.getIsWeeklyRecurring());
                    }
                    return taCourseSessionConstraintService.updateConstraint(constraint).map(ignored -> null);
                });
    }

    public Result<Void> deleteTAConstraint(UUID courseId, UUID taCourseSessionConstraintId, CurrentUser currentUser){
        User user = currentUser.getUser();
        if (!user.getUserType().equals(User.UserType.TA)) {
            return Result.error(ErrorCode.USER_NOT_TEACHING_ASSISTANT.toError());
        }

        return taCourseSessionConstraintService.getConstraint(taCourseSessionConstraintId)
                .flatMap(constraint -> {
                    if (!constraint.getTaCourseAssignment().getCourse().getCourseId().equals(courseId)) {
                        return Result.error(ErrorCode.TA_CONSTRAINT_NOT_FOUND.toError());
                    }
                    if (!constraint.getTaCourseAssignment().getTa().getUserId().equals(user.getUserId())) {
                        return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
                    }
                    return taCourseSessionConstraintService.deleteConstraint(constraint);
                });
    }

}
