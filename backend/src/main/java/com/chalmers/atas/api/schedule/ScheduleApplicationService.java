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
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAuthorizationService;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScheduleApplicationService {

    private final List<AlgorithmService> algorithmServices;
    private final AlgorithmType algorithmType;
    private final int softConstraintWeight;
    private final CourseAuthorizationService courseAuthorizationService;
    private final CourseSessionService courseSessionService;
    private final ScheduleService scheduleService;
    private final ScheduleSessionAllocationService scheduleSessionAllocationService;
    private final TACourseAssignmentService taCourseAssignmentService;
    private final TACourseSessionConstraintService taCourseSessionConstraintService;

    public ScheduleApplicationService(
            List<AlgorithmService> algorithmServices,
            @Value("${app.alg.type:NAIVE_CHOCO}") AlgorithmType algorithmType,
            @Value("${app.alg.soft-constraint-weight:1}") int softConstraintWeight,
            CourseAuthorizationService courseAuthorizationService,
            CourseSessionService courseSessionService,
            ScheduleService scheduleService,
            ScheduleSessionAllocationService scheduleSessionAllocationService,
            TACourseAssignmentService taCourseAssignmentService,
            TACourseSessionConstraintService taCourseSessionConstraintService
    ) {
        this.algorithmServices = algorithmServices;
        this.algorithmType = algorithmType;
        this.softConstraintWeight = softConstraintWeight;
        this.courseAuthorizationService = courseAuthorizationService;
        this.courseSessionService = courseSessionService;
        this.scheduleService = scheduleService;
        this.scheduleSessionAllocationService = scheduleSessionAllocationService;
        this.taCourseAssignmentService = taCourseAssignmentService;
        this.taCourseSessionConstraintService = taCourseSessionConstraintService;
    }

    @Transactional
    public Result<ScheduleResponse> createSchedule(
            UUID courseId,
            CurrentUser currentUser) {
        AlgorithmService algorithmService = resolveChocoAlgorithmService();
        if (algorithmService == null) {
            return Result.error(ErrorCode.INTERNAL_SERVER_ERROR.toError("No Choco algorithm service configured"));
        }

        Result<Course> courseResult = courseAuthorizationService.assertUserIsCrOfCourse(courseId, currentUser.getUser());
        if (!courseResult.isSuccess()) {
            return Result.error(courseResult.getError());
        }

        Course course = courseResult.getData();
        Result<List<CourseSession>> courseSessionsResult = courseSessionService.getCourseSessions(courseId);
        if (!courseSessionsResult.isSuccess()) {
            return Result.error(courseSessionsResult.getError());
        }

        List<CourseSession> courseSessions = courseSessionsResult.getData();
        if (courseSessions.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST.toError("Course has no course sessions"));
        }

        Result<List<TACourseAssignment>> assignmentsResult = taCourseAssignmentService.getCourseAssignments(
                course,
                Optional.empty(),
                Sort.unsorted()
        );
        if (!assignmentsResult.isSuccess()) {
            return Result.error(assignmentsResult.getError());
        }

        List<TACourseAssignment> joinedAssignments = assignmentsResult.getData().stream()
                .filter(assignment -> assignment.getStatus() == CourseAssignmentStatus.JOINED)
                .toList();
        if (joinedAssignments.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST.toError("Course has no joined TA course assignments"));
        }

        Result<Void> budgetValidation = validateHourBudgets(joinedAssignments);
        if (!budgetValidation.isSuccess()) {
            return Result.error(budgetValidation.getError());
        }

        Result<List<TACourseSessionConstraint>> constraintsResult = taCourseSessionConstraintService.getCourseConstraints(
                course,
                Optional.empty()
        );
        if (!constraintsResult.isSuccess()) {
            return Result.error(constraintsResult.getError());
        }

        Set<UUID> joinedAssignmentIds = joinedAssignments.stream()
                .map(TACourseAssignment::getTaCourseAssignmentId)
                .collect(Collectors.toSet());
        List<TACourseSessionConstraint> filteredConstraints = constraintsResult.getData().stream()
                .filter(constraint -> joinedAssignmentIds.contains(
                        constraint.getTaCourseAssignment().getTaCourseAssignmentId()
                ))
                .toList();

        Result<AlgorithmResult> algorithmResult = algorithmService.runAlgorithm(toAlgorithmRequest(
                course,
                courseSessions,
                joinedAssignments,
                filteredConstraints
        ));
        if (!algorithmResult.isSuccess()) {
            return Result.error(algorithmResult.getError());
        }

        return saveAllocationsAndBuildResponse(
                courseId,
                currentUser,
                courseSessions,
                joinedAssignments,
                algorithmResult.getData()
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
            Course course,
            List<CourseSession> courseSessions,
            List<TACourseAssignment> assignments,
            List<TACourseSessionConstraint> constraints
    ) {
        return new AlgorithmRequest(
                courseSessions.stream().map(this::toAlgorithmSession).toList(),
                assignments.stream().map(this::toAlgorithmTA).toList(),
                toHardConstraints(constraints, course.getEndDate()),
                toSoftConstraints(constraints, course.getEndDate())
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

    private List<AlgorithmHardSessionConstraint> toHardConstraints(
            List<TACourseSessionConstraint> constraints,
            LocalDate recurringEndDate
    ) {
        return constraints.stream()
                .filter(constraint -> constraint.getConstraintType() == TACourseSessionConstraint.ConstraintType.HARD)
                .flatMap(constraint -> expandHardConstraints(constraint, recurringEndDate).stream())
                .toList();
    }

    private List<AlgorithmSoftSessionConstraint> toSoftConstraints(
            List<TACourseSessionConstraint> constraints,
            LocalDate recurringEndDate
    ) {
        return constraints.stream()
                .filter(constraint -> constraint.getConstraintType() == TACourseSessionConstraint.ConstraintType.SOFT)
                .flatMap(constraint -> expandSoftConstraints(constraint, recurringEndDate).stream())
                .toList();
    }

    private List<AlgorithmHardSessionConstraint> expandHardConstraints(
            TACourseSessionConstraint constraint,
            LocalDate recurringEndDate
    ) {
        if (!constraint.isWeeklyRecurring()) {
            return List.of(toHardConstraint(constraint));
        }

        List<AlgorithmHardSessionConstraint> expanded = new ArrayList<>();
        LocalDate date = constraint.getStartDateTime().toLocalDate();
        LocalTime startTime = constraint.getStartDateTime().toLocalTime();
        LocalTime endTime = constraint.getEndDateTime().toLocalTime();
        UUID taAssignmentId = constraint.getTaCourseAssignment().getTaCourseAssignmentId();

        while (!date.isAfter(recurringEndDate)) {
            expanded.add(new AlgorithmHardSessionConstraint(
                    taAssignmentId,
                    new AlgorithmTimeInterval(
                            LocalDateTime.of(date, startTime),
                            LocalDateTime.of(date, endTime)
                    )
            ));
            date = date.plusWeeks(1);
        }

        return expanded;
    }

    private List<AlgorithmSoftSessionConstraint> expandSoftConstraints(
            TACourseSessionConstraint constraint,
            LocalDate recurringEndDate
    ) {
        if (!constraint.isWeeklyRecurring()) {
            return List.of(toSoftConstraint(constraint));
        }

        List<AlgorithmSoftSessionConstraint> expanded = new ArrayList<>();
        LocalDate date = constraint.getStartDateTime().toLocalDate();
        LocalTime startTime = constraint.getStartDateTime().toLocalTime();
        LocalTime endTime = constraint.getEndDateTime().toLocalTime();
        UUID taAssignmentId = constraint.getTaCourseAssignment().getTaCourseAssignmentId();

        while (!date.isAfter(recurringEndDate)) {
            expanded.add(new AlgorithmSoftSessionConstraint(
                    taAssignmentId,
                    new AlgorithmTimeInterval(
                            LocalDateTime.of(date, startTime),
                            LocalDateTime.of(date, endTime)
                    ),
                    softConstraintWeight
            ));
            date = date.plusWeeks(1);
        }

        return expanded;
    }

    @Transactional
    private Result<ScheduleResponse> saveAllocationsAndBuildResponse(
            UUID courseId,
            CurrentUser currentUser,
            List<CourseSession> courseSessions,
            List<TACourseAssignment> assignments,
            AlgorithmResult algorithmResult
    ) {
        if (!algorithmResult.feasible()) {
            return Result.error(ErrorCode.SCHEDULE_INFEASIBLE.toError());
        }

        TransactionalResult<Schedule> scheduleResult = scheduleService.createSchedule(courseId, currentUser.getUser());
        if (!scheduleResult.isSuccess()) {
            return Result.error(scheduleResult.getError());
        }

        Schedule schedule = scheduleResult.getData();

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

        TransactionalResult<List<ScheduleSessionAllocation>> savedAllocationsResult = scheduleSessionAllocationService
                .replaceAllocations(courseId, allocations, currentUser.getUser());
        if (!savedAllocationsResult.isSuccess()) {
            return Result.error(savedAllocationsResult.getError());
        }

        return Result.ok(ScheduleResponse.of(
                schedule,
                savedAllocationsResult.getData().stream().map(ScheduleSessionAllocationResponse::of).toList()
        ));
    }
}
