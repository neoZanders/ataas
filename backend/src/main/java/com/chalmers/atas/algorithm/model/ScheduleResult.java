package com.chalmers.atas.algorithm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

//This is the result of scheduling as a single session,

public class ScheduleResult {
    private final Sessions session;
    private final List<UUID> assignedTaIds;

    public ScheduleResult(Sessions session, List<UUID> assignedTaIds){
        this.session = session;
        List<UUID> tmp;
        if(assignedTaIds != null)
            tmp = new ArrayList<>(assignedTaIds);
        else
            tmp = new ArrayList<>();
        this.assignedTaIds = Collections.unmodifiableList(tmp);
    }

    public boolean isFullyStaffed() {
        return assignedTaIds.size() >= session.getMinTa();
    }

    public int getRemainingSlots() {
        return Math.max(0, session.getMinTa() - assignedTaIds.size());
    }

    public boolean isAtMaxCapacity() {
        return assignedTaIds.size() >= session.getMaxTA();
    }


    //getter
    public Sessions getSession() {
        return session;
    }

    public List<UUID> getAssignedTaIds() {
        return assignedTaIds;
    }
}
