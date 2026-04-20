package com.chalmers.atas.api.schedule;

import com.chalmers.atas.algorithm.AlgorithmService;
import com.chalmers.atas.algorithm.AlgorithmType;
import com.chalmers.atas.algorithm.model.AlgorithmHardSessionConstraint;
import com.chalmers.atas.algorithm.model.AlgorithmRequest;
import com.chalmers.atas.algorithm.model.AlgorithmResult;
import com.chalmers.atas.algorithm.model.AlgorithmSession;
import com.chalmers.atas.algorithm.model.AlgorithmSoftSessionConstraint;
import com.chalmers.atas.algorithm.model.AlgorithmTA;
import com.chalmers.atas.algorithm.model.AlgorithmTimeInterval;
import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionHandler;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.coursesession.CourseSessionService;
import com.chalmers.atas.domain.schedule.Schedule;
import com.chalmers.atas.domain.schedule.ScheduleService;
import com.chalmers.atas.domain.schedulesessionallocation.ScheduleSessionAllocation;
import com.chalmers.atas.domain.schedulesessionallocation.ScheduleSessionAllocationService;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentService;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraintService;
import com.chalmers.atas.domain.user.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScheduleApplicationService {

    private final List<AlgorithmService> algorithmServices;
    private final AlgorithmType algorithmType;
    private final int softConstraintWeight;
    private final CourseSessionService courseSessionService;
    private final ScheduleService scheduleService;
    private final ScheduleSessionAllocationService scheduleSessionAllocationService;
    private final TACourseAssignmentService taCourseAssignmentService;
    private final TACourseSessionConstraintService taCourseSessionConstraintService;
    private final TransactionHandler transactionHandler;

    public ScheduleApplicationService(
            List<AlgorithmService> algorithmServices,
            @Value("${app.alg.type:NAIVE_CHOCO}") AlgorithmType algorithmType,
            @Value("${app.alg.soft-constraint-weight:1}") int softConstraintWeight,
            CourseSessionService courseSessionService,
            ScheduleService scheduleService,
            ScheduleSessionAllocationService scheduleSessionAllocationService,
            TACourseAssignmentService taCourseAssignmentService,
            TACourseSessionConstraintService taCourseSessionConstraintService,
            TransactionHandler transactionHandler
    ) {
        this.algorithmServices = algorithmServices;
        this.algorithmType = algorithmType;
        this.softConstraintWeight = softConstraintWeight;
        this.courseSessionService = courseSessionService;
        this.scheduleService = scheduleService;
        this.scheduleSessionAllocationService = scheduleSessionAllocationService;
        this.taCourseAssignmentService = taCourseAssignmentService;
        this.taCourseSessionConstraintService = taCourseSessionConstraintService;
        this.transactionHandler = transactionHandler;
    }

    public Result<ScheduleResponse> createSchedule(
            UUID courseId,
            CurrentUser currentUser) {
        AlgorithmService algorithmService = resolveChocoAlgorithmService();
        if (algorithmService == null) {
            return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.toError("No Choco algorithm service configured"));
        }

        return transactionHandler.executeInTransaction(() ->
                scheduleService.createSchedule(courseId, currentUser.getUser())
                        .flatMap(schedule -> courseSessionService.getCourseSessions(courseId)
                                .flatMap(courseSessions -> {
                                    if (courseSessions.isEmpty()) {
                                        return Result.error(ErrorCode.BAD_REQUEST.toError("Course has no course sessions"));
                                    }

                                    return taCourseAssignmentService.getCourseAssignments(schedule.getCourse())
                                            .flatMap(assignments -> {
                                                List<TACourseAssignment> joinedAssignments = assignments.stream()
                                                        .filter(assignment -> assignment.getStatus() == CourseAssignmentStatus.JOINED)
                                                        .toList();
                                                if (joinedAssignments.isEmpty()) {
                                                    return Result.error(ErrorCode.BAD_REQUEST.toError(
                                                            "Course has no joined TA course assignments"));
                                                }

                                                return validateHourBudgets(joinedAssignments)
                                                        .flatMap(ignored -> taCourseSessionConstraintService.getCourseConstraints(schedule.getCourse())
                                                                .flatMap(constraints -> algorithmService
                                                                        .runAlgorithm(toAlgorithmRequest(
                                                                                courseSessions,
                                                                                joinedAssignments,
                                                                                constraints
                                                                        ))
                                                                        .flatMap(result -> saveAllocationsAndBuildResponse(
                                                                                courseId,
                                                                                currentUser,
                                                                                schedule,
                                                                                courseSessions,
                                                                                joinedAssignments,
                                                                                result
                                                                        ))));
                                            });
                                }))
        );
    }

    public Result<ScheduleResponse> getSchedule(UUID courseId, CurrentUser currentUser) {
        return scheduleService.getSchedule(courseId, currentUser.getUser())
                .flatMap(schedules -> {
                    if (schedules.isEmpty()) {
                        return Result.error(ErrorCode.NOT_FOUND.toError("Schedule not found"));
                    }

                    Schedule schedule = schedules.getFirst();
                    return scheduleSessionAllocationService
                            .getAllocations(courseId, currentUser.getUser())
                            .map(allocations -> ScheduleResponse.of(
                                    schedule,
                                    allocations.stream().map(ScheduleSessionAllocationResponse::of).toList()
                            ));
                });
    }

    private AlgorithmService resolveChocoAlgorithmService() {
        return algorithmServices.stream()
                .filter(service -> service.getType() == algorithmType)
                .findFirst()
                .orElseGet(() -> algorithmServices.stream().findFirst().orElse(null));
    }

    private AlgorithmRequest toAlgorithmRequest(
            List<CourseSession> courseSessions,
            List<TACourseAssignment> assignments,
            List<TACourseSessionConstraint> constraints
    ) {
        return new AlgorithmRequest(
                courseSessions.stream().map(this::toAlgorithmSession).toList(),
                assignments.stream().map(this::toAlgorithmTA).toList(),
                constraints.stream()
                        .filter(constraint -> constraint.getConstraintType() == TACourseSessionConstraint.ConstraintType.HARD)
                        .map(this::toHardConstraint)
                        .toList(),
                constraints.stream()
                        .filter(constraint -> constraint.getConstraintType() == TACourseSessionConstraint.ConstraintType.SOFT)
                        .map(this::toSoftConstraint)
                        .toList()
        );
    }

    private AlgorithmSession toAlgorithmSession(CourseSession courseSession) {
        return new AlgorithmSession(
                courseSession.getCourseSessionId(),
                new AlgorithmTimeInterval(courseSession.getStartDateTime(), courseSession.getEndDateTime()),
                courseSession.getCourseSessionType(),
                courseSession.getMinTAs(),
                courseSession.getMaxTAs()
        );
    }

    private AlgorithmTA toAlgorithmTA(TACourseAssignment assignment) {
        return new AlgorithmTA(
                assignment.getTaCourseAssignmentId(),
                assignment.getMinHours(),
                assignment.getMaxHours(),
                orderedPreferences(assignment),
                Boolean.TRUE.equals(assignment.getIsCompactSchedule())
        );
    }

    private Result<Void> validateHourBudgets(List<TACourseAssignment> assignments) {
        for (TACourseAssignment assignment : assignments) {
            if (assignment.getMinHours() == null || assignment.getMaxHours() == null) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "TA course assignment is missing hour budget: " + assignment.getTaCourseAssignmentId()));
            }
        }
        return Result.ok();
    }

    private List<CourseSession.CourseSessionType> orderedPreferences(TACourseAssignment assignment) {
        List<CourseSession.CourseSessionType> ranked = Arrays.asList(
                assignment.getSessionTypePreference1(),
                assignment.getSessionTypePreference2(),
                assignment.getSessionTypePreference3(),
                assignment.getSessionTypePreference4()
        ).stream().filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());

        for (CourseSession.CourseSessionType type : CourseSession.CourseSessionType.values()) {
            if (!ranked.contains(type)) {
                ranked.add(type);
            }
        }
        return ranked;
    }

    private AlgorithmHardSessionConstraint toHardConstraint(TACourseSessionConstraint constraint) {
        return new AlgorithmHardSessionConstraint(
                constraint.getTaCourseAssignment().getTaCourseAssignmentId(),
                new AlgorithmTimeInterval(constraint.getStartDateTime(), constraint.getEndDateTime())
        );
    }

    private AlgorithmSoftSessionConstraint toSoftConstraint(TACourseSessionConstraint constraint) {
        return new AlgorithmSoftSessionConstraint(
                constraint.getTaCourseAssignment().getTaCourseAssignmentId(),
                new AlgorithmTimeInterval(constraint.getStartDateTime(), constraint.getEndDateTime()),
                softConstraintWeight
        );
    }

    private Result<ScheduleResponse> saveAllocationsAndBuildResponse(
            UUID courseId,
            CurrentUser currentUser,
            Schedule schedule,
            List<CourseSession> courseSessions,
            List<TACourseAssignment> assignments,
            AlgorithmResult algorithmResult
    ) {
        if (!algorithmResult.feasible()) {
            return Result.error(ErrorCode.SCHEDULE_INFEASIBLE.toError());
        }

        Map<UUID, CourseSession> courseSessionById = courseSessions.stream()
                .collect(Collectors.toMap(CourseSession::getCourseSessionId, Function.identity()));
        Map<UUID, TACourseAssignment> assignmentById = assignments.stream()
                .collect(Collectors.toMap(TACourseAssignment::getTaCourseAssignmentId, Function.identity()));

        List<ScheduleSessionAllocation> allocations = algorithmResult.allocations().stream()
                .flatMap(allocation -> allocation.taAssignmentIds().stream()
                        .map(taAssignmentId -> ScheduleSessionAllocation.of(
                                schedule,
                                courseSessionById.get(allocation.sessionId()),
                                assignmentById.get(taAssignmentId)
                        )))
                .toList();

        if (allocations.stream().anyMatch(
                allocation -> allocation.getCourseSession() == null || allocation.getTaCourseAssignment() == null
        )) {
            return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.toError(
                    "Algorithm output referenced unknown session or TA assignment"));
        }

        return scheduleSessionAllocationService
                .replaceAllocations(courseId, allocations, currentUser.getUser())
                .map(savedAllocations -> ScheduleResponse.of(
                        schedule,
                        savedAllocations.stream().map(ScheduleSessionAllocationResponse::of).toList()
                ));
    }
}
