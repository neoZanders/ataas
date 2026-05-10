package com.chalmers.atas.domain.schedulesessionallocation;

import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.schedule.Schedule;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_session_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSessionAllocation implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "schedule_session_allocation_id", columnDefinition = "UUID")
    private UUID scheduleSessionAllocationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseSession.CourseSessionType courseSessionType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ta_course_assignment_id", nullable = false)
    private TACourseAssignment taCourseAssignment;

    public static ScheduleSessionAllocation of(
            Schedule schedule,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            CourseSession.CourseSessionType courseSessionType,
            TACourseAssignment taCourseAssignment
    ) {
        ScheduleSessionAllocation allocation = new ScheduleSessionAllocation();
        allocation.schedule = schedule;
        allocation.startDateTime = startDateTime;
        allocation.endDateTime = endDateTime;
        allocation.courseSessionType = courseSessionType;
        allocation.taCourseAssignment = taCourseAssignment;
        return allocation;
    }
}