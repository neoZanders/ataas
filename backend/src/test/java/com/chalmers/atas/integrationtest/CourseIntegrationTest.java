package com.chalmers.atas.integrationtest;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.integrationtest.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CourseIntegrationTest extends IntegrationTestBase {

    @Test
    void testCreateCourse__validCRRequest__returnsCreatedCourse() {
        User cr = createUser()
                .asCR()
                .persist();

        asCurrentUser(cr)
                .post("/api/courses", validCreateCourseRequest())
                .expect(status().isOk())
                .body("courseId", notNullValue())
                .body("courseCode", "DAT123")
                .body("owner.userId", cr.getUserId().toString())
                .body("owner.email", cr.getEmail())
                .body("owner.name", cr.getName())
                .body("owner.userType", "CR")
                .body("description", "Test course")
                .body("canTASeeAllSchedules", true)
                .body("canTACreateAnnouncements", false)
                .body("startDate", "2026-01-19")
                .body("endDate", "2026-03-22");
    }

    @Test
    void testCreateCourse__unauthenticatedUser__returnsUnauthorizedOrForbidden() {
        asUnauthenticated()
                .post("/api/courses", validCreateCourseRequest())
                .expect(status().is4xxClientError());
    }

    @Test
    void testGetCourses__userHasOwnerCourse__returnsCoursesWithAssignmentStatus() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .withCourseCode("DAT123")
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        asCurrentUser(cr)
                .get("/api/courses")
                .expect(status().isOk())
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("$[*].course.courseCode", hasItem("DAT123"))
                .body("$[*].assignmentStatus", hasItem("OWNER"));
    }

    @Test
    void testGetCourse__existingCourseAndOwner__returnsCourseDetails() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .withCourseCode("DAT123")
                .withDescription("Test course")
                .withCanTASeeAllSchedules(true)
                .withCanTACreateAnnouncements(false)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        asCurrentUser(cr)
                .get("/api/courses/{courseId}/details", course.getCourseId())
                .expect(status().isOk())
                .body("courseId", course.getCourseId().toString())
                .body("courseCode", "DAT123")
                .body("owner.userId", cr.getUserId().toString())
                .body("owner.email", cr.getEmail())
                .body("owner.name", cr.getName())
                .body("owner.userType", "CR")
                .body("description", "Test course")
                .body("canTASeeAllSchedules", true)
                .body("canTACreateAnnouncements", false);
    }

    @Test
    void testGetCourse__nonExistingCourse__returnsClientError() {
        User cr = createUser()
                .asCR()
                .persist();

        asCurrentUser(cr)
                .get("/api/courses/{courseId}/details", UUID.randomUUID())
                .expect(status().is4xxClientError());
    }

    @Test
    void testUpdateCourse__ownerUpdatesCourse__returnsUpdatedCourse() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .withCourseCode("DAT123")
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        asCurrentUser(cr)
                .patch("/api/courses/{courseId}", validUpdateCourseRequest(), course.getCourseId())
                .expect(status().isOk())
                .body("courseId", course.getCourseId().toString())
                .body("courseCode", "DAT123")
                .body("description", "Updated description")
                .body("canTASeeAllSchedules", false)
                .body("canTACreateAnnouncements", true);
    }

    @Test
    void testUpdateCourse__TAUpdatesCourse__returnsClientError() {
        User cr = createUser()
                .asCR()
                .persist();

        User ta = createUser()
                .asTA()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        createTACourseAssignment()
                .withCourse(course)
                .withTA(ta)
                .persist();

        asCurrentUser(ta)
                .patch("/api/courses/{courseId}", validUpdateCourseRequest(), course.getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testArchiveCourse__ownerArchivesCourse__returnsArchivedCourse() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        asCurrentUser(cr)
                .put("/api/courses/{courseId}/archive", Map.of(), course.getCourseId())
                .expect(status().isOk())
                .body("courseId", course.getCourseId().toString());
    }

    @Test
    void testDeleteCourse__ownerDeletesCourse__returnsOk() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        asCurrentUser(cr)
                .delete("/api/courses/{courseId}", course.getCourseId())
                .expect(status().isOk());
    }

    @Test
    void testGetCourseSessions__courseHasSessions__returnsCourseSessions() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        CourseSession session = createCourseSession()
                .withCourse(course)
                .persist();

        asCurrentUser(cr)
                .get("/api/courses/{courseId}/course-sessions", course.getCourseId())
                .expect(status().isOk())
                .body("$", hasSize(1))
                .body("$[0].courseSessionId", session.getCourseSessionId().toString())
                .body("$[0].courseId", course.getCourseId().toString())
                .body("$[0].courseSessionType", "LABORATION");
    }

    @Test
    void testCreateCourseSession__ownerCreatesValidSession__returnsCreatedSession() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        asCurrentUser(cr)
                .post("/api/courses/{courseId}/course-sessions", validCreateCourseSessionRequest(), course.getCourseId())
                .expect(status().isOk())
                .body("courseSessionId", notNullValue())
                .body("courseId", course.getCourseId().toString())
                .body("startDateTime", "2026-01-19T10:00:00")
                .body("endDateTime", "2026-01-19T12:00:00")
                .body("courseSessionType", "LABORATION")
                .body("minTAs", 1)
                .body("maxTAs", 2)
                .body("isWeeklyRecurring", false);
    }

    @Test
    void testCreateCourseSession__endBeforeStart__returnsClientError() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        Map<String, Object> body = validCreateCourseSessionRequest();
        body.put("startDateTime", "2026-01-19T12:00:00");
        body.put("endDateTime", "2026-01-19T10:00:00");

        asCurrentUser(cr)
                .post("/api/courses/{courseId}/course-sessions", body, course.getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testDeleteCourseSession__ownerDeletesExistingSession__returnsOk() {
        User cr = createUser()
                .asCR()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .persist();

        createCRCourseAssignment()
                .withCourse(course)
                .withCR(cr)
                .asOwner()
                .persist();

        CourseSession session = createCourseSession()
                .withCourse(course)
                .persist();

        asCurrentUser(cr)
                .delete(
                        "/api/courses/{courseId}/course-sessions/{courseSessionId}",
                        course.getCourseId(),
                        session.getCourseSessionId()
                )
                .expect(status().isOk());
    }

    private Map<String, Object> validCreateCourseRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("courseCode", "DAT123");
        body.put("description", "Test course");
        body.put("canTASeeAllSchedules", true);
        body.put("canTACreateAnnouncements", false);
        body.put("startDate", "2026-01-19");
        body.put("endDate", "2026-03-22");
        return body;
    }

    private Map<String, Object> validUpdateCourseRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("courseCode", "DAT123");
        body.put("description", "Updated description");
        body.put("canTASeeAllSchedules", false);
        body.put("canTACreateAnnouncements", true);
        body.put("startDate", "2026-01-19");
        body.put("endDate", "2026-03-22");
        return body;
    }

    private Map<String, Object> validCreateCourseSessionRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("startDateTime", "2026-01-19T10:00:00");
        body.put("endDateTime", "2026-01-19T12:00:00");
        body.put("courseSessionType", "LABORATION");
        body.put("minTAs", 1);
        body.put("maxTAs", 2);
        body.put("isWeeklyRecurring", false);
        return body;
    }
}