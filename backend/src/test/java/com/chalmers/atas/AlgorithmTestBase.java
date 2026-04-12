package com.chalmers.atas;

import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.domain.coursesession.CourseSession;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType.*;

public abstract class AlgorithmTestBase {

    protected static final LocalDateTime COURSE_START = LocalDateTime.of(2026, 1, 19, 0,  0);
    protected static final LocalDateTime COURSE_END   = LocalDateTime.of(2026, 3, 22, 23, 59);

    // TA IDs
    protected final UUID sebastiaanId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    protected final UUID yakovId      = UUID.fromString("22222222-2222-2222-2222-222222222222");
    protected final UUID leviId       = UUID.fromString("33333333-3333-3333-3333-333333333333");
    protected final UUID albericId    = UUID.fromString("44444444-4444-4444-4444-444444444444");
    protected final UUID phoebeId     = UUID.fromString("55555555-5555-5555-5555-555555555555");
    protected final UUID noelleId     = UUID.fromString("66666666-6666-6666-6666-666666666666");

    protected List<TA> tas;
    protected List<Sessions> sessions;

    @BeforeEach
    void setUp() {
        tas      = buildTAs();
        sessions = buildSessions();
    }


    protected List<TA> buildTAs() {
        return List.of(
                new TA(sebastiaanId, 120, 67, false, sebastiaanConstraints(), List.of(GRADING, LABORATION, HELP, EXERCISE)),
                new TA(yakovId,      80,  51, true,  yakovConstraints(),      List.of(LABORATION, GRADING, HELP, EXERCISE)),
                new TA(leviId,       80,  51, false, leviConstraints(),       List.of(GRADING, LABORATION, HELP, EXERCISE)),
                new TA(albericId,    80,  51, true,  albericConstraints(),    List.of(LABORATION, GRADING, HELP, EXERCISE)),
                new TA(phoebeId,     80,  51, false, phoebeConstraints(),     List.of(LABORATION, GRADING, HELP, EXERCISE)),
                new TA(noelleId,     80,  51, true,  noelleConstraints(),     List.of(LABORATION, GRADING, HELP, EXERCISE))
        );
    }


