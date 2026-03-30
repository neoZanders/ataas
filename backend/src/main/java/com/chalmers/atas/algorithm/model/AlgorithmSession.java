package com.chalmers.atas.algorithm.model;

import com.chalmers.atas.domain.coursesession.CourseSession;

import java.util.UUID;

public record AlgorithmSession(
        UUID sessionId,
        AlgorithmTimeInterval timeInterval,
        CourseSession.CourseSessionType type,
        int minTAs,
        int maxTAs
) {}
