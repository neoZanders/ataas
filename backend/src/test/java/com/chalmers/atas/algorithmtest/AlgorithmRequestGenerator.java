package com.chalmers.atas.algorithmtest;

import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.domain.coursesession.CourseSession;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static com.chalmers.atas.domain.coursesession.CourseSession.CourseSessionType.*;

public final class AlgorithmRequestGenerator {

    private AlgorithmRequestGenerator() {}

    public record Config(
            String scenarioName,
            long seed,
            LocalDate startDate,
            int weeks,
            int numberOfTAs,
            int sessionsPerWeek,
            int minTAsPerSession,
            int maxTAsPerSession,
            int hardConstraintsPerTA,
            int softConstraintsPerTA,
            int softConstraintWeight
    ) {}

    public static AlgorithmRequest generate(Config config) {
        Random random = new Random(config.seed());

        List<UUID> taIds = generateTAIds(config);
        List<AlgorithmSession> sessions = generateSessions(config, random);

        /*
         * This creates a simple valid baseline assignment.
         * It is not returned, but it is used to make sure generated hard constraints
         * do not accidentally make the problem impossible.
         */
        Map<UUID, List<AlgorithmSession>> baselineAssignments =
                createBaselineAssignments(taIds, sessions, config.minTAsPerSession());

        List<AlgorithmTA> tas = generateTAs(config, taIds, baselineAssignments);
        List<AlgorithmHardSessionConstraint> hardConstraints =
                generateHardConstraints(config, random, taIds, sessions, baselineAssignments);
        List<AlgorithmSoftSessionConstraint> softConstraints =
                generateSoftConstraints(config, random, taIds, sessions);

        return new AlgorithmRequest(
                sessions,
                tas,
                hardConstraints,
                softConstraints
        );
    }

    private static List<UUID> generateTAIds(Config config) {
        List<UUID> result = new ArrayList<>();

        for (int i = 0; i < config.numberOfTAs(); i++) {
            result.add(stableUuid(config.scenarioName() + "_ta_" + i));
        }

        return result;
    }

    private static List<AlgorithmSession> generateSessions(Config config, Random random) {
        List<AlgorithmSession> result = new ArrayList<>();

        CourseSession.CourseSessionType[] types = {
                LABORATION,
                GRADING,
                HELP,
                EXERCISE
        };

        LocalTime[] startTimes = {
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                LocalTime.of(13, 0),
                LocalTime.of(15, 0)
        };

        for (int week = 0; week < config.weeks(); week++) {
            LocalDate weekStart = config.startDate().plusWeeks(week);

            for (int i = 0; i < config.sessionsPerWeek(); i++) {
                DayOfWeek day = DayOfWeek.of(1 + random.nextInt(5));
                LocalTime startTime = startTimes[random.nextInt(startTimes.length)];
                LocalDate date = weekStart.with(day);

                LocalDateTime start = LocalDateTime.of(date, startTime);
                LocalDateTime end = start.plusHours(2);

                CourseSession.CourseSessionType type = types[random.nextInt(types.length)];

                result.add(new AlgorithmSession(
                        stableUuid(config.scenarioName() + "_session_" + week + "_" + i),
                        new AlgorithmTimeInterval(start, end),
                        type,
                        config.minTAsPerSession(),
                        config.maxTAsPerSession()
                ));
            }
        }

        return result.stream()
                .sorted(Comparator.comparing(session -> session.timeInterval().getStart()))
                .toList();
    }