    protected List<Sessions> buildSessions() {
        List<Sessions> list = new ArrayList<>();

        list.add(recurring("session_mlab", LocalDateTime.of(2026, 1, 19, 8,  0), LocalDateTime.of(2026, 1, 19, 10, 0), LABORATION, 2, 3));
        list.add(recurring("session_wlab", LocalDateTime.of(2026, 1, 21, 13, 0), LocalDateTime.of(2026, 1, 21, 15, 0), LABORATION, 3, 4));
        list.add(recurring("session_flab", LocalDateTime.of(2026, 1, 23, 15, 0), LocalDateTime.of(2026, 1, 23, 17, 0), LABORATION, 2, 4));

        list.add(single("session_lab1_grading1_1",       LocalDateTime.of(2026, 2, 9,  8,  0), LocalDateTime.of(2026, 2, 9,  10, 0), GRADING, 1, 6));
        list.add(single("session_lab1_grading1_2",       LocalDateTime.of(2026, 2, 9,  10, 0), LocalDateTime.of(2026, 2, 9,  12, 0), GRADING, 1, 6));
        list.add(single("session_lab1_grading1_3",       LocalDateTime.of(2026, 2, 9,  13, 0), LocalDateTime.of(2026, 2, 9,  15, 0), GRADING, 1, 6));
        list.add(single("session_lab1_grading1_4",       LocalDateTime.of(2026, 2, 9,  15, 0), LocalDateTime.of(2026, 2, 9,  17, 0), GRADING, 1, 6));
        list.add(single("session_lab1_gradingReserve_1", LocalDateTime.of(2026, 2, 10, 8,  0), LocalDateTime.of(2026, 2, 10, 12, 0), GRADING, 1, 3));
        list.add(single("session_lab1_gradingReserve_2", LocalDateTime.of(2026, 2, 10, 13, 0), LocalDateTime.of(2026, 2, 10, 17, 0), GRADING, 1, 3));

        list.add(single("session_lab2_grading1_1",       LocalDateTime.of(2026, 2, 23, 8,  0), LocalDateTime.of(2026, 2, 23, 10, 0), GRADING, 1, 6));
        list.add(single("session_lab2_grading1_2",       LocalDateTime.of(2026, 2, 23, 10, 0), LocalDateTime.of(2026, 2, 23, 12, 0), GRADING, 1, 6));
        list.add(single("session_lab2_grading1_3",       LocalDateTime.of(2026, 2, 23, 13, 0), LocalDateTime.of(2026, 2, 23, 15, 0), GRADING, 1, 6));
        list.add(single("session_lab2_grading1_4",       LocalDateTime.of(2026, 2, 23, 15, 0), LocalDateTime.of(2026, 2, 23, 17, 0), GRADING, 1, 6));
        list.add(single("session_lab2_gradingReserve_1", LocalDateTime.of(2026, 2, 24, 8,  0), LocalDateTime.of(2026, 2, 24, 12, 0), GRADING, 1, 3));
        list.add(single("session_lab2_gradingReserve_2", LocalDateTime.of(2026, 2, 24, 13, 0), LocalDateTime.of(2026, 2, 24, 17, 0), GRADING, 1, 3));

        list.add(single("session_lab3_grading1_1",       LocalDateTime.of(2026, 3, 9,  8,  0), LocalDateTime.of(2026, 3, 9,  10, 0), GRADING, 1, 6));
        list.add(single("session_lab3_grading1_2",       LocalDateTime.of(2026, 3, 9,  10, 0), LocalDateTime.of(2026, 3, 9,  12, 0), GRADING, 1, 6));
        list.add(single("session_lab3_grading1_3",       LocalDateTime.of(2026, 3, 9,  13, 0), LocalDateTime.of(2026, 3, 9,  15, 0), GRADING, 1, 6));
        list.add(single("session_lab3_grading1_4",       LocalDateTime.of(2026, 3, 9,  15, 0), LocalDateTime.of(2026, 3, 9,  17, 0), GRADING, 1, 6));
        list.add(single("session_lab3_gradingReserve_1", LocalDateTime.of(2026, 3, 10, 8,  0), LocalDateTime.of(2026, 3, 10, 12, 0), GRADING, 1, 3));
        list.add(single("session_lab3_gradingReserve_2", LocalDateTime.of(2026, 3, 10, 13, 0), LocalDateTime.of(2026, 3, 10, 17, 0), GRADING, 1, 3));


        list.add(single("session_exam_grading1", LocalDateTime.of(2026, 3, 19, 8, 0), LocalDateTime.of(2026, 3, 19, 17, 0), GRADING, 4, 6));
        list.add(single("session_exam_grading2", LocalDateTime.of(2026, 3, 20, 8, 0), LocalDateTime.of(2026, 3, 20, 17, 0), GRADING, 3, 6));
        list.add(single("session_exam_grading3", LocalDateTime.of(2026, 3, 21, 8, 0), LocalDateTime.of(2026, 3, 21, 17, 0), GRADING, 2, 6));

        return list;
    }


    private List<TAConstraint> sebastiaanConstraints() {
        List<TAConstraint> c = new ArrayList<>();
        c.addAll(weekly(sebastiaanId, LocalDateTime.of(2026, 1, 19, 10, 0), LocalDateTime.of(2026, 1, 19, 15, 0)));
        c.addAll(weekly(sebastiaanId, LocalDateTime.of(2026, 1, 21, 10, 0), LocalDateTime.of(2026, 1, 21, 15, 0)));
        c.addAll(weekly(sebastiaanId, LocalDateTime.of(2026, 1, 23, 10, 0), LocalDateTime.of(2026, 1, 23, 15, 0)));
        return c;
    }

    private List<TAConstraint> yakovConstraints() {
        List<TAConstraint> c = new ArrayList<>();
        c.addAll(weekly(yakovId, LocalDateTime.of(2026, 1, 19, 8,  0), LocalDateTime.of(2026, 1, 19, 12, 0)));
        c.addAll(weekly(yakovId, LocalDateTime.of(2026, 1, 21, 10, 0), LocalDateTime.of(2026, 1, 21, 17, 0)));
        return c;
    }

