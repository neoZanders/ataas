package com.chalmers.atas.domain.schedulesessionallocation;

import com.chalmers.atas.domain.schedule.Schedule;
import com.chalmers.atas.domain.coursesession.CourseSession;
import com.chalmers.atas.domain.tacourseassignment.TACourseAssignment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_session_id", nullable = false)
    private CourseSession courseSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ta_course_assignment_id", nullable = false)
    private TACourseAssignment taCourseAssignment;

    public static ScheduleSessionAllocation of(
            Schedule schedule,
            CourseSession courseSession,
            TACourseAssignment taCourseAssignment
    ) {
        ScheduleSessionAllocation allocation = new ScheduleSessionAllocation();
        allocation.schedule = schedule;
        allocation.courseSession = courseSession;
        allocation.taCourseAssignment = taCourseAssignment;
        return allocation;
    }
}
