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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chalmers.atas.common.ErrorCode.*;

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
    private final TransactionHandler transactionHandler;

    public ScheduleApplicationService(
            List<AlgorithmService> algorithmServices,
            @Value("${app.alg.type:NAIVE_CHOCO}") AlgorithmType algorithmType,
            @Value("${app.alg.soft-constraint-weight:1}") int softConstraintWeight,
            CourseAuthorizationService courseAuthorizationService,
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
        this.courseAuthorizationService = courseAuthorizationService;
        this.courseSessionService = courseSessionService;
        this.scheduleService = scheduleService;
        this.scheduleSessionAllocationService = scheduleSessionAllocationService;
        this.taCourseAssignmentService = taCourseAssignmentService;
        this.taCourseSessionConstraintService = taCourseSessionConstraintService;
        this.transactionHandler = transactionHandler;
    }

    public Result<ScheduleResponse> createSchedule(UUID courseId, CurrentUser currentUser) {
        AlgorithmService algorithmService = resolveChocoAlgorithmService();
        if (algorithmService == null) {
            return Result.errorFromCode(INTERNAL_SERVER_ERROR, "No Choco algorithm service configured");
        }

        Result<Course> courseResult = courseAuthorizationService.assertUserIsCrOfCourse(
                courseId,
                currentUser.getUser()
        );
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
            return Result.errorFromCode(BAD_REQUEST, "Course has no course sessions");
        }

        List<AlgorithmSession> algorithmSessions = toAlgorithmSessions(
                courseSessions,
                course.getEndDate()
        );

        if (algorithmSessions.isEmpty()) {
            return Result.errorFromCode(BAD_REQUEST, "Course has no schedulable course session occurrences");
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
            return Result.errorFromCode(BAD_REQUEST, "Course has no joined TA course assignments");
        }

        Result<Void> budgetValidation = validateHourBudgets(joinedAssignments);
        if (!budgetValidation.isSuccess()) {
            return Result.error(budgetValidation.getError());
        }

        Result<List<TACourseSessionConstraint>> constraintsResult = taCourseSessionConstraintService
                .getCourseConstraints(course, Optional.empty());

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

        AlgorithmRequest algorithmRequest = toAlgorithmRequest(
                course,
                algorithmSessions,
                joinedAssignments,
                filteredConstraints
        );

        Result<AlgorithmResult> algorithmResult = algorithmService.runAlgorithm(algorithmRequest);
        if (!algorithmResult.isSuccess()) {
            return Result.error(algorithmResult.getError());
        }

        return saveAllocationsAndBuildResponse(
                courseId,
                currentUser,
                algorithmSessions,
                joinedAssignments,
                algorithmResult.getData()
        );
    }

    public Result<ScheduleResponse> getSchedule(UUID courseId, CurrentUser currentUser) {
        return scheduleService.getSchedule(courseId, currentUser.getUser())
                .flatMap(schedules -> {
                    if (schedules.isEmpty()) {
                        return Result.errorFromCode(NOT_FOUND, "Schedule not found");
                    }

                    Schedule schedule = schedules.getFirst();

                    return scheduleSessionAllocationService
                            .getAllocations(courseId, currentUser.getUser())
                            .map(allocations -> ScheduleResponse.of(
                                    schedule,
                                    allocations.stream()
                                            .map(ScheduleSessionAllocationResponse::of)
                                            .toList()
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
            List<AlgorithmSession> algorithmSessions,
            List<TACourseAssignment> assignments,
            List<TACourseSessionConstraint> constraints
    ) {
        return new AlgorithmRequest(
                algorithmSessions,
                assignments.stream().map(this::toAlgorithmTA).toList(),
                toHardConstraints(constraints, course.getEndDate()),
                toSoftConstraints(constraints, course.getEndDate())
        );
    }

    private List<AlgorithmSession> toAlgorithmSessions(
            List<CourseSession> courseSessions,
            LocalDate recurringEndDate
    ) {
        return courseSessions.stream()
                .flatMap(session -> expandCourseSession(session, recurringEndDate).stream())
                .toList();
    }

    private List<AlgorithmSession> expandCourseSession(
            CourseSession session,
            LocalDate recurringEndDate
    ) {
        if (!session.isWeeklyRecurring()) {
            return List.of(toAlgorithmSession(
                    session.getCourseSessionId(),
                    session.getStartDateTime(),
                    session.getEndDateTime(),
                    session.getCourseSessionType(),
                    session.getMinTAs(),
                    session.getMaxTAs()
            ));
        }

        List<AlgorithmSession> expanded = new ArrayList<>();

        LocalDate date = session.getStartDateTime().toLocalDate();
        LocalTime startTime = session.getStartDateTime().toLocalTime();
        LocalTime endTime = session.getEndDateTime().toLocalTime();

        while (!date.isAfter(recurringEndDate)) {
            expanded.add(toAlgorithmSession(
                    UUID.randomUUID(),
                    LocalDateTime.of(date, startTime),
                    LocalDateTime.of(date, endTime),
                    session.getCourseSessionType(),
                    session.getMinTAs(),
                    session.getMaxTAs()
            ));

            date = date.plusWeeks(1);
        }

        return expanded;
    }

    private AlgorithmSession toAlgorithmSession(
            UUID sessionId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            CourseSession.CourseSessionType courseSessionType,
            int minTAs,
            int maxTAs
    ) {
        return new AlgorithmSession(
                sessionId,
                new AlgorithmTimeInterval(startDateTime, endDateTime),
                courseSessionType,
                minTAs,
                maxTAs
        );
    }

    private AlgorithmTA toAlgorithmTA(TACourseAssignment assignment) {
        return new AlgorithmTA(
                assignment.getTaCourseAssignmentId(),
                assignment.getMinHours(),
                assignment.getMaxHours(),
                orderedPreferences(assignment),
                assignment.getIsCompactSchedule()
        );
    }

    private Result<Void> validateHourBudgets(List<TACourseAssignment> assignments) {
        for (TACourseAssignment assignment : assignments) {
            if (assignment.getMinHours() == null || assignment.getMaxHours() == null) {
                return Result.errorFromCode(BAD_REQUEST,
                        "TA course assignment is missing hour budget: " + assignment.getTaCourseAssignmentId()
                );
            }

            if (assignment.getMinHours() < 0 || assignment.getMaxHours() < 0) {
                return Result.errorFromCode(BAD_REQUEST,
                        "TA course assignment has negative hour budget: " + assignment.getTaCourseAssignmentId()
                );
            }

            if (assignment.getMinHours() > assignment.getMaxHours()) {
                return Result.errorFromCode(BAD_REQUEST,
                        "TA min hours cannot be greater than max hours: " + assignment.getTaCourseAssignmentId()
                );
            }
        }

        return Result.ok();
    }

    private List<CourseSession.CourseSessionType> orderedPreferences(TACourseAssignment assignment) {
        List<CourseSession.CourseSessionType> ranked = Stream.of(
                        assignment.getSessionTypePreference1(),
                        assignment.getSessionTypePreference2(),
                        assignment.getSessionTypePreference3(),
                        assignment.getSessionTypePreference4()
                )
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

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
                new AlgorithmTimeInterval(
                        constraint.getStartDateTime(),
                        constraint.getEndDateTime()
                )
        );
    }

    private AlgorithmSoftSessionConstraint toSoftConstraint(TACourseSessionConstraint constraint) {
        return new AlgorithmSoftSessionConstraint(
                constraint.getTaCourseAssignment().getTaCourseAssignmentId(),
                new AlgorithmTimeInterval(
                        constraint.getStartDateTime(),
                        constraint.getEndDateTime()
                ),
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

    private Result<ScheduleResponse> saveAllocationsAndBuildResponse(
            UUID courseId,
            CurrentUser currentUser,
            List<AlgorithmSession> algorithmSessions,
            List<TACourseAssignment> assignments,
            AlgorithmResult algorithmResult
    ) {
        if (!algorithmResult.feasible()) {
            return Result.error(ErrorCode.SCHEDULE_INFEASIBLE.toError());
        }

        return transactionHandler.executeInTransaction(() ->
                scheduleService.createSchedule(courseId, currentUser.getUser()).flatMap(schedule -> {
                    Map<UUID, AlgorithmSession> algorithmSessionById = algorithmSessions.stream()
                            .collect(Collectors.toMap(AlgorithmSession::sessionId, Function.identity()));

                    Map<UUID, TACourseAssignment> assignmentById = assignments.stream()
                            .collect(Collectors.toMap(TACourseAssignment::getTaCourseAssignmentId, Function.identity()));

                    List<ScheduleSessionAllocation> allocations = algorithmResult.allocations().stream()
                            .flatMap(allocation -> allocation.taAssignmentIds().stream()
                                    .map(taAssignmentId -> {
                                        AlgorithmSession session = algorithmSessionById.get(allocation.sessionId());
                                        TACourseAssignment assignment = assignmentById.get(taAssignmentId);

                                        if (session == null || assignment == null) {
                                            return null;
                                        }

                                        return ScheduleSessionAllocation.of(
                                                schedule,
                                                session.timeInterval().getStart(),
                                                session.timeInterval().getEnd(),
                                                session.type(),
                                                assignment
                                        );
                                    }))
                            .toList();

                    if (allocations.stream().anyMatch(java.util.Objects::isNull)) {
                        return TransactionalResult.rollbackFor(INTERNAL_SERVER_ERROR.toError(
                                "Algorithm output referenced unknown session or TA assignment"));
                    }

                    return scheduleSessionAllocationService
                            .replaceAllocations(courseId, allocations, currentUser.getUser())
                            .map(savedAllocations -> new SavedScheduleAllocations(
                                    schedule,
                                    savedAllocations
                            ));
                })
        ).map(savedAllocations -> ScheduleResponse.of(
                savedAllocations.schedule(),
                savedAllocations.allocations().stream()
                        .map(ScheduleSessionAllocationResponse::of)
                        .toList()
        ));
    }

    private record SavedScheduleAllocations(
            Schedule schedule,
            List<ScheduleSessionAllocation> allocations
    ) {}
}