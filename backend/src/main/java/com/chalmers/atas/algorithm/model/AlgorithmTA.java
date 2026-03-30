package com.chalmers.atas.algorithm.model;

import com.chalmers.atas.domain.coursesession.CourseSession;

import java.util.List;
import java.util.UUID;

public record AlgorithmTA(
        UUID taAssignmentId,
        int minHours,
        int maxHours,
        List<CourseSession.CourseSessionType> sessionTypePreferences,
        boolean preferCompactSchedule
) {}
