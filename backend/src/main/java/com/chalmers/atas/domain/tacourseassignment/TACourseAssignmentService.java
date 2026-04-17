package com.chalmers.atas.domain.tacourseassignment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType;
import com.chalmers.atas.domain.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TACourseAssignmentService {
    private final TACourseAssignmentRepository taCourseAssignmentRepository;

    @Transactional
    public TransactionalResult<Void> createInviteAssignment(User ta, Course course){
        if (!ta.getUserType().equals(User.UserType.TA)) {
            return TransactionalResult.rollbackFor(
                ErrorCode.USER_NOT_TEACHING_ASSISTANT.toError()
            );
        }
        if (taCourseAssignmentRepository.findByTaAndCourse(ta, course).isPresent()) {
            return TransactionalResult.rollbackFor(
                ErrorCode.USER_ALREADY_HAS_COURSE_ASSIGNMENT.toError()
            );
        }

        taCourseAssignmentRepository.save(
            TACourseAssignment.of(
                ta,
                course,
                CourseAssignmentStatus.INVITED,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        );

        return TransactionalResult.ok();
    }

    @Transactional
    public TransactionalResult<Void> join(TACourseAssignment taCourseAssignment){
        if (!taCourseAssignment.getStatus().equals(CourseAssignmentStatus.INVITED)) {
            return TransactionalResult.rollbackFor(
                ErrorCode.INVALID_COURSE_ASSIGNMENT_STATUS.toError()
            );
        }

        taCourseAssignment.setStatus(CourseAssignmentStatus.JOINED);
        taCourseAssignmentRepository.save(taCourseAssignment);
        return TransactionalResult.ok();
    }

    @Transactional
    public TransactionalResult<TACourseAssignment> updateAssignment(
            TACourseAssignment taCourseAssignment,
            Integer minHours,
            Integer maxHours,
            CourseSessionType sessionTypePreference1,
            CourseSessionType sessionTypePreference2,
            CourseSessionType sessionTypePreference3,
            CourseSessionType sessionTypePreference4,
            Boolean isCompactSchedule
    ){
        if (!taCourseAssignment.getStatus().equals(CourseAssignmentStatus.JOINED)) {
            return TransactionalResult.rollbackFor(
                ErrorCode.INVALID_COURSE_ASSIGNMENT_STATUS.toError()
            );
        }

        Integer updatedMinHours = minHours != null ? minHours : taCourseAssignment.getMinHours();
        Integer updatedMaxHours = maxHours != null ? maxHours : taCourseAssignment.getMaxHours();
        CourseSessionType updatedPreference1 =
            sessionTypePreference1 != null ? sessionTypePreference1 : taCourseAssignment.getSessionTypePreference1();
        CourseSessionType updatedPreference2 =
            sessionTypePreference2 != null ? sessionTypePreference2 : taCourseAssignment.getSessionTypePreference2();
        CourseSessionType updatedPreference3 =
            sessionTypePreference3 != null ? sessionTypePreference3 : taCourseAssignment.getSessionTypePreference3();
        CourseSessionType updatedPreference4 =
            sessionTypePreference4 != null ? sessionTypePreference4 : taCourseAssignment.getSessionTypePreference4();

        if (updatedMinHours != null && updatedMaxHours != null && updatedMaxHours < updatedMinHours) {
            return TransactionalResult.rollbackFor(
                ErrorCode.BAD_REQUEST.toError("maxHours cannot be less than minHours")
            );
        }

        if (hasDuplicatePreferences(
                updatedPreference1,
                updatedPreference2,
                updatedPreference3,
                updatedPreference4
        )) {
            return TransactionalResult.rollbackFor(
                ErrorCode.BAD_REQUEST.toError("session type preferences must be unique")
            );
        }

        if (minHours != null) {
            taCourseAssignment.setMinHours(minHours);
        }
        if (maxHours != null) {
            taCourseAssignment.setMaxHours(maxHours);
        }
        if (sessionTypePreference1 != null) {
            taCourseAssignment.setSessionTypePreference1(sessionTypePreference1);
        }
        if (sessionTypePreference2 != null) {
            taCourseAssignment.setSessionTypePreference2(sessionTypePreference2);
        }
        if (sessionTypePreference3 != null) {
            taCourseAssignment.setSessionTypePreference3(sessionTypePreference3);
        }
        if (sessionTypePreference4 != null) {
            taCourseAssignment.setSessionTypePreference4(sessionTypePreference4);
        }
        if (isCompactSchedule != null) {
            taCourseAssignment.setIsCompactSchedule(isCompactSchedule);
        }

        return TransactionalResult.ok(taCourseAssignmentRepository.save(taCourseAssignment));
    }

    private boolean hasDuplicatePreferences(CourseSessionType... preferences) {
        Set<CourseSessionType> seenPreferences = new HashSet<>();
        for (CourseSessionType preference : preferences) {
            if (preference != null && !seenPreferences.add(preference)) {
                return true;
            }
        }
        return false;
    }

    public Result<List<TACourseAssignment>> getCourseAssignments(Course course){
        return Result.ok(taCourseAssignmentRepository.findAllByCourse(course));
    }

    public Result<List<TACourseAssignment>> getTAAssignments(User ta){
        return Result.ok(taCourseAssignmentRepository.findAllByTa(ta));
    }

    public Result<TACourseAssignment> getAssignment(User ta, Course course) {
        return Result.ofOptional(
                taCourseAssignmentRepository.findByTaAndCourse(ta, course),
                ErrorCode.USER_HAS_NOT_JOINED_COURSE.toError()
        );
    }

    @Transactional
    public TransactionalResult<Void> deleteAssignment(TACourseAssignment taCourseAssignment){
        taCourseAssignmentRepository.delete(taCourseAssignment);
        return TransactionalResult.ok();
    }

    public boolean isUserTaOfCourse(User user, Course course) {
        return taCourseAssignmentRepository.existsByTaAndCourseAndStatus(
                user,
                course,
                CourseAssignmentStatus.JOINED
        );
    }

}
