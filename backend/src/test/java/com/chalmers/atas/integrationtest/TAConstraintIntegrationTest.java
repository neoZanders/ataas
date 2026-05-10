package com.chalmers.atas.integrationtest;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.integrationtest.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TAConstraintIntegrationTest extends IntegrationTestBase {

    @Test
    void testGetCourseConstraints__courseHasTAConstraints__returnsConstraintsGroupedByTA() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        asCurrentUser(setup.owner())
                .get("/api/courses/{courseId}/ta-constraints", setup.course().getCourseId())
                .expect(status().isOk())
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void testGetCourseConstraints__usernameFilterMatchesTAName__returnsFilteredConstraints() {
        User owner = createUser()
                .asCR()
                .persist();

        User ta = createUser()
                .asTA()
                .withName("Constraint TA")
                .withEmail("constraint-ta@example.com")
                .persist();

        Course course = createCourse()
                .withOwner(owner)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(owner)
                .asOwner()
                .persist();

        TACourseAssignment assignment = createTACourseAssignment()
                .withCourse(course)
                .withTA(ta)
                .persist();

        createTACourseSessionConstraint()
                .withTACourseAssignment(assignment)
                .asHard()
                .persist();

        asCurrentUser(owner)
                .get("/api/courses/{courseId}/ta-constraints?username=Constraint TA", course.getCourseId())
                .expect(status().isOk())
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void testGetTAConstraints__existingTAHasConstraints__returnsConstraints() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        TACourseSessionConstraint constraint = createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asSoft()
                .persist();

        asCurrentUser(setup.owner())
                .get(
                        "/api/courses/{courseId}/ta-constraints/{taId}",
                        setup.course().getCourseId(),
                        setup.ta().getUserId()
                )
                .expect(status().isOk())
                .body("$", hasSize(1))
                .body("$[0].taCourseSessionConstraintId", constraint.getTaCourseSessionConstraintId().toString())
                .body("$[0].courseId", setup.course().getCourseId().toString())
                .body("$[0].taId", setup.ta().getUserId().toString())
                .body("$[0].constraintType", "SOFT");
    }

    @Test
    void testGetTAConstraints__TAAccessesOwnConstraints__returnsConstraints() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        TACourseSessionConstraint constraint = createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        asCurrentUser(setup.ta())
                .get(
                        "/api/courses/{courseId}/ta-constraints/{taId}",
                        setup.course().getCourseId(),
                        setup.ta().getUserId()
                )
                .expect(status().isOk())
                .body("$", hasSize(1))
                .body("$[0].taCourseSessionConstraintId", constraint.getTaCourseSessionConstraintId().toString())
                .body("$[0].taId", setup.ta().getUserId().toString())
                .body("$[0].constraintType", "HARD");
    }

    @Test
    void testGetTAConstraints__TAAccessesOtherTAConstraints__returnsClientError() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        User otherTA = createUser()
                .asTA()
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(otherTA)
                .persist();

        asCurrentUser(otherTA)
                .get(
                        "/api/courses/{courseId}/ta-constraints/{taId}",
                        setup.course().getCourseId(),
                        setup.ta().getUserId()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateTAConstraint__TAAddsValidHardConstraint__returnsOk() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        asCurrentUser(setup.ta())
                .post("/api/courses/{courseId}/ta-constraints", validCreateTAConstraintRequest(), setup.course().getCourseId())
                .expect(status().isOk());
    }

    @Test
    void testCreateTAConstraint__endBeforeStart__returnsClientError() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        Map<String, Object> body = validCreateTAConstraintRequest();
        body.put("startDateTime", "2026-01-20T12:00:00");
        body.put("endDateTime", "2026-01-20T10:00:00");

        asCurrentUser(setup.ta())
                .post("/api/courses/{courseId}/ta-constraints", body, setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateTAConstraint__userNotAssignedToCourse__returnsClientError() {
        User owner = createUser()
                .asCR()
                .persist();

        User outsider = createUser()
                .asTA()
                .persist();

        Course course = createCourse()
                .withOwner(owner)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(owner)
                .asOwner()
                .persist();

        asCurrentUser(outsider)
                .post("/api/courses/{courseId}/ta-constraints", validCreateTAConstraintRequest(), course.getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testReplaceTAConstraints__TAReplacesAllConstraints__returnsNewConstraints() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        asCurrentUser(setup.ta())
                .put("/api/courses/{courseId}/ta-constraints", validReplaceTAConstraintsRequest(), setup.course().getCourseId())
                .expect(status().isOk())
                .body("$", hasSize(2))
                .body("$[*].courseId", everyItem(is(setup.course().getCourseId().toString())))
                .body("$[*].taId", everyItem(is(setup.ta().getUserId().toString())))
                .body("$[*].constraintType", containsInAnyOrder("HARD", "SOFT"));
    }

    @Test
    void testReplaceTAConstraints__emptyList__removesAllConstraints() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("requests", List.of());

        asCurrentUser(setup.ta())
                .put("/api/courses/{courseId}/ta-constraints", body, setup.course().getCourseId())
                .expect(status().isOk())
                .body("$", hasSize(0));
    }

    @Test
    void testUpdateTAConstraint__TAUpdatesOwnConstraint__returnsUpdatedConstraint() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        TACourseSessionConstraint constraint = createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        asCurrentUser(setup.ta())
                .patch(
                        "/api/courses/{courseId}/ta-constraints/{taCourseSessionConstraintId}",
                        validUpdateTAConstraintRequest(),
                        setup.course().getCourseId(),
                        constraint.getTaCourseSessionConstraintId()
                )
                .expect(status().isOk())
                .body("taCourseSessionConstraintId", constraint.getTaCourseSessionConstraintId().toString())
                .body("taId", setup.ta().getUserId().toString())
                .body("constraintType", "SOFT")
                .body("isWeeklyRecurring", true);
    }

    @Test
    void testUpdateTAConstraint__nonExistingConstraint__returnsClientError() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        asCurrentUser(setup.owner())
                .patch(
                        "/api/courses/{courseId}/ta-constraints/{taCourseSessionConstraintId}",
                        validUpdateTAConstraintRequest(),
                        setup.course().getCourseId(),
                        UUID.randomUUID()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testDeleteTAConstraint__ownerDeletesExistingConstraint__returnsForbidden() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        TACourseSessionConstraint constraint = createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        asCurrentUser(setup.owner())
                .delete(
                        "/api/courses/{courseId}/ta-constraints/{taCourseSessionConstraintId}",
                        setup.course().getCourseId(),
                        constraint.getTaCourseSessionConstraintId()
                )
                .expect(status().isForbidden());
    }

    @Test
    void testDeleteTAConstraint__TADeletesOwnConstraint__returnsOk() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        TACourseSessionConstraint constraint = createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        asCurrentUser(setup.ta())
                .delete(
                        "/api/courses/{courseId}/ta-constraints/{taCourseSessionConstraintId}",
                        setup.course().getCourseId(),
                        constraint.getTaCourseSessionConstraintId()
                )
                .expect(status().isOk());
    }

    @Test
    void testDeleteTAConstraint__TADeletesOtherTAConstraint__returnsClientError() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        User otherTA = createUser()
                .asTA()
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(otherTA)
                .persist();

        TACourseSessionConstraint constraint = createTACourseSessionConstraint()
                .withTACourseAssignment(setup.taAssignment())
                .asHard()
                .persist();

        asCurrentUser(otherTA)
                .delete(
                        "/api/courses/{courseId}/ta-constraints/{taCourseSessionConstraintId}",
                        setup.course().getCourseId(),
                        constraint.getTaCourseSessionConstraintId()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testImportFromTimeEdit__invalidRequest__returnsClientError() {
        TestCourseSetup setup = createCourseWithOwnerAndTA();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timeEditId", "");
        body.put("taId", setup.ta().getUserId().toString());

        asCurrentUser(setup.ta())
                .post("/api/courses/{courseId}/ta-constraints/import", body, setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    private TestCourseSetup createCourseWithOwnerAndTA() {
        User owner = createUser()
                .asCR()
                .withName("Owner CR")
                .withEmail("owner-cr@example.com")
                .persist();

        User ta = createUser()
                .asTA()
                .withName("Test TA")
                .withEmail("test-ta@example.com")
                .persist();

        Course course = createCourse()
                .withOwner(owner)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(owner)
                .asOwner()
                .persist();

        TACourseAssignment taAssignment = createTACourseAssignment()
                .withCourse(course)
                .withTA(ta)
                .persist();

        return new TestCourseSetup(owner, ta, course, taAssignment);
    }

    private Map<String, Object> validCreateTAConstraintRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("constraintType", "HARD");
        body.put("startDateTime", "2026-01-20T08:00:00");
        body.put("endDateTime", "2026-01-20T10:00:00");
        body.put("isWeeklyRecurring", false);
        return body;
    }

    private Map<String, Object> validUpdateTAConstraintRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("constraintType", "SOFT");
        body.put("startDateTime", "2026-01-21T08:00:00");
        body.put("endDateTime", "2026-01-21T10:00:00");
        body.put("isWeeklyRecurring", true);
        return body;
    }

    private Map<String, Object> validReplaceTAConstraintsRequest() {
        List<Map<String, Object>> requests = new ArrayList<>();

        Map<String, Object> first = new LinkedHashMap<>();
        first.put("taCourseConstraintId", null);
        first.put("constraintType", "HARD");
        first.put("startDateTime", "2026-01-20T08:00:00");
        first.put("endDateTime", "2026-01-20T10:00:00");
        first.put("isWeeklyRecurring", false);

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("taCourseConstraintId", null);
        second.put("constraintType", "SOFT");
        second.put("startDateTime", "2026-01-21T14:00:00");
        second.put("endDateTime", "2026-01-21T16:00:00");
        second.put("isWeeklyRecurring", true);

        requests.add(first);
        requests.add(second);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("requests", requests);
        return body;
    }

    private record TestCourseSetup(
            User owner,
            User ta,
            Course course,
            TACourseAssignment taAssignment
    ) {
    }
}
