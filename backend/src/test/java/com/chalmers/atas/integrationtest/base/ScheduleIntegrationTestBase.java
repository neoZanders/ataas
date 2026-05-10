package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignment;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduleIntegrationTestBase extends TACourseSessionConstraintIntegrationTestBase {

    private static final LocalDate COURSE_START = LocalDate.of(2026, 1, 19);
    private static final LocalDate COURSE_END = LocalDate.of(2026, 3, 22);

    protected SchedulingCase createFeasibleSchedule() {
        SchedulingCase setup = createBaseScheduleCase(
                true,
                CourseAssignmentStatus.JOINED,
                CourseAssignmentStatus.JOINED,
                0,
                10,
                0,
                10,
                true
        );

        /*
         * Expected solution:
         * Monday session: TA 1
         * Tuesday session: TA 2
         */
        saveTAConstraint(
                setup.taAssignment2(),
                TACourseSessionConstraint.ConstraintType.HARD,
                setup.mondaySession().getStartDateTime(),
                setup.mondaySession().getEndDateTime(),
                false
        );

        saveTAConstraint(
                setup.taAssignment1(),
                TACourseSessionConstraint.ConstraintType.HARD,
                setup.tuesdaySession().getStartDateTime(),
                setup.tuesdaySession().getEndDateTime(),
                false
        );

        return setup;
    }

    protected SchedulingCase createFeasibleScheduleWhereTAsCannotSeeAllSchedules() {
        SchedulingCase setup = createBaseScheduleCase(
                false,
                CourseAssignmentStatus.JOINED,
                CourseAssignmentStatus.JOINED,
                0,
                10,
                0,
                10,
                true
        );

        /*
         * Expected solution:
         * Monday session: TA 1
         * Tuesday session: TA 2
         */
        saveTAConstraint(
                setup.taAssignment2(),
                TACourseSessionConstraint.ConstraintType.HARD,
                setup.mondaySession().getStartDateTime(),
                setup.mondaySession().getEndDateTime(),
                false
        );

        saveTAConstraint(
                setup.taAssignment1(),
                TACourseSessionConstraint.ConstraintType.HARD,
                setup.tuesdaySession().getStartDateTime(),
                setup.tuesdaySession().getEndDateTime(),
                false
        );

        return setup;
    }

    protected SchedulingCase createCourseWithoutCourseSessions() {
        return createBaseScheduleCase(
                true,
                CourseAssignmentStatus.JOINED,
                CourseAssignmentStatus.JOINED,
                0,
                10,
                0,
                10,
                false
        );
    }

    protected SchedulingCase createCourseWithoutJoinedTAs() {
        return createBaseScheduleCase(
                true,
                CourseAssignmentStatus.INVITED,
                CourseAssignmentStatus.INVITED,
                0,
                10,
                0,
                10,
                true
        );
    }

    protected SchedulingCase createCourseWithMissingTABudget() {
        return createBaseScheduleCase(
                true,
                CourseAssignmentStatus.JOINED,
                CourseAssignmentStatus.JOINED,
                null,
                10,
                0,
                10,
                true
        );
    }

    protected SchedulingCase createInfeasibleScheduleCase() {
        SchedulingCase setup = createFeasibleSchedule();

        saveTAConstraint(
                setup.taAssignment1(),
                TACourseSessionConstraint.ConstraintType.HARD,
                setup.mondaySession().getStartDateTime(),
                setup.mondaySession().getEndDateTime(),
                false
        );

        saveTAConstraint(
                setup.taAssignment2(),
                TACourseSessionConstraint.ConstraintType.HARD,
                setup.tuesdaySession().getStartDateTime(),
                setup.tuesdaySession().getEndDateTime(),
                false
        );

        return setup;
    }

    protected User createNonOwnerCRForScheduleCourse(Course course) {
        User nonOwnerCR = saveUser(
                "schedule-non-owner-cr-" + UUID.randomUUID() + "@example.com",
                "Schedule Non Owner CR",
                User.UserType.CR
        );

        saveCRAssignment(
                course,
                nonOwnerCR,
                CourseAssignmentStatus.JOINED
        );

        return nonOwnerCR;
    }

    private SchedulingCase createBaseScheduleCase(
            boolean canTASeeAllSchedules,
            CourseAssignmentStatus ta1Status,
            CourseAssignmentStatus ta2Status,
            Integer ta1MinHours,
            Integer ta1MaxHours,
            Integer ta2MinHours,
            Integer ta2MaxHours,
            boolean includeCourseSessions
    ) {
        User owner = saveUser(
                "schedule-owner-" + UUID.randomUUID() + "@example.com",
                "Schedule Owner",
                User.UserType.CR
        );

        User ta1 = saveUser(
                "schedule-ta-1-" + UUID.randomUUID() + "@example.com",
                "Schedule TA 1",
                User.UserType.TA
        );

        User ta2 = saveUser(
                "schedule-ta-2-" + UUID.randomUUID() + "@example.com",
                "Schedule TA 2",
                User.UserType.TA
        );

        Course course = saveCourse(owner, canTASeeAllSchedules);

        CRCourseAssignment ownerAssignment = saveCRAssignment(
                course,
                owner,
                CourseAssignmentStatus.OWNER
        );

        TACourseAssignment taAssignment1 = saveTAAssignment(
                course,
                ta1,
                ta1Status,
                ta1MinHours,
                ta1MaxHours
        );

        TACourseAssignment taAssignment2 = saveTAAssignment(
                course,
                ta2,
                ta2Status,
                ta2MinHours,
                ta2MaxHours
        );

        CourseSession mondaySession = null;
        CourseSession tuesdaySession = null;

        if (includeCourseSessions) {
            mondaySession = saveCourseSession(
                    course,
                    LocalDateTime.of(2026, 1, 19, 10, 0),
                    LocalDateTime.of(2026, 1, 19, 12, 0),
                    CourseSession.CourseSessionType.LABORATION,
                    1,
                    1,
                    false
            );

            tuesdaySession = saveCourseSession(
                    course,
                    LocalDateTime.of(2026, 1, 20, 10, 0),
                    LocalDateTime.of(2026, 1, 20, 12, 0),
                    CourseSession.CourseSessionType.LABORATION,
                    1,
                    1,
                    false
            );
        }

        return new SchedulingCase(
                owner,
                ta1,
                ta2,
                course,
                ownerAssignment,
                taAssignment1,
                taAssignment2,
                mondaySession,
                tuesdaySession
        );
    }

    private User saveUser(String email, String name, User.UserType userType) {
        User user = User.of(
                email,
                "password",
                name,
                userType
        );

        return userRepository.save(user);
    }

    private Course saveCourse(User owner, boolean canTASeeAllSchedules) {
        Course course = Course.of(
                "DAT" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                owner,
                "Schedule integration test course",
                canTASeeAllSchedules,
                false,
                COURSE_START,
                COURSE_END
        );

        return courseRepository.save(course);
    }

    private CRCourseAssignment saveCRAssignment(
            Course course,
            User cr,
            CourseAssignmentStatus status
    ) {
        CRCourseAssignment assignment = CRCourseAssignment.of(
                cr,
                course,
                status
        );

        return crCourseAssignmentRepository.save(assignment);
    }

    private TACourseAssignment saveTAAssignment(
            Course course,
            User ta,
            CourseAssignmentStatus status,
            Integer minHours,
            Integer maxHours
    ) {
        TACourseAssignment assignment = TACourseAssignment.of(
                ta,
                course,
                status,
                minHours,
                maxHours,
                CourseSession.CourseSessionType.LABORATION,
                CourseSession.CourseSessionType.GRADING,
                CourseSession.CourseSessionType.HELP,
                CourseSession.CourseSessionType.EXERCISE,
                false
        );

        return taCourseAssignmentRepository.save(assignment);
    }

    private CourseSession saveCourseSession(
            Course course,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            CourseSession.CourseSessionType type,
            int minTAs,
            int maxTAs,
            boolean isWeeklyRecurring
    ) {
        CourseSession session = CourseSession.of(
                course,
                startDateTime,
                endDateTime,
                type,
                minTAs,
                maxTAs,
                isWeeklyRecurring
        );

        return courseSessionRepository.save(session);
    }

    private TACourseSessionConstraint saveTAConstraint(
            TACourseAssignment taCourseAssignment,
            TACourseSessionConstraint.ConstraintType constraintType,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            boolean isWeeklyRecurring
    ) {
        TACourseSessionConstraint constraint = TACourseSessionConstraint.of(
                taCourseAssignment,
                constraintType,
                startDateTime,
                endDateTime,
                isWeeklyRecurring
        );

        return taCourseSessionConstraintRepository.save(constraint);
    }

    protected record SchedulingCase(
            User owner,
            User ta1,
            User ta2,
            Course course,
            CRCourseAssignment ownerAssignment,
            TACourseAssignment taAssignment1,
            TACourseAssignment taAssignment2,
            CourseSession mondaySession,
            CourseSession tuesdaySession
    ) {
    }
}