    private List<TAConstraint> leviConstraints() {
        List<TAConstraint> c = new ArrayList<>();
        c.addAll(weekly(leviId, LocalDateTime.of(2026, 1, 19, 13, 0), LocalDateTime.of(2026, 1, 19, 15, 0)));
        c.addAll(weekly(leviId, LocalDateTime.of(2026, 1, 22, 10, 0), LocalDateTime.of(2026, 1, 22, 12, 0)));
        return c;
    }

    private List<TAConstraint> albericConstraints() {
        List<TAConstraint> c = new ArrayList<>();
        c.addAll(weekly(albericId, LocalDateTime.of(2026, 1, 20, 8,  0), LocalDateTime.of(2026, 1, 20, 10, 0)));
        c.addAll(weekly(albericId, LocalDateTime.of(2026, 1, 21, 10, 0), LocalDateTime.of(2026, 1, 21, 12, 0)));
        c.add(once(albericId,      LocalDateTime.of(2026, 1, 23, 8,  0), LocalDateTime.of(2026, 1, 23, 10, 0)));
        c.add(once(albericId,      LocalDateTime.of(2026, 1, 30, 8,  0), LocalDateTime.of(2026, 1, 30, 10, 0)));
        return c;
    }

    private List<TAConstraint> phoebeConstraints() {
        List<TAConstraint> c = new ArrayList<>();
        c.addAll(weekly(phoebeId, LocalDateTime.of(2026, 1, 22, 15, 0), LocalDateTime.of(2026, 1, 22, 17, 0)));
        c.addAll(weekly(phoebeId, LocalDateTime.of(2026, 1, 23, 15, 0), LocalDateTime.of(2026, 1, 23, 17, 0)));
        c.addAll(weekly(phoebeId, LocalDateTime.of(2026, 1, 20, 13, 0), LocalDateTime.of(2026, 1, 20, 15, 0)));
        c.addAll(weekly(phoebeId, LocalDateTime.of(2026, 1, 23, 13, 0), LocalDateTime.of(2026, 1, 23, 15, 0)));
        return c;
    }

    private List<TAConstraint> noelleConstraints() {
        List<TAConstraint> c = new ArrayList<>();
        c.addAll(weekly(noelleId, LocalDateTime.of(2026, 1, 20, 8, 0), LocalDateTime.of(2026, 1, 20, 12, 0)));
        c.addAll(weekly(noelleId, LocalDateTime.of(2026, 1, 21, 8, 0), LocalDateTime.of(2026, 1, 21, 10, 0)));
        c.addAll(weekly(noelleId, LocalDateTime.of(2026, 1, 23, 8, 0), LocalDateTime.of(2026, 1, 23, 12, 0)));
        return c;
    }


    protected Sessions single(String name, LocalDateTime start, LocalDateTime end,
                              CourseSession.CourseSessionType type, int min, int max) {
        return new Sessions(stableUuid(name), new Timeslots(start, end), type, min, max, start, end, false);
    }

    protected Sessions recurring(String name, LocalDateTime start, LocalDateTime end,
                                 CourseSession.CourseSessionType type, int min, int max) {
        return new Sessions(stableUuid(name), new Timeslots(start, end), type, min, max, COURSE_START, COURSE_END, true);
    }

    protected TAConstraint once(UUID taId, LocalDateTime start, LocalDateTime end) {
        return new TAConstraint(new Timeslots(start, end), taId);
    }

    protected List<TAConstraint> weekly(UUID taId, LocalDateTime start, LocalDateTime end) {
        List<TAConstraint> result = new ArrayList<>();
        while (!start.isAfter(COURSE_END)) {
            result.add(once(taId, start, end));
            start = start.plusWeeks(1);
            end   = end.plusWeeks(1);
        }
        return result;
    }

    protected UUID stableUuid(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes());
    }
}