package com.chalmers.atas.domain.tacoursesessionconstraint;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint.ConstraintType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TACourseSessionConstraintService {

    private final TACourseSessionConstraintRepository taCourseSessionConstraintRepository;

    public Result<List<TACourseSessionConstraint>> getCourseConstraints(Course course, Optional<String> maybeUsername) {
        return Result.ok(maybeUsername.map(username ->
                taCourseSessionConstraintRepository.findAllByTaCourseAssignmentCourseAndTaCourseAssignmentTaNameOrderByStartDateTime(course, username)
        ).orElse(taCourseSessionConstraintRepository.findAllByTaCourseAssignmentCourse(course)));
    }

    public Result<List<TACourseSessionConstraint>> getTAConstraints(Course course, UUID taId) {
        return Result.ok(
                taCourseSessionConstraintRepository
                        .findAllByTaCourseAssignmentTaUserIdAndTaCourseAssignmentCourseOrderByStartDateTime(taId, course)
        );
    }

    public Result<TACourseSessionConstraint> getConstraint(UUID taCourseSessionConstraintId) {
        return Result.ofOptional(
                taCourseSessionConstraintRepository.findById(taCourseSessionConstraintId),
                ErrorCode.TA_CONSTRAINT_NOT_FOUND
        );
    }

    @Transactional
    public TransactionalResult<TACourseSessionConstraint> createConstraint(
            TACourseAssignment taCourseAssignment,
            ConstraintType constraintType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            boolean isWeeklyRecurring
    ) {
        if (startDateTime.isAfter(endDateTime)) {
            return TransactionalResult.rollbackFor(ErrorCode.START_AFTER_END);
        }

        return TransactionalResult.ok(taCourseSessionConstraintRepository.save(
                TACourseSessionConstraint.of(
                        taCourseAssignment,
                        constraintType,
                        startDateTime,
                        endDateTime,
                        isWeeklyRecurring
                )));
    }

    @Transactional
    public TransactionalResult<List<TACourseSessionConstraint>> createConstraints(TACourseAssignment taCourseAssignment, List<TAConstraintRequest> requests, Course course) {
        requests.addAll(taCourseSessionConstraintRepository.findAllByTaCourseAssignment(taCourseAssignment).stream().map(
                courseAssignment ->
                        new TAConstraintRequest(
                                courseAssignment.getConstraintType(),
                                courseAssignment.getStartDateTime(),
                                courseAssignment.getEndDateTime(),
                                courseAssignment.isWeeklyRecurring())
        ).toList());

        List<TACourseSessionConstraint> constraints = normalize(
                taCourseAssignment,
                requests,
                course.getStartDate(),
                course.getEndDate()
        ).stream().map(request ->
                TACourseSessionConstraint.of(
                        taCourseAssignment,
                        request.constraintType(),
                        request.startDateTime(),
                        request.endDateTime(),
                        request.isWeeklyRecurring()
                )
        ).toList();

        return TransactionalResult.ok(taCourseSessionConstraintRepository.saveAll(constraints));
    }

    @Transactional
    public TransactionalResult<TACourseSessionConstraint> updateConstraint(
            TACourseSessionConstraint constraint,
            ConstraintType constraintType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Boolean isWeeklyRecurring
    ) {
        if (constraintType != null) {
            constraint.setConstraintType(constraintType);
        }
        if (startDateTime != null) {
            constraint.setStartDateTime(startDateTime);
        }
        if (endDateTime != null) {
            constraint.setEndDateTime(endDateTime);
        }
        if (isWeeklyRecurring != null) {
            constraint.setWeeklyRecurring(isWeeklyRecurring);
        }

        if (constraint.getStartDateTime().isAfter(constraint.getEndDateTime())) {
            return TransactionalResult.rollbackFor(ErrorCode.START_AFTER_END);
        }

        return TransactionalResult.ok(taCourseSessionConstraintRepository.save(constraint));
    }

    @Transactional
    public TransactionalResult<Void> deleteConstraint(TACourseSessionConstraint constraint) {
        taCourseSessionConstraintRepository.delete(constraint);
        return TransactionalResult.ok();
    }

    private List<TAConstraintRequest> normalize(
            TACourseAssignment taCourseAssignment,
            List<TAConstraintRequest> requests,
            LocalDate courseStartDate,
            LocalDate courseEndDate
    ) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<TAConstraintRequest> compressed = requests.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.groupingBy(
                        request -> Map.entry(slot(request), request.constraintType()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values()
                .stream()
                .flatMap(group -> group.stream()
                        .filter(TAConstraintRequest::isWeeklyRecurring)
                        .min(Comparator.comparing(TAConstraintRequest::startDateTime))
                        .map(Stream::of)
                        .orElseGet(() -> {
                            if (!shouldCompress(group, courseStartDate, courseEndDate)) {
                                return group.stream();
                            }

                            TAConstraintRequest first = group.stream()
                                    .min(Comparator.comparing(TAConstraintRequest::startDateTime))
                                    .orElseThrow();

                            return Stream.of(new TAConstraintRequest(
                                    first.constraintType(),
                                    first.startDateTime(),
                                    first.endDateTime(),
                                    true
                            ));
                        }))
                .distinct()
                .toList();

        List<TAConstraintRequest> hardConstraints = compressed.stream()
                .filter(request -> request.constraintType() == TACourseSessionConstraint.ConstraintType.HARD)
                .toList();

        List<TACourseSessionConstraint> existing =
                taCourseSessionConstraintRepository.findAllByTaCourseAssignment(taCourseAssignment);

        return compressed.stream()
                .filter(request -> existing.stream()
                        .map(existingConstraint ->
                                new TAConstraintRequest(
                                        existingConstraint.getConstraintType(),
                                        existingConstraint.getStartDateTime(),
                                        existingConstraint.getEndDateTime(),
                                        existingConstraint.isWeeklyRecurring()
                                )
                        ).noneMatch(existingRequest -> existingRequest.equals(request)))
                .filter(request ->
                        request.constraintType() == TACourseSessionConstraint.ConstraintType.HARD
                                || hardConstraints.stream().noneMatch(hard -> hardCoversSoft(hard, request))
                )
                .sorted(Comparator
                        .comparing(TAConstraintRequest::startDateTime)
                        .thenComparing(TAConstraintRequest::endDateTime)
                        .thenComparing(request -> request.constraintType().name())
                        .thenComparing(TAConstraintRequest::isWeeklyRecurring))
                .toList();
    }

    private Slot slot(TAConstraintRequest request) {
        return new Slot(
                request.startDateTime().getDayOfWeek(),
                request.startDateTime().toLocalTime(),
                request.endDateTime().getDayOfWeek(),
                request.endDateTime().toLocalTime()
        );
    }

    private boolean shouldCompress(
            List<TAConstraintRequest> group,
            LocalDate courseStartDate,
            LocalDate courseEndDate
    ) {
        if (group.size() < 2) {
            return false;
        }

        Slot slot = slot(group.getFirst());

        int daySpan = slot.endDay().getValue() - slot.startDay().getValue();
        if (daySpan < 0) {
            daySpan += 7;
        }

        Set<LocalDate> actualStarts = group.stream()
                .map(request -> request.startDateTime().toLocalDate())
                .collect(Collectors.toSet());

        Set<LocalDate> expectedStarts = new HashSet<>();

        LocalDate firstStart = courseStartDate.with(
                TemporalAdjusters.previousOrSame(slot.startDay())
        );

        for (LocalDate start = firstStart; !start.isAfter(courseEndDate); start = start.plusWeeks(1)) {
            LocalDate occurrenceEnd = start.plusDays(daySpan);

            boolean overlapsCourse =
                    !occurrenceEnd.isBefore(courseStartDate)
                            && !start.isAfter(courseEndDate);

            if (overlapsCourse) {
                expectedStarts.add(start);
            }
        }

        return expectedStarts.size() >= 2 && actualStarts.containsAll(expectedStarts);
    }

    private boolean hardCoversSoft(TAConstraintRequest hard, TAConstraintRequest soft) {
        if (hard.constraintType() != TACourseSessionConstraint.ConstraintType.HARD
                || soft.constraintType() != TACourseSessionConstraint.ConstraintType.SOFT) {
            return false;
        }

        if (!slot(hard).equals(slot(soft))) {
            return false;
        }

        if (soft.isWeeklyRecurring() && !hard.isWeeklyRecurring()) {
            return false;
        }

        return hard.isWeeklyRecurring()
                || hard.startDateTime().equals(soft.startDateTime())
                && hard.endDateTime().equals(soft.endDateTime());
    }

    public record TAConstraintRequest(
            TACourseSessionConstraint.ConstraintType constraintType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            boolean isWeeklyRecurring
    ) {}

    private record Slot(
            DayOfWeek startDay,
            LocalTime startTime,
            DayOfWeek endDay,
            LocalTime endTime
    ) {}
}
