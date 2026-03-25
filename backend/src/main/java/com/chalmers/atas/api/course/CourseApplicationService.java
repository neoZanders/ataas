package com.chalmers.atas.api.course;

import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionHandler;
import com.chalmers.atas.domain.course.CourseService;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentService;
import com.chalmers.atas.domain.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseApplicationService {

    private final CourseService courseService;
    private final CRCourseAssignmentService crCourseAssignmentService;
    private final TransactionHandler transactionHandler;

    public Result<CourseResponse> createCourse(CreateCourseRequest request, CurrentUser currentUser) {
        return transactionHandler.executeInTransaction(() ->
                courseService.createCourse(
                        currentUser.getUser(),
                        request.getCourseCode(),
                        request.getDescription(),
                        request.getCanTASeeAllSchedules(),
                        request.getCanTACreateAnnouncements(),
                        request.getStartDate(),
                        request.getEndDate()
                ).flatMap(course ->
                        crCourseAssignmentService.createOwnerAssignment(
                                currentUser.getUser(),
                                course
                        ).map(ignored -> course)
                ).map(CourseResponse::of)
        );
    }

    public Result<List<CourseResponse>> getCourses(CurrentUser currentUser) {
            return courseService.getCourses(currentUser.getUser())
                    .map(courses ->
                            courses.stream().map(CourseResponse::of).toList());
    }

    public Result<CourseResponse> archiveCourse(UUID courseId, CurrentUser currentUser) {
        return courseService.archiveCourse(courseId, currentUser.getUser()).map(CourseResponse::of);
    }

    public Result<Void> deleteCourse(UUID courseId, CurrentUser currentUser) {
        return courseService.deleteCourse(courseId, currentUser.getUser());
    }

    public Result<CourseResponse> updateCourse(UUID courseId, UpdateCourseRequest request, CurrentUser currentUser) {
        return courseService.updateCourse(
                courseId,
                currentUser.getUser(),
                request.getDescription(),
                request.getCanTASeeAllSchedules(),
                request.getCanTACreateAnnouncements()
        ).map(CourseResponse::of);
    }
}