    private static Map<UUID, List<AlgorithmSession>> createBaselineAssignments(
            List<UUID> taIds,
            List<AlgorithmSession> sessions,
            int minTAsPerSession
    ) {
        Map<UUID, List<AlgorithmSession>> assignments = new LinkedHashMap<>();
        taIds.forEach(taId -> assignments.put(taId, new ArrayList<>()));

        int nextTA = 0;

        for (AlgorithmSession session : sessions) {
            int assigned = 0;
            int attempts = 0;

            while (assigned < minTAsPerSession && attempts < taIds.size() * 2) {
                UUID taId = taIds.get(nextTA);
                nextTA = (nextTA + 1) % taIds.size();
                attempts++;

                boolean overlapsExisting = assignments.get(taId).stream()
                        .anyMatch(existing -> existing.timeInterval().isOverlappingWith(session.timeInterval()));

                if (!overlapsExisting) {
                    assignments.get(taId).add(session);
                    assigned++;
                }
            }

            if (assigned < minTAsPerSession) {
                throw new IllegalArgumentException(
                        "Could not create a feasible baseline assignment. " +
                                "Try increasing numberOfTAs or reducing sessionsPerWeek/minTAsPerSession."
                );
            }
        }

        return assignments;
    }

    private static List<AlgorithmTA> generateTAs(
            Config config,
            List<UUID> taIds,
            Map<UUID, List<AlgorithmSession>> baselineAssignments
    ) {
        List<AlgorithmTA> result = new ArrayList<>();

        List<List<CourseSession.CourseSessionType>> preferenceOrders = List.of(
                List.of(LABORATION, GRADING, HELP, EXERCISE),
                List.of(GRADING, LABORATION, HELP, EXERCISE),
                List.of(HELP, LABORATION, GRADING, EXERCISE),
                List.of(EXERCISE, HELP, LABORATION, GRADING)
        );

        for (int i = 0; i < taIds.size(); i++) {
            UUID taId = taIds.get(i);

            int assignedHours = baselineAssignments.get(taId).stream()
                    .mapToInt(AlgorithmRequestGenerator::durationHours)
                    .sum();

            int minHours = Math.max(0, assignedHours / 2);
            int maxHours = Math.max(assignedHours + 10, config.weeks() * 10);
            Boolean preferCompact = i % 3 == 0 ? null : i % 2 == 0;

            result.add(new AlgorithmTA(
                    taId,
                    minHours,
                    maxHours,
                    preferenceOrders.get(i % preferenceOrders.size()),
                    preferCompact
            ));
        }

        return result;
    }

    private static List<AlgorithmHardSessionConstraint> generateHardConstraints(
            Config config,
            Random random,
            List<UUID> taIds,
            List<AlgorithmSession> sessions,
            Map<UUID, List<AlgorithmSession>> baselineAssignments
    ) {
        List<AlgorithmHardSessionConstraint> result = new ArrayList<>();

        for (UUID taId : taIds) {
            int created = 0;
            int attempts = 0;

            while (created < config.hardConstraintsPerTA() && attempts < config.hardConstraintsPerTA() * 20) {
                attempts++;

                AlgorithmSession session = sessions.get(random.nextInt(sessions.size()));

                boolean conflictsWithBaseline = baselineAssignments.get(taId).stream()
                        .anyMatch(assigned -> assigned.timeInterval().isOverlappingWith(session.timeInterval()));

                if (conflictsWithBaseline) {
                    continue;
                }

                result.add(new AlgorithmHardSessionConstraint(
                        taId,
                        session.timeInterval()
                ));

                created++;
            }
        }

        return result;
    }

    private static List<AlgorithmSoftSessionConstraint> generateSoftConstraints(
            Config config,
            Random random,
            List<UUID> taIds,
            List<AlgorithmSession> sessions
    ) {
        List<AlgorithmSoftSessionConstraint> result = new ArrayList<>();

        for (UUID taId : taIds) {
            Set<UUID> usedSessionIds = new HashSet<>();

            while (usedSessionIds.size() < config.softConstraintsPerTA()
                    && usedSessionIds.size() < sessions.size()) {

                AlgorithmSession session = sessions.get(random.nextInt(sessions.size()));

                if (!usedSessionIds.add(session.sessionId())) {
                    continue;
                }

                result.add(new AlgorithmSoftSessionConstraint(
                        taId,
                        session.timeInterval(),
                        config.softConstraintWeight()
                ));
            }
        }

        return result;
    }

    private static int durationHours(AlgorithmSession session) {
        return (int) Duration.between(
                session.timeInterval().getStart(),
                session.timeInterval().getEnd()
        ).toHours();
    }

    private static UUID stableUuid(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }
}