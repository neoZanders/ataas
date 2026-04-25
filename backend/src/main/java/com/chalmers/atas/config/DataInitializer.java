package com.chalmers.atas.config;

import com.chalmers.atas.domain.announcement.Announcement;
import com.chalmers.atas.domain.announcement.AnnouncementRepository;
import com.chalmers.atas.domain.course.Course;
import com.chalmers.atas.domain.course.CourseRepository;
import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.coursesession.CourseSessionRepository;
import com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType;
import com.chalmers.atas.domain.courseassignment.CourseAssignmentStatus;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignment;
import com.chalmers.atas.domain.crcourseassignment.CRCourseAssignmentRepository;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignmentRepository;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraintRepository;
import com.chalmers.atas.domain.tacoursesessionconstraint.TACourseSessionConstraint.ConstraintType;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final AnnouncementRepository announcementRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final CourseRepository courseRepository;
    private final CRCourseAssignmentRepository crCourseAssignmentRepository;
    private final TACourseAssignmentRepository taCourseAssignmentRepository;
    private final TACourseSessionConstraintRepository taCourseSessionConstraintRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void initializeData(ApplicationReadyEvent ignored) {
        if (userRepository.count() > 0) {
            return;
        }

        // Users

        User dejana = User.of(
                "dej@test.se",
                passwordEncoder.encode("dejana123"),
                "Dejana",
                User.UserType.CR
        );
        userRepository.save(dejana);

        User sebastiaanCR = User.of(
                "seb@test.se",
                passwordEncoder.encode("sebastiaan123"),
                "Sebastiaan",
                User.UserType.CR
        );
        userRepository.save(sebastiaanCR);

        User sebastiaanTA = User.of(
                "seb@student.test.se",
                passwordEncoder.encode("sebastiaan123"),
                "Sebastiaan",
                User.UserType.TA
        );
        userRepository.save(sebastiaanTA);

        User yakov = User.of(
                "yak@student.test.se",
                passwordEncoder.encode("yakov123"),
                "Yakov",
                User.UserType.TA
        );
        userRepository.save(yakov);

        User levi = User.of(
                "lev@student.test.se",
                passwordEncoder.encode("levi123"),
                "Levi",
                User.UserType.TA
        );
        userRepository.save(levi);

        User alberic = User.of(
                "alb@student.test.se",
                passwordEncoder.encode("alberic123"),
                "Alberic",
                User.UserType.TA
        );
        userRepository.save(alberic);

        User phoebe = User.of(
                "pho@student.test.se",
                passwordEncoder.encode("phoebe123"),
                "Phoebe",
                User.UserType.TA
        );
        userRepository.save(phoebe);

        User noelle = User.of(
                "noe@student.test.se",
                passwordEncoder.encode("noelle123"),
                "Noelle",
                User.UserType.TA
        );
        userRepository.save(noelle);

        // Courses

        Course vol101 = Course.of(
                "VOL101",
                dejana,
                "This course is about volcano eruptions",
                true,
                true,
                LocalDate.of(2026, 3, 19),
                LocalDate.of(2026, 6, 10)
        );
        courseRepository.save(vol101);

        // CRCourseAssignments

        crCourseAssignmentRepository.save(CRCourseAssignment.of(
                dejana,
                vol101,
                CourseAssignmentStatus.OWNER
        ));

        crCourseAssignmentRepository.save(CRCourseAssignment.of(
                sebastiaanCR,
                vol101,
                CourseAssignmentStatus.INVITED
        ));

        // TACourseAssignment

        TACourseAssignment sebTAAssignment = TACourseAssignment.of(
                sebastiaanTA,
                vol101,
                CourseAssignmentStatus.INVITED,
                67,
                120,
                CourseSessionType.GRADING,
                CourseSessionType.LABORATION,
                CourseSessionType.HELP,
                CourseSessionType.EXERCISE,
                null
        );
        taCourseAssignmentRepository.save(sebTAAssignment);

        TACourseAssignment yakAssignment = TACourseAssignment.of(
                yakov,
                vol101,
                CourseAssignmentStatus.INVITED,
                51,
                80,
                CourseSessionType.LABORATION,
                CourseSessionType.GRADING,
                CourseSessionType.HELP,
                CourseSessionType.EXERCISE,
                true
        );
        taCourseAssignmentRepository.save(yakAssignment);

        TACourseAssignment levAssignment = TACourseAssignment.of(
                levi,
                vol101,
                CourseAssignmentStatus.INVITED,
                51,
                80,
                CourseSessionType.GRADING,
                CourseSessionType.LABORATION,
                CourseSessionType.HELP,
                CourseSessionType.EXERCISE,
                null
        );
        taCourseAssignmentRepository.save(levAssignment);

        TACourseAssignment albAssignment = TACourseAssignment.of(
                alberic,
                vol101,
                CourseAssignmentStatus.INVITED,
                51,
                80,
                CourseSessionType.LABORATION,
                CourseSessionType.GRADING,
                CourseSessionType.HELP,
                CourseSessionType.EXERCISE,
                true
        );
        taCourseAssignmentRepository.save(albAssignment);

        TACourseAssignment phoAssignment = TACourseAssignment.of(
                phoebe,
                vol101,
                CourseAssignmentStatus.INVITED,
                51,
                80,
                CourseSessionType.LABORATION,
                CourseSessionType.GRADING,
                CourseSessionType.HELP,
                CourseSessionType.EXERCISE,
                false
        );
        taCourseAssignmentRepository.save(phoAssignment);

        TACourseAssignment noeAssignment = TACourseAssignment.of(
                noelle,
                vol101,
                CourseAssignmentStatus.INVITED,
                51,
                80,
                CourseSessionType.LABORATION,
                CourseSessionType.GRADING,
                CourseSessionType.HELP,
                CourseSessionType.EXERCISE,
                true
        );
        taCourseAssignmentRepository.save(noeAssignment);
        
        // CourseSessions

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 1, 19, 8, 0),
                LocalDateTime.of(2026, 1, 19, 10, 0),
                CourseSession.CourseSessionType.LABORATION,
                2,
                3,
                true
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 1, 21, 13, 0),
                LocalDateTime.of(2026, 1, 21, 15, 0),
                CourseSession.CourseSessionType.LABORATION,
                3,
                4,
                true
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 1, 23, 15, 0),
                LocalDateTime.of(2026, 1, 23, 17, 0),
                CourseSession.CourseSessionType.LABORATION,
                2,
                4,
                true
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 9, 8, 0),
                LocalDateTime.of(2026, 2, 9, 10, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 9, 10, 0),
                LocalDateTime.of(2026, 2, 9, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 9, 13, 0),
                LocalDateTime.of(2026, 2, 9, 15, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 9, 15, 0),
                LocalDateTime.of(2026, 2, 9, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 10, 8, 0),
                LocalDateTime.of(2026, 2, 10, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 10, 13, 0),
                LocalDateTime.of(2026, 2, 10, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 23, 8, 0),
                LocalDateTime.of(2026, 2, 23, 10, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 23, 10, 0),
                LocalDateTime.of(2026, 2, 23, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 23, 13, 0),
                LocalDateTime.of(2026, 2, 23, 15, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 23, 15, 0),
                LocalDateTime.of(2026, 2, 23, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 24, 8, 0),
                LocalDateTime.of(2026, 2, 24, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 2, 24, 13, 0),
                LocalDateTime.of(2026, 2, 24, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 9, 8, 0),
                LocalDateTime.of(2026, 3, 9, 10, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 9, 10, 0),
                LocalDateTime.of(2026, 3, 9, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 9, 13, 0),
                LocalDateTime.of(2026, 3, 9, 15, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 9, 15, 0),
                LocalDateTime.of(2026, 3, 9, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 10, 8, 0),
                LocalDateTime.of(2026, 3, 10, 12, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 10, 13, 0),
                LocalDateTime.of(2026, 3, 10, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                1,
                3,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 19, 8, 0),
                LocalDateTime.of(2026, 3, 19, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                4,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 20, 8, 0),
                LocalDateTime.of(2026, 3, 20, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                3,
                6,
                false
        ));

        courseSessionRepository.save(CourseSession.of(
                vol101,
                LocalDateTime.of(2026, 3, 21, 8, 0),
                LocalDateTime.of(2026, 3, 21, 17, 0),
                CourseSession.CourseSessionType.GRADING,
                2,
                6,
                false
        ));

        // TACourseSessionConstraint 

        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                sebTAAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 19, 10, 00),
                LocalDateTime.of(2026, 01, 19, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                sebTAAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 21, 10, 00),
                LocalDateTime.of(2026, 01, 21, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                sebTAAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 23, 10, 00),
                LocalDateTime.of(2026, 01, 23, 15, 00),
                true
        ));

        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                yakAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 19, 8, 00),
                LocalDateTime.of(2026, 01, 19, 12, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                yakAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 20, 8, 00),
                LocalDateTime.of(2026, 01, 20, 12, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                yakAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 21, 10, 00),
                LocalDateTime.of(2026, 01, 21, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                yakAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 23, 8, 00),
                LocalDateTime.of(2026, 01, 23, 12, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                yakAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 03, 16, 8, 00),
                LocalDateTime.of(2026, 03, 21, 17, 00),
                false
        ));

        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 19, 10, 00),
                LocalDateTime.of(2026, 01, 19, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 21, 13, 00),
                LocalDateTime.of(2026, 01, 21, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 22, 10, 00),
                LocalDateTime.of(2026, 01, 22, 12, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 19, 8, 00),
                LocalDateTime.of(2026, 01, 19, 10, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 20, 8, 00),
                LocalDateTime.of(2026, 01, 20, 10, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 21, 8, 00),
                LocalDateTime.of(2026, 01, 21, 10, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 22, 8, 00),
                LocalDateTime.of(2026, 01, 22, 10, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 23, 8, 00),
                LocalDateTime.of(2026, 01, 23, 10, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 16, 8, 00),
                LocalDateTime.of(2026, 03, 16, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 17, 8, 00),
                LocalDateTime.of(2026, 03, 17, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 18, 8, 00),
                LocalDateTime.of(2026, 03, 18, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 19, 8, 00),
                LocalDateTime.of(2026, 03, 19, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 20, 8, 00),
                LocalDateTime.of(2026, 03, 20, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                levAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 21, 8, 00),
                LocalDateTime.of(2026, 03, 21, 17, 00),
                false
        ));

        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 20, 8, 00),
                LocalDateTime.of(2026, 01, 20, 10, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 21, 10, 00),
                LocalDateTime.of(2026, 01, 21, 12, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 23, 8, 00),
                LocalDateTime.of(2026, 01, 23, 10, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 30, 8, 00),
                LocalDateTime.of(2026, 01, 30, 10, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 21, 13, 00),
                LocalDateTime.of(2026, 01, 21, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 19, 8, 00),
                LocalDateTime.of(2026, 01, 19, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 23, 13, 00),
                LocalDateTime.of(2026, 01, 23, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 16, 8, 00),
                LocalDateTime.of(2026, 03, 16, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 17, 8, 00),
                LocalDateTime.of(2026, 03, 17, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 18, 8, 00),
                LocalDateTime.of(2026, 03, 18, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 03, 19, 8, 00),
                LocalDateTime.of(2026, 03, 19, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 03, 20, 8, 00),
                LocalDateTime.of(2026, 03, 20, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                albAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 21, 8, 00),
                LocalDateTime.of(2026, 03, 21, 17, 00),
                false
        ));

        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 22, 15, 00),
                LocalDateTime.of(2026, 01, 22, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 23, 15, 00),
                LocalDateTime.of(2026, 01, 23, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 19, 13, 00),
                LocalDateTime.of(2026, 01, 19, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 20, 13, 00),
                LocalDateTime.of(2026, 01, 20, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 21, 13, 00),
                LocalDateTime.of(2026, 01, 21, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 23, 13, 00),
                LocalDateTime.of(2026, 01, 23, 15, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 19, 15, 00),
                LocalDateTime.of(2026, 01, 19, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 20, 15, 00),
                LocalDateTime.of(2026, 01, 20, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 21, 15, 00),
                LocalDateTime.of(2026, 01, 21, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 22, 15, 00),
                LocalDateTime.of(2026, 01, 22, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 01, 23, 15, 00),
                LocalDateTime.of(2026, 01, 23, 17, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                phoAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 03, 16, 8, 00),
                LocalDateTime.of(2026, 03, 21, 17, 00),
                false
        ));

        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 20, 8, 00),
                LocalDateTime.of(2026, 01, 20, 12, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 21, 8, 00),
                LocalDateTime.of(2026, 01, 21, 10, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.HARD,
                LocalDateTime.of(2026, 01, 23, 8, 00),
                LocalDateTime.of(2026, 01, 23, 12, 00),
                true
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 16, 8, 00),
                LocalDateTime.of(2026, 03, 16, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 17, 8, 00),
                LocalDateTime.of(2026, 03, 17, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 18, 8, 00),
                LocalDateTime.of(2026, 03, 18, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 19, 8, 00),
                LocalDateTime.of(2026, 03, 19, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 20, 8, 00),
                LocalDateTime.of(2026, 03, 20, 17, 00),
                false
        ));
        taCourseSessionConstraintRepository.save(TACourseSessionConstraint.of(
                noeAssignment,
                ConstraintType.SOFT,
                LocalDateTime.of(2026, 03, 21, 8, 00),
                LocalDateTime.of(2026, 03, 21, 17, 00),
                false
        ));

        // Announcements

        announcementRepository.save(Announcement.of(
                vol101,
                dejana,
                "Welcome all TAs",
                "We are going to have our first meeting in the course, to make this easier for everyone, " +
                        "we can meet at lunch Monday 12-13, if anyone can’t make it please contact me asap.",
                true));




        // STA888 Course
        // Users
        User olivia = User.of(
                "oli@student.test.se",
                passwordEncoder.encode("olivia123"),
                "Olívia",
                User.UserType.TA
                );
        userRepository.save(olivia);

        User zephyrus = User.of(
                "zep@student.test.se",
                passwordEncoder.encode("zephyrus123"),
                "Zephyrus",
                User.UserType.TA
                );
        userRepository.save(zephyrus);

        // Course
        Course sta888 = Course.of(
                "STA888",
                sebastiaanCR,
                "STARS",
                true,
                true,
                LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 6, 6)
        );
        courseRepository.save(sta888);

        // CRCourseAssignments
        crCourseAssignmentRepository.save(CRCourseAssignment.of(
                sebastiaanCR,
                sta888,
                CourseAssignmentStatus.OWNER
        ));

        // TACourseAssignment

        TACourseAssignment oliTAAssignment = TACourseAssignment.of(
                olivia,
                sta888,
                CourseAssignmentStatus.INVITED,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        taCourseAssignmentRepository.save(oliTAAssignment);

        TACourseAssignment zepTAAssignment = TACourseAssignment.of(
                zephyrus,
                sta888,
                CourseAssignmentStatus.INVITED,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        taCourseAssignmentRepository.save(zepTAAssignment);
        

    }
}
