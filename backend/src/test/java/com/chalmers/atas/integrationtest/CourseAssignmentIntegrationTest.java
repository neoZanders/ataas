package com.chalmers.atas.integrationtest;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.integrationtest.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseAssignmentIntegrationTest extends IntegrationTestBase {

    @Test
    void testJoinCourse__TAJoinsExistingCourse__returnsOk() {
        User owner = createUser()
                .withEmail("owner-cr@example.com")
                .withName("Owner CR")
                .withUserType(User.UserType.CR)
                .persist();
        User ta = createUser()
                .withEmail("joining-ta@example.com")
                .withName("Joining TA")
                .withUserType(User.UserType.TA)
                .persist();
        Course course = createCourse().withOwner(owner).persist();

        createTACourseAssignment().asInvited().withCourse(course).withTA(ta).persist();

        asCurrentUser(ta)
                .post("/api/courses/{courseId}/course-assignments/join", Map.of(), course.getCourseId())
                .expect(status().isOk());
    }

    @Test
    void testJoinCourse__sameTAJoinsTwice__returnsClientError() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .persist();

        asCurrentUser(ta)
                .post("/api/courses/{courseId}/course-assignments/join", Map.of(), setup.course().getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testJoinCourse__nonExistingCourse__returnsClientError() {
        User ta = createUser()
                .asTA()
                .persist();

        asCurrentUser(ta)
                .post("/api/courses/{courseId}/course-assignments/join", Map.of(), UUID.randomUUID())
                .expect(status().is4xxClientError());
    }

    @Test
    void testInviteCR__ownerInvitesExistingCR__returnsOk() {
        TestCourseUsers setup = createCourseWithOwner();

        User invitedCR = createUser()
                .asCR()
                .withEmail("invited-cr@example.com")
                .persist();

        asCurrentUser(setup.owner())
                .post(
                        "/api/courses/{courseId}/course-assignments/cr-invitations",
                        Map.of("crEmail", invitedCR.getEmail()),
                        setup.course().getCourseId()
                )
                .expect(status().isOk());
    }

    @Test
    void testInviteCR__TAInvitesCR__returnsClientError() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .persist();

        User invitedCR = createUser()
                .asCR()
                .withEmail("invited-cr@example.com")
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .persist();

        asCurrentUser(ta)
                .post(
                        "/api/courses/{courseId}/course-assignments/cr-invitations",
                        Map.of("crEmail", invitedCR.getEmail()),
                        setup.course().getCourseId()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testInviteTA__ownerInvitesExistingTA__returnsOk() {
        TestCourseUsers setup = createCourseWithOwner();

        User invitedTA = createUser()
                .asTA()
                .withEmail("invited-ta@example.com")
                .persist();

        asCurrentUser(setup.owner())
                .post(
                        "/api/courses/{courseId}/course-assignments/ta-invitations",
                        Map.of("taEmail", invitedTA.getEmail()),
                        setup.course().getCourseId()
                )
                .expect(status().isOk());
    }

    @Test
    void testInviteTA__unknownEmail__returnsClientError() {
        TestCourseUsers setup = createCourseWithOwner();

        asCurrentUser(setup.owner())
                .post(
                        "/api/courses/{courseId}/course-assignments/ta-invitations",
                        Map.of("taEmail", "does-not-exist@example.com"),
                        setup.course().getCourseId()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testGetAssignments__courseHasCRAndTA__returnsAssignments() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .withName("Test TA")
                .withEmail("test-ta@example.com")
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .withMinHours(10)
                .withMaxHours(20)
                .persist();

        asCurrentUser(setup.owner())
                .get("/api/courses/{courseId}/course-assignments", setup.course().getCourseId())
                .expect(status().isOk())
                .body("courseId", setup.course().getCourseId().toString())
                .body("crCourseAssignments", hasSize(greaterThanOrEqualTo(1)))
                .body("crCourseAssignments[0].status", "OWNER")
                .body("crCourseAssignments[0].user.userId", setup.owner().getUserId().toString())
                .body("crCourseAssignments[0].user.email", setup.owner().getEmail())
                .body("crCourseAssignments[0].user.name", setup.owner().getName())
                .body("crCourseAssignments[0].user.userType", "CR")
                .body("taCourseAssignments", hasSize(1))
                .body("taCourseAssignments[0].ta.userId", ta.getUserId().toString())
                .body("taCourseAssignments[0].ta.email", ta.getEmail())
                .body("taCourseAssignments[0].ta.name", ta.getName())
                .body("taCourseAssignments[0].ta.userType", "TA")
                .body("taCourseAssignments[0].status", "JOINED")
                .body("taCourseAssignments[0].minHours", 10)
                .body("taCourseAssignments[0].maxHours", 20);
    }

    @Test
    void testGetAssignments__usernameFilterMatchesTAName__returnsFilteredAssignments() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .withName("Specific TA")
                .withEmail("specific-ta@example.com")
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .persist();

        asCurrentUser(setup.owner())
                .get("/api/courses/{courseId}/course-assignments?username=Specific TA", setup.course().getCourseId())
                .expect(status().isOk())
                .body("courseId", setup.course().getCourseId().toString())
                .body("taCourseAssignments[*].ta.name", hasItem("Specific TA"));
    }

    @Test
    void testGetTAAssignment__existingTAInCourse__returnsTAAssignment() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .withName("Test TA")
                .withEmail("test-ta@example.com")
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .withMinHours(10)
                .withMaxHours(20)
                .persist();

        asCurrentUser(setup.owner())
                .get(
                        "/api/courses/{courseId}/course-assignments/tas/{taId}/details",
                        setup.course().getCourseId(),
                        ta.getUserId()
                )
                .expect(status().isOk())
                .body("taCourseAssignmentId", notNullValue())
                .body("ta.userId", ta.getUserId().toString())
                .body("ta.email", ta.getEmail())
                .body("ta.name", ta.getName())
                .body("ta.userType", "TA")
                .body("status", "JOINED")
                .body("minHours", 10)
                .body("maxHours", 20);
    }

    @Test
    void testGetTAAssignment__nonExistingTA__returnsClientError() {
        TestCourseUsers setup = createCourseWithOwner();

        asCurrentUser(setup.owner())
                .get(
                        "/api/courses/{courseId}/course-assignments/tas/{taId}/details",
                        setup.course().getCourseId(),
                        UUID.randomUUID()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testUpdateTAAssignment__ownerUpdatesHoursAndPreferences__returnsForbidden() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .persist();

        asCurrentUser(setup.owner())
                .patch(
                        "/api/courses/{courseId}/course-assignments/tas/{taId}",
                        validUpdateTAAssignmentRequest(),
                        setup.course().getCourseId(),
                        ta.getUserId()
                )
                .expect(status().isForbidden());
    }

    @Test
    void testUpdateTAAssignment__minHoursGreaterThanMaxHours__returnsClientError() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .persist();

        Map<String, Object> body = validUpdateTAAssignmentRequest();
        body.put("minHours", 50);
        body.put("maxHours", 10);

        asCurrentUser(setup.owner())
                .patch(
                        "/api/courses/{courseId}/course-assignments/tas/{taId}",
                        body,
                        setup.course().getCourseId(),
                        ta.getUserId()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testDeleteTAAssignment__ownerDeletesTA__returnsOk() {
        TestCourseUsers setup = createCourseWithOwner();

        User ta = createUser()
                .asTA()
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(ta)
                .persist();

        asCurrentUser(setup.owner())
                .delete(
                        "/api/courses/{courseId}/course-assignments/tas/{taId}",
                        setup.course().getCourseId(),
                        ta.getUserId()
                )
                .expect(status().isOk());
    }

    @Test
    void testDeleteTAAssignment__TADeletesAnotherTA__returnsClientError() {
        TestCourseUsers setup = createCourseWithOwner();

        User taToDelete = createUser()
                .asTA()
                .persist();

        User actingTA = createUser()
                .asTA()
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(taToDelete)
                .persist();

        createTACourseAssignment()
                .withCourse(setup.course())
                .withTA(actingTA)
                .persist();

        asCurrentUser(actingTA)
                .delete(
                        "/api/courses/{courseId}/course-assignments/tas/{taId}",
                        setup.course().getCourseId(),
                        taToDelete.getUserId()
                )
                .expect(status().is4xxClientError());
    }

    @Test
    void testDeleteCRAssignment__ownerDeletesJoinedCR__returnsOk() {
        TestCourseUsers setup = createCourseWithOwner();

        User otherCR = createUser()
                .asCR()
                .persist();

        createCRCourseAssignment()
                .withCourse(setup.course())
                .withCR(otherCR)
                .asJoined()
                .persist();

        asCurrentUser(setup.owner())
                .delete(
                        "/api/courses/{courseId}/course-assignments/crs/{crId}",
                        setup.course().getCourseId(),
                        otherCR.getUserId()
                )
                .expect(status().isOk());
    }

    @Test
    void testDeleteCRAssignment__ownerDeletesSelfOwnerAssignment__returnsClientError() {
        TestCourseUsers setup = createCourseWithOwner();

        asCurrentUser(setup.owner())
                .delete(
                        "/api/courses/{courseId}/course-assignments/crs/{crId}",
                        setup.course().getCourseId(),
                        setup.owner().getUserId()
                )
                .expect(status().is4xxClientError());
    }

    private TestCourseUsers createCourseWithOwner() {
        User owner = createUser()
                .asCR()
                .withName("Owner CR")
                .withEmail("owner-cr@example.com")
                .persist();

        Course course = createCourse()
                .withOwner(owner)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(owner)
                .asOwner()
                .persist();

        return new TestCourseUsers(owner, course);
    }

    private Map<String, Object> validUpdateTAAssignmentRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("minHours", 15);
        body.put("maxHours", 40);
        body.put("sessionTypePreference1", "GRADING");
        body.put("sessionTypePreference2", "LABORATION");
        body.put("sessionTypePreference3", "HELP");
        body.put("sessionTypePreference4", "EXERCISE");
        body.put("isCompactSchedule", true);
        return body;
    }

    private record TestCourseUsers(
            User owner,
            Course course
    ) {
    }
}
