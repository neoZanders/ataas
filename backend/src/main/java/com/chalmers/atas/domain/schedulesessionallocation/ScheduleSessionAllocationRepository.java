package com.chalmers.atas.domain.schedulesessionallocation;

import com.chalmers.atas.domain.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduleSessionAllocationRepository extends JpaRepository<ScheduleSessionAllocation, UUID> {
    List<ScheduleSessionAllocation> findBySchedule(Schedule schedule);

    void deleteBySchedule(Schedule schedule);
}
