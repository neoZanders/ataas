package com.chalmers.atas.integrationtest;

import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.integrationtest.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ScheduleIntegrationTest extends IntegrationTestBase {

    @Test
    void testCreateSchedule__ownerCreatesScheduleForFeasibleCourse__returnsSchedule() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk())
                .body("scheduleId", notNullValue())
                .body("courseId", is(setup.course().getCourseId().toString()))
                .body("allocations", hasSize(2))
                .body("allocations[*].scheduleSessionAllocationId", everyItem(notNullValue()))
                .body("allocations[*].scheduleId", everyItem(notNullValue()))
                .body("allocations[*].courseSessionId", containsInAnyOrder(
                        setup.mondaySession().getCourseSessionId().toString(),
                        setup.tuesdaySession().getCourseSessionId().toString()
                ))
                .body("allocations[*].taCourseAssignmentId", containsInAnyOrder(
                        setup.taAssignment1().getTaCourseAssignmentId().toString(),
                        setup.taAssignment2().getTaCourseAssignmentId().toString()
                ));
    }

    @Test
    void testCreateSchedule__hardConstraintsForceSpecificAllocations__returnsExpectedAllocations() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk())
                .body("allocations[?(@.courseSessionId == '%s')].taCourseAssignmentId"
                                .formatted(setup.mondaySession().getCourseSessionId()),
                        contains(setup.taAssignment1().getTaCourseAssignmentId().toString()))
                .body("allocations[?(@.courseSessionId == '%s')].taCourseAssignmentId"
                                .formatted(setup.tuesdaySession().getCourseSessionId()),
                        contains(setup.taAssignment2().getTaCourseAssignmentId().toString()));
    }

    @Test
    void testCreateSchedule__calledTwice__replacesAllocationsWithoutDuplicating() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk())
                .body("scheduleId", notNullValue())
                .body("allocations", hasSize(2));

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk())
                .body("scheduleId", notNullValue())
                .body("allocations", hasSize(2))
                .body("allocations[*].courseSessionId", containsInAnyOrder(
                        setup.mondaySession().getCourseSessionId().toString(),
                        setup.tuesdaySession().getCourseSessionId().toString()
                ));

        asCurrentUser(setup.owner())
                .get("/api/courses/{courseId}/schedule", setup.course().getCourseId())
                .expect(status().isOk())
                .body("allocations", hasSize(2));
    }

    @Test
    void testGetSchedule__existingScheduleAsOwner__returnsSchedule() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk());

        asCurrentUser(setup.owner())
                .get("/api/courses/{courseId}/schedule", setup.course().getCourseId())
                .expect(status().isOk())
                .body("scheduleId", notNullValue())
                .body("courseId", is(setup.course().getCourseId().toString()))
                .body("allocations", hasSize(2))
                .body("allocations[*].courseSessionId", containsInAnyOrder(
                        setup.mondaySession().getCourseSessionId().toString(),
                        setup.tuesdaySession().getCourseSessionId().toString()
                ))
                .body("allocations[*].taCourseAssignmentId", containsInAnyOrder(
                        setup.taAssignment1().getTaCourseAssignmentId().toString(),
                        setup.taAssignment2().getTaCourseAssignmentId().toString()
                ));
    }

    @Test
    void testGetSchedule__existingScheduleAsTA1__returnsOnlyTA1Allocations() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk());

        asCurrentUser(setup.ta1())
                .get("/api/courses/{courseId}/schedule", setup.course().getCourseId())
                .expect(status().isOk())
                .body("scheduleId", notNullValue())
                .body("courseId", is(setup.course().getCourseId().toString()))
                .body("allocations", hasSize(1))
                .body("allocations[0].courseSessionId", is(setup.mondaySession().getCourseSessionId().toString()))
                .body("allocations[0].taCourseAssignmentId", is(setup.taAssignment1().getTaCourseAssignmentId().toString()));
    }

    @Test
    void testGetSchedule__existingScheduleAsTA2__returnsOnlyTA2Allocations() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk());

        asCurrentUser(setup.ta2())
                .get("/api/courses/{courseId}/schedule", setup.course().getCourseId())
                .expect(status().isOk())
                .body("scheduleId", notNullValue())
                .body("courseId", is(setup.course().getCourseId().toString()))
                .body("allocations", hasSize(1))
                .body("allocations[0].courseSessionId", is(setup.tuesdaySession().getCourseSessionId().toString()))
                .body("allocations[0].taCourseAssignmentId", is(setup.taAssignment2().getTaCourseAssignmentId().toString()));
    }

    @Test
    void testGetSchedule__noScheduleExists__returnsClientError() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .get("/api/courses/{courseId}/schedule", setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testGetSchedule__unknownCourse__returnsClientError() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .get("/api/courses/{courseId}/schedule", UUID.randomUUID())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateSchedule__TAAttemptsToCreateSchedule__returnsClientError() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.ta1())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateSchedule__nonOwnerCRAttemptsToCreateSchedule__returnsClientError() {
        SchedulingCase setup = createFeasibleSchedule();
        User nonOwnerCR = createNonOwnerCRForScheduleCourse(setup.course());

        asCurrentUser(nonOwnerCR)
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testGetSchedule__nonCourseUserAttemptsToGetSchedule__returnsClientError() {
        SchedulingCase setup = createFeasibleSchedule();

        User otherTA = createUser()
                .withEmail("schedule-other-ta-" + UUID.randomUUID() + "@example.com")
                .withName("Other TA")
                .withUserType(User.UserType.TA)
                .persist();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk());

        asCurrentUser(otherTA)
                .get("/api/courses/{courseId}/schedule", setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testGetSchedule__nonOwnerCRButJoinedCRCanViewSchedule__returnsSchedule() {
        SchedulingCase setup = createFeasibleSchedule();
        User joinedCR = createNonOwnerCRForScheduleCourse(setup.course());

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().isOk());

        asCurrentUser(joinedCR)
                .get("/api/courses/{courseId}/schedule", setup.course().getCourseId())
                .expect(status().isOk())
                .body("scheduleId", notNullValue())
                .body("courseId", is(setup.course().getCourseId().toString()))
                .body("allocations", hasSize(2));
    }

    @Test
    void testCreateSchedule__courseHasNoSessions__returnsClientError() {
        SchedulingCase setup = createCourseWithoutCourseSessions();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateSchedule__courseHasNoJoinedTAs__returnsClientError() {
        SchedulingCase setup = createCourseWithoutJoinedTAs();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateSchedule__joinedTAHasMissingMinHours__returnsClientError() {
        SchedulingCase setup = createCourseWithMissingTABudget();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateSchedule__scheduleIsInfeasibleBecauseOfHardConstraints__returnsClientError() {
        SchedulingCase setup = createInfeasibleScheduleCase();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateSchedule__unknownCourse__returnsClientError() {
        SchedulingCase setup = createFeasibleSchedule();

        asCurrentUser(setup.owner())
                .post("/api/courses/{courseId}/schedule", Map.of(), UUID.randomUUID())
                .expect(status().is4xxClientError());
    }
}