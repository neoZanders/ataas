package com.chalmers.atas.integrationtest;

import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.integrationtest.base.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnnouncementIntegrationTest extends IntegrationTestBase {

    @Test
    void testGetAnnouncements__courseHasAnnouncements__returnsAnnouncements() {
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

        createAnnouncement()
                .withCourse(course)
                .withOwner(cr)
                .withTitle("Important")
                .withBody("Remember the lab")
                .withSendByEmail(false)
                .persist();

        asCurrentUser(cr)
                .get("/api/courses/{courseId}/announcements", course.getCourseId())
                .expect(status().isOk())
                .body("$", hasSize(1))
                .body("$[0].id", notNullValue())
                .body("$[0].owner.userId", cr.getUserId().toString())
                .body("$[0].owner.email", cr.getEmail())
                .body("$[0].owner.name", cr.getName())
                .body("$[0].owner.userType", "CR")
                .body("$[0].title", "Important")
                .body("$[0].body", "Remember the lab")
                .body("$[0].sendByEmail", false)
                .body("$[0].createdAt", notNullValue());
    }

    @Test
    void testGetAnnouncements__nonExistingCourse__returnsClientError() {
        User cr = createUser()
                .asCR()
                .persist();

        asCurrentUser(cr)
                .get("/api/courses/{courseId}/announcements", UUID.randomUUID())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateAnnouncement__CRCreatesValidAnnouncement__returnsAnnouncement() {
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
                .post("/api/courses/{courseId}/announcements", validCreateAnnouncementRequest(), course.getCourseId())
                .expect(status().isOk())
                .body("id", notNullValue())
                .body("owner.userId", cr.getUserId().toString())
                .body("owner.email", cr.getEmail())
                .body("owner.name", cr.getName())
                .body("owner.userType", "CR")
                .body("title", "New announcement")
                .body("body", "This is visible to the course.")
                .body("sendByEmail", false)
                .body("createdAt", notNullValue());
    }

    @Test
    void testCreateAnnouncement__TAWithoutPermissionCreatesAnnouncement__returnsClientError() {
        User cr = createUser()
                .asCR()
                .persist();

        User ta = createUser()
                .asTA()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .withCanTACreateAnnouncements(false)
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
                .post("/api/courses/{courseId}/announcements", validCreateAnnouncementRequest(), course.getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateAnnouncement__TAWithPermissionCreatesAnnouncement__returnsAnnouncement() {
        User cr = createUser()
                .asCR()
                .persist();

        User ta = createUser()
                .asTA()
                .persist();

        Course course = createCourse()
                .withOwner(cr)
                .withCanTACreateAnnouncements(true)
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
                .post("/api/courses/{courseId}/announcements", validCreateAnnouncementRequest(), course.getCourseId())
                .expect(status().isOk())
                .body("title", "New announcement")
                .body("body", "This is visible to the course.")
                .body("owner.userId", ta.getUserId().toString());
    }

    @Test
    void testCreateAnnouncement__blankTitle__returnsClientError() {
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

        Map<String, Object> body = validCreateAnnouncementRequest();
        body.put("title", "");

        asCurrentUser(cr)
                .post("/api/courses/{courseId}/announcements", body, course.getCourseId())
                .expect(status().is4xxClientError());
    }

    @Test
    void testCreateAnnouncement__unauthenticatedUser__returnsUnauthorizedOrForbidden() {
        asUnauthenticated()
                .post("/api/courses/{courseId}/announcements", validCreateAnnouncementRequest(), UUID.randomUUID())
                .expect(status().is4xxClientError());
    }

    private Map<String, Object> validCreateAnnouncementRequest() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", "New announcement");
        body.put("body", "This is visible to the course.");
        body.put("sendByEmail", false);
        return body;
    }
}
