package com.chalmers.atas.domain.schedule;

import com.chalmers.atas.domain.session.Session;
import com.chalmers.atas.domain.user.User;

import java.util.List;

public interface AlgorithmService {
    List<ScheduleSessionAllocation> generateAllocations(
            Schedule schedule,
            List<Session> sessions,
            List<User> TAs
    );
}
