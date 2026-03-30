package com.chalmers.atas.algorithmtest;

import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.domain.coursesession.CourseSession;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AlgorithmTestBase {

    protected static final LocalDate RECURRING_START_DATE = LocalDate.of(2026, 1, 19);
    protected static final LocalDate RECURRING_END_DATE = LocalDate.of(2026, 3, 22);
    protected static final int DEFAULT_SOFT_WEIGHT = 10;

    protected AlgorithmRequest request;

    protected UUID sebastiaanId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    protected UUID yakovId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    protected UUID leviId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    protected UUID albericId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    protected UUID phoebeId = UUID.fromString("55555555-5555-5555-5555-555555555555");
    protected UUID noelleId = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @BeforeEach
    void setUpAlgorithmRequest() {
        request = new AlgorithmRequest(
                buildSessions(),
                buildTAs(),
                buildHardConstraints(),
                buildSoftConstraints()
        );
    }

    protected List<AlgorithmTA> buildTAs() {
        return List.of(
                new AlgorithmTA(
                        sebastiaanId,
                        67,
                        120,
                        List.of(CourseSession.CourseSessionType.GRADING),
                        false
                ),
                new AlgorithmTA(
                        yakovId,
                        51,
                        80,
                        List.of(CourseSession.CourseSessionType.LABORATION),
                        true
                ),
                new AlgorithmTA(
                        leviId,
                        51,
                        80,
                        List.of(CourseSession.CourseSessionType.GRADING),
                        false
                ),
                new AlgorithmTA(
                        albericId,
                        51,
                        80,
                        List.of(CourseSession.CourseSessionType.LABORATION),
                        true
                ),
                new AlgorithmTA(
                        phoebeId,
                        51,
                        80,
                        List.of(CourseSession.CourseSessionType.LABORATION),
                        false
                ),
                new AlgorithmTA(
                        noelleId,
                        51,
                        80,
                        List.of(CourseSession.CourseSessionType.LABORATION),
                        true
                )
        );
    }

    protected List<AlgorithmSession> buildSessions() {
        List<AlgorithmSession> sessions = new ArrayList<>();

        sessions.addAll(weeklySessions(
                "session_mlab",
                LocalDate.of(2026, 1, 19),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                CourseSession.CourseSessionType.LABORATION,
                2,
                3
        ));

        sessions.addAll(weeklySessions(
                "session_wlab",
                LocalDate.of(2026, 1, 21),
                LocalTime.of(13, 0),
                LocalTime.of(15, 0),
                CourseSession.CourseSessionType.LABORATION,
                3,
                4
        ));

        sessions.addAll(weeklySessions(
                "session_flab",
                LocalDate.of(2026, 1, 23),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                CourseSession.CourseSessionType.LABORATION,
                2,
                4
        ));

        sessions.add(session(
                "session_lab1_grading1_1",
                LocalDateTime.of(2026, 2, 9, 8, 0),
                LocalDateTime.of(2026, 2, 9, 10, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab1_grading1_2",
                LocalDateTime.of(2026, 2, 9, 10, 0),
                LocalDateTime.of(2026, 2, 9, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab1_grading1_3",
                LocalDateTime.of(2026, 2, 9, 13, 0),
                LocalDateTime.of(2026, 2, 9, 15, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab1_grading1_4",
                LocalDateTime.of(2026, 2, 9, 15, 0),
                LocalDateTime.of(2026, 2, 9, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab1_gradingReserve_1",
                LocalDateTime.of(2026, 2, 10, 8, 0),
                LocalDateTime.of(2026, 2, 10, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3
        ));
        sessions.add(session(
                "session_lab1_gradingReserve_2",
                LocalDateTime.of(2026, 2, 10, 13, 0),
                LocalDateTime.of(2026, 2, 10, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3
        ));

        sessions.add(session(
                "session_lab2_grading1_1",
                LocalDateTime.of(2026, 2, 23, 8, 0),
                LocalDateTime.of(2026, 2, 23, 10, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab2_grading1_2",
                LocalDateTime.of(2026, 2, 23, 10, 0),
                LocalDateTime.of(2026, 2, 23, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab2_grading1_3",
                LocalDateTime.of(2026, 2, 23, 13, 0),
                LocalDateTime.of(2026, 2, 23, 15, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab2_grading1_4",
                LocalDateTime.of(2026, 2, 23, 15, 0),
                LocalDateTime.of(2026, 2, 23, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab2_gradingReserve_1",
                LocalDateTime.of(2026, 2, 24, 8, 0),
                LocalDateTime.of(2026, 2, 24, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3
        ));
        sessions.add(session(
                "session_lab2_gradingReserve_2",
                LocalDateTime.of(2026, 2, 24, 13, 0),
                LocalDateTime.of(2026, 2, 24, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3
        ));

        sessions.add(session(
                "session_lab3_grading1_1",
                LocalDateTime.of(2026, 3, 9, 8, 0),
                LocalDateTime.of(2026, 3, 9, 10, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab3_grading1_2",
                LocalDateTime.of(2026, 3, 9, 10, 0),
                LocalDateTime.of(2026, 3, 9, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab3_grading1_3",
                LocalDateTime.of(2026, 3, 9, 13, 0),
                LocalDateTime.of(2026, 3, 9, 15, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab3_grading1_4",
                LocalDateTime.of(2026, 3, 9, 15, 0),
                LocalDateTime.of(2026, 3, 9, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6
        ));
        sessions.add(session(
                "session_lab3_gradingReserve_1",
                LocalDateTime.of(2026, 3, 10, 8, 0),
                LocalDateTime.of(2026, 3, 10, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3
        ));
        sessions.add(session(
                "session_lab3_gradingReserve_2",
                LocalDateTime.of(2026, 3, 10, 13, 0),
                LocalDateTime.of(2026, 3, 10, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3
        ));

        sessions.add(session(
                "session_exam_grading1",
                LocalDateTime.of(2026, 3, 19, 8, 0),
                LocalDateTime.of(2026, 3, 19, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                4,
                6
        ));
        sessions.add(session(
                "session_exam_grading2",
                LocalDateTime.of(2026, 3, 20, 8, 0),
                LocalDateTime.of(2026, 3, 20, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                3,
                6
        ));
        sessions.add(session(
                "session_exam_grading3",
                LocalDateTime.of(2026, 3, 21, 8, 0),
                LocalDateTime.of(2026, 3, 21, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                2,
                6
        ));

        return sessions;
    }

    protected List<AlgorithmHardSessionConstraint> buildHardConstraints() {
        List<AlgorithmHardSessionConstraint> constraints = new ArrayList<>();

        constraints.addAll(weeklyHardConstraints(
                sebastiaanId,
                LocalDate.of(2026, 1, 19),
                LocalTime.of(10, 0),
                LocalTime.of(15, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                sebastiaanId,
                LocalDate.of(2026, 1, 21),
                LocalTime.of(10, 0),
                LocalTime.of(15, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                sebastiaanId,
                LocalDate.of(2026, 1, 23),
                LocalTime.of(10, 0),
                LocalTime.of(15, 0)
        ));

        constraints.addAll(weeklyHardConstraints(
                yakovId,
                LocalDate.of(2026, 1, 19),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                yakovId,
                LocalDate.of(2026, 1, 21),
                LocalTime.of(10, 0),
                LocalTime.of(17, 0)
        ));

        constraints.addAll(weeklyHardConstraints(
                leviId,
                LocalDate.of(2026, 1, 19),
                LocalTime.of(13, 0),
                LocalTime.of(15, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                leviId,
                LocalDate.of(2026, 1, 22),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        ));

        constraints.addAll(weeklyHardConstraints(
                albericId,
                LocalDate.of(2026, 1, 20),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                albericId,
                LocalDate.of(2026, 1, 21),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        ));
        constraints.add(hardConstraint(
                albericId,
                LocalDateTime.of(2026, 1, 23, 8, 0),
                LocalDateTime.of(2026, 1, 23, 10, 0)
        ));
        constraints.add(hardConstraint(
                albericId,
                LocalDateTime.of(2026, 1, 30, 8, 0),
                LocalDateTime.of(2026, 1, 30, 10, 0)
        ));

        constraints.addAll(weeklyHardConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 22),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 23),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 20),
                LocalTime.of(13, 0),
                LocalTime.of(15, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 23),
                LocalTime.of(13, 0),
                LocalTime.of(15, 0)
        ));

        constraints.addAll(weeklyHardConstraints(
                noelleId,
                LocalDate.of(2026, 1, 20),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                noelleId,
                LocalDate.of(2026, 1, 21),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0)
        ));
        constraints.addAll(weeklyHardConstraints(
                noelleId,
                LocalDate.of(2026, 1, 23),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        ));

        return constraints;
    }

    protected List<AlgorithmSoftSessionConstraint> buildSoftConstraints() {
        List<AlgorithmSoftSessionConstraint> constraints = new ArrayList<>();

        constraints.addAll(weeklySoftConstraints(
                leviId,
                LocalDate.of(2026, 1, 19),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                leviId,
                LocalDate.of(2026, 1, 20),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                leviId,
                LocalDate.of(2026, 1, 21),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                leviId,
                LocalDate.of(2026, 1, 22),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                leviId,
                LocalDate.of(2026, 1, 23),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                DEFAULT_SOFT_WEIGHT
        ));

        constraints.addAll(weeklySoftConstraints(
                albericId,
                LocalDate.of(2026, 1, 21),
                LocalTime.of(13, 0),
                LocalTime.of(15, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                albericId,
                LocalDate.of(2026, 1, 19),
                LocalTime.of(8, 0),
                LocalTime.of(17, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                albericId,
                LocalDate.of(2026, 1, 23),
                LocalTime.of(13, 0),
                LocalTime.of(17, 0),
                DEFAULT_SOFT_WEIGHT
        ));

        constraints.addAll(weeklySoftConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 19),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 20),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 21),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 22),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                DEFAULT_SOFT_WEIGHT
        ));
        constraints.addAll(weeklySoftConstraints(
                phoebeId,
                LocalDate.of(2026, 1, 23),
                LocalTime.of(15, 0),
                LocalTime.of(17, 0),
                DEFAULT_SOFT_WEIGHT
        ));

        return constraints;
    }

    protected AlgorithmSession session(
            String name,
            LocalDateTime start,
            LocalDateTime end,
            CourseSession.CourseSessionType type,
            int minTAs,
            int maxTAs
    ) {
        return new AlgorithmSession(
                stableUuid(name),
                new AlgorithmTimeInterval(start, end),
                type,
                minTAs,
                maxTAs
        );
    }

    protected AlgorithmHardSessionConstraint hardConstraint(
            UUID taAssignmentId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return new AlgorithmHardSessionConstraint(
                taAssignmentId,
                new AlgorithmTimeInterval(start, end)
        );
    }

    protected AlgorithmSoftSessionConstraint softConstraint(
            UUID taAssignmentId,
            LocalDateTime start,
            LocalDateTime end,
            int weight
    ) {
        return new AlgorithmSoftSessionConstraint(
                taAssignmentId,
                new AlgorithmTimeInterval(start, end),
                weight
        );
    }

    protected List<AlgorithmSession> weeklySessions(
            String baseName,
            LocalDate firstDate,
            LocalTime startTime,
            LocalTime endTime,
            CourseSession.CourseSessionType type,
            int minTAs,
            int maxTAs
    ) {
        List<AlgorithmSession> result = new ArrayList<>();
        LocalDate date = firstDate;
        int index = 1;

        while (!date.isAfter(RECURRING_END_DATE)) {
            result.add(session(
                    baseName + "_" + index,
                    LocalDateTime.of(date, startTime),
                    LocalDateTime.of(date, endTime),
                    type,
                    minTAs,
                    maxTAs
            ));
            date = date.plusWeeks(1);
            index++;
        }

        return result;
    }

    protected List<AlgorithmHardSessionConstraint> weeklyHardConstraints(
            UUID taAssignmentId,
            LocalDate firstDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        List<AlgorithmHardSessionConstraint> result = new ArrayList<>();
        LocalDate date = firstDate;

        while (!date.isAfter(RECURRING_END_DATE)) {
            result.add(hardConstraint(
                    taAssignmentId,
                    LocalDateTime.of(date, startTime),
                    LocalDateTime.of(date, endTime)
            ));
            date = date.plusWeeks(1);
        }

        return result;
    }

    protected List<AlgorithmSoftSessionConstraint> weeklySoftConstraints(
            UUID taAssignmentId,
            LocalDate firstDate,
            LocalTime startTime,
            LocalTime endTime,
            int weight
    ) {
        List<AlgorithmSoftSessionConstraint> result = new ArrayList<>();
        LocalDate date = firstDate;

        while (!date.isAfter(RECURRING_END_DATE)) {
            result.add(softConstraint(
                    taAssignmentId,
                    LocalDateTime.of(date, startTime),
                    LocalDateTime.of(date, endTime),
                    weight
            ));
            date = date.plusWeeks(1);
        }

        return result;
    }

    protected UUID stableUuid(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes());
    }
}
