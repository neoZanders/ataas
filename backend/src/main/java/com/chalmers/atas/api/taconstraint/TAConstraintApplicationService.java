package com.chalmers.atas.api.taconstraint;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.chalmers.atas.common.*;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import org.springframework.stereotype.Service;

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
    private final TransactionHandler transactionHandler;

    public Result<List<TAConstraintsResponse>> getCourseConstraints(UUID courseId, String username, CurrentUser currentUser){
        User user = currentUser.getUser();
        if (!user.getUserType().equals(User.UserType.CR)) {
            return Result.error(ErrorCode.USER_NOT_ALLOWED_FOR_COURSE_ACTION.toError());
        }

        return courseAuthorizationService.assertUserIsCrOfCourse(courseId, user)
                .flatMap(course ->
                        taCourseSessionConstraintService.getCourseConstraints(course, Optional.ofNullable(username))
                                .map(constraints -> {
                                    Map<User, List<TACourseSessionConstraint>> constraintsByTAs =
                                            constraints.stream().collect(Collectors.groupingBy(
                                                    constraint -> constraint.getTaCourseAssignment().getTa()
                                            ));

                                    return constraintsByTAs.entrySet().stream()
                                            .map(constraintsByTA ->
                                                    TAConstraintsResponse.of(
                                                        constraintsByTA.getKey(),
                                                        constraintsByTA.getValue()
                                                    )
                                            ).toList();
                                }));
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
                        taCourseSessionConstraintService.getTAConstraints(course, taId)
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

    public Result<List<TAConstraintResponse>> replaceTAConstraints(
            UUID courseId,
            ReplaceTAConstraintsRequest request,
            CurrentUser currentUser
    ) {
        User user = currentUser.getUser();

        if (!user.getUserType().equals(User.UserType.TA)) {
            return Result.error(ErrorCode.USER_NOT_TEACHING_ASSISTANT.toError());
        }

        return courseAuthorizationService.assertUserIsTaOfCourse(courseId, user)
                .flatMap(course ->
                        taCourseAssignmentService.getAssignment(user, course)
                                .flatMap(taCourseAssignment ->
                                        transactionHandler.executeInTransaction(() -> {
                                            Result<List<TACourseSessionConstraint>> existingConstraintsResult =
                                                    taCourseSessionConstraintService.getTAConstraints(course, currentUser.getUserId());

                                            if (!existingConstraintsResult.isSuccess()) {
                                                return TransactionalResult.rollbackFor(existingConstraintsResult.getError());
                                            }

                                            Map<UUID, TACourseSessionConstraint> remainingConstraints =
                                                    existingConstraintsResult.getData().stream()
                                                            .collect(Collectors.toMap(
                                                                    TACourseSessionConstraint::getTaCourseSessionConstraintId,
                                                                    Function.identity()
                                                            ));

                                            List<TAConstraintResponse> responses = new ArrayList<>();

                                            for (ReplaceTAConstraintRequest constraintRequest : request.getRequests()) {
                                                TransactionalResult<TACourseSessionConstraint> constraintResult;

                                                if (constraintRequest.getTaCourseConstraintId() == null) {
                                                    constraintResult = taCourseSessionConstraintService.createConstraint(
                                                            taCourseAssignment,
                                                            constraintRequest.getConstraintType(),
                                                            constraintRequest.getStartDateTime(),
                                                            constraintRequest.getEndDateTime(),
                                                            constraintRequest.getIsWeeklyRecurring()
                                                    );
                                                } else {
                                                    TACourseSessionConstraint existingConstraint =
                                                            remainingConstraints.remove(constraintRequest.getTaCourseConstraintId());

                                                    if (existingConstraint == null) {
                                                        return TransactionalResult.rollbackFor(
                                                                ErrorCode.TA_CONSTRAINT_NOT_FOUND.toError(
                                                                        "TA course session constraint with id="
                                                                                + constraintRequest.getTaCourseConstraintId()
                                                                                + " could not be found!"
                                                                )
                                                        );
                                                    }

                                                    constraintResult = taCourseSessionConstraintService.updateConstraint(
                                                            existingConstraint,
                                                            constraintRequest.getConstraintType(),
                                                            constraintRequest.getStartDateTime(),
                                                            constraintRequest.getEndDateTime(),
                                                            constraintRequest.getIsWeeklyRecurring()
                                                    );
                                                }

                                                if (!constraintResult.isSuccess()) {
                                                    return TransactionalResult.rollbackFor(constraintResult.getError());
                                                }

                                                responses.add(TAConstraintResponse.of(constraintResult.getData()));
                                            }

                                            for (TACourseSessionConstraint constraintToDelete : remainingConstraints.values()) {
                                                TransactionalResult<Void> deleteResult =
                                                        taCourseSessionConstraintService.deleteConstraint(constraintToDelete);

                                                if (!deleteResult.isSuccess()) {
                                                    return TransactionalResult.rollbackFor(deleteResult.getError());
                                                }
                                            }

                                            return TransactionalResult.ok(responses);
                                        })
                                )
                );
    }

    public Result<TAConstraintResponse> updateTAConstraint(
            UUID courseId,
            UUID taCourseSessionConstraintId,
            UpdateTAConstraintRequest request,
            CurrentUser currentUser
    ){
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
                    return taCourseSessionConstraintService.updateConstraint(
                            constraint,
                            request.getConstraintType(),
                            request.getStartDateTime(),
                            request.getEndDateTime(),
                            request.getIsWeeklyRecurring()
                    ).map(TAConstraintResponse::of);
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
