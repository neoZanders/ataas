package com.chalmers.atas.domain.crcourseassignment;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CRCourseAssignmentService {
    private final CRCourseAssignmentRepository crCourseAssignmentRepository;

    @Transactional
    public TransactionalResult<Void> createOwnerAssignment(User cr, Course course) {
        if (!cr.getUserType().equals(User.UserType.CR)) {
            return TransactionalResult.rollbackFor(
                    ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError()
            );
        }

        if (crCourseAssignmentRepository.findByCrAndCourse(cr, course).isPresent()) {
            return TransactionalResult.rollbackFor(
                    ErrorCode.USER_ALREADY_HAS_COURSE_ASSIGNMENT.toError()
            );
        }

        crCourseAssignmentRepository.save(
                CRCourseAssignment.of(cr, course, CRCourseAssignment.CRAssignmentStatus.OWNER)
        );
        return TransactionalResult.ok();
    }

    @Transactional
    public TransactionalResult<Void> createInviteAssignment(User cr, Course course) {
        if (!cr.getUserType().equals(User.UserType.CR)) {
            return TransactionalResult.rollbackFor(
                    ErrorCode.USER_NOT_COURSE_RESPONSIBLE.toError()
            );
        }

        if (crCourseAssignmentRepository.findByCrAndCourse(cr, course).isPresent()) {
            return TransactionalResult.rollbackFor(
                    ErrorCode.USER_ALREADY_HAS_COURSE_ASSIGNMENT.toError()
            );
        }

        crCourseAssignmentRepository.save(
                CRCourseAssignment.of(cr, course, CRCourseAssignment.CRAssignmentStatus.INVITED)
        );
        return TransactionalResult.ok();
    }

    @Transactional
    public TransactionalResult<Void> join(CRCourseAssignment crCourseAssignment) {
        if (!crCourseAssignment.getStatus().equals(CRCourseAssignment.CRAssignmentStatus.INVITED)) {
            return TransactionalResult.rollbackFor(
                    ErrorCode.INVALID_COURSE_ASSIGNMENT_STATUS.toError()
            );
        }

        crCourseAssignment.setStatus(CRCourseAssignment.CRAssignmentStatus.JOINED);
        crCourseAssignmentRepository.save(crCourseAssignment);
        return TransactionalResult.ok();
    }

    public boolean isUserCrOfCourse(User user, Course course) {
        return crCourseAssignmentRepository.existsByCrAndCourseAndStatusIn(
                user,
                course,
                Set.of(
                        CRCourseAssignment.CRAssignmentStatus.OWNER,
                        CRCourseAssignment.CRAssignmentStatus.JOINED
                )
        );
    }

    public Result<List<CRCourseAssignment>> getCourseAssignments(Course course) {
        return Result.ok(crCourseAssignmentRepository.findAllByCourse(course));
    }

    @Transactional
    public TransactionalResult<Void> deleteAssignment(CRCourseAssignment crCourseAssignment) {
        if (crCourseAssignment.getStatus().equals(CRCourseAssignment.CRAssignmentStatus.OWNER)) {
            return TransactionalResult.rollbackFor(
                    ErrorCode.CANNOT_DELETE_COURSE_OWNER.toError()
            );
        }

        if (!crCourseAssignment.getStatus().equals(CRCourseAssignment.CRAssignmentStatus.JOINED)) {
            return TransactionalResult.rollbackFor(
                    ErrorCode.INVALID_COURSE_ASSIGNMENT_STATUS.toError()
            );
        }

        crCourseAssignmentRepository.delete(crCourseAssignment);
        return TransactionalResult.ok();
    }
}
