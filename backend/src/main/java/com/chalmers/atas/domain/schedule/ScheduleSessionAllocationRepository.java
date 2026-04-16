ackage com.chalmers.atas.domain.schedule;

import com.chalmers.atas.domain.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduleSessionAllocationRepository extends JpaRepository<ScheduleSessionAllocation, UUID> {
    List<ScheduleSessionAllocation> findByCourse(Course course);
}
