package com.chalmers.atas.domain.tacourseassignment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
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
    public TransactionalResult<Void> updateAssignment(TACourseAssignment taCourseAssignment){
        if (!taCourseAssignment.getStatus().equals(CourseAssignmentStatus.JOINED)) {
            return TransactionalResult.rollbackFor(
                ErrorCode.INVALID_COURSE_ASSIGNMENT_STATUS.toError()
            );
        }

        taCourseAssignmentRepository.save(taCourseAssignment);
        return TransactionalResult.ok();
    }

    public Result<List<TACourseAssignment>> getCourseAssignment(Course course){
        return Result.ok(taCourseAssignmentRepository.findAllByCourse(course));
    }

    @Transactional
    public TransactionalResult<Void> deleteAssignment(TACourseAssignment taCourseAssignment){
        taCourseAssignmentRepository.delete(taCourseAssignment);
        return TransactionalResult.ok();
    }

    public boolean isUserTaOfCourse(User user, Course course) {
        return taCourseAssignmentRepository.existsByTaAndCourse(user, course);
    }

}
