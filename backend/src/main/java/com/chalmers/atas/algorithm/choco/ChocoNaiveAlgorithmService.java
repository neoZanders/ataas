package com.chalmers.atas.algorithm.choco;

import com.chalmers.atas.algorithm.AlgorithmService;
import com.chalmers.atas.algorithm.AlgorithmType;
import com.chalmers.atas.algorithm.model.*;
import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.chocosolver.solver.Model.MINIMIZE;

@Service
public class ChocoNaiveAlgorithmService implements AlgorithmService {

    protected final long timeoutMs;
    protected final long plateauMs;
    protected final boolean shouldOptimize;

    public ChocoNaiveAlgorithmService(
            @Value("${app.alg.timeout-ms}") long timeoutMs,
            @Value("${app.alg.plateau-ms}") long plateauMs,
            @Value("${app.alg.should-optimize:true}") boolean shouldOptimize
    ) {
        this.timeoutMs = timeoutMs;
        this.plateauMs = plateauMs;
        this.shouldOptimize = shouldOptimize;
    }

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.NAIVE_CHOCO;
    }

    @Override
    public Result<AlgorithmResult> runAlgorithm(AlgorithmRequest request) {
        List<AlgorithmSession> sessions = request.sessions();
        List<AlgorithmTA> tas = request.tas();

        Result<Void> validationResult = validateRequest(request, sessions, tas);
        if (!validationResult.isSuccess()) {
            return Result.error(validationResult.getError());
        }

        Model model = new Model("TA Scheduling");

        Map<UUID, Integer> taIndexByAssignmentId = buildTAIndex(tas);

        BoolVar[][] x = createAssignmentVariables(model, tas, sessions);

        postSessionCoverageConstraints(model, x, tas, sessions);
        postTAHourConstraints(model, x, tas, sessions);
        postNoOverlapConstraints(model, x, tas, sessions);
        postHardConstraints(model, x, request.hardConstraints(), sessions, taIndexByAssignmentId);

        PenaltyModel penaltyModel = buildPenaltyModel(
                model,
                x,
                request,
                tas,
                sessions,
                taIndexByAssignmentId
        );

        Solver solver = getBaseSolver(model, penaltyModel);

        return solve(solver, penaltyModel, x, tas, sessions);
    }

    protected Result<AlgorithmResult> solve(
            Solver solver,
            PenaltyModel penaltyModel,
            BoolVar[][] x,
            List<AlgorithmTA> tas,
            List<AlgorithmSession> sessions) {

        Solution sol;
        if (shouldOptimize) {
            if (timeoutMs > 0) {
                solver.limitTime(timeoutMs);
            }
            sol = solver.findOptimalSolution(penaltyModel.totalPenalty(), MINIMIZE);
        } else {
            sol = solver.findSolution();
        }

        if (sol == null) {
            if (shouldOptimize && timeoutMs > 0) {
                return Result.error(ErrorCode.SCHEDULE_GENERATION_TIMED_OUT.toError());
            } else {
                return Result.error(ErrorCode.SCHEDULE_INFEASIBLE.toError());
            }
        }

        List<AlgorithmSessionAllocation> allocations = extractAllocations(sol, x, tas, sessions);

        return Result.ok(new AlgorithmResult(
                allocations,
                sol.getIntVal(penaltyModel.totalPenalty()),
                true,
                solver.isObjectiveOptimal()
        ));
    }

    protected Result<Void> validateRequest(
            AlgorithmRequest request,
            List<AlgorithmSession> sessions,
            List<AlgorithmTA> tas
    ) {
        Set<UUID> taIds = new HashSet<>();
        for (AlgorithmTA ta : tas) {
            if (!taIds.add(ta.taAssignmentId())) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "Duplicate TA assignment id: " + ta.taAssignmentId()
                ));
            }
            if (ta.minHours() > ta.maxHours()) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "TA minHours cannot exceed maxHours for TA assignment: " + ta.taAssignmentId()
                ));
            }
            if (ta.sessionTypePreferences().size() != 4 ||
                    ta.sessionTypePreferences().stream().distinct().count() != 4) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "TA model preferences does not contain ranking for all session types for " +
                                "TA assignment: " + ta.taAssignmentId()
                ));
            }
        }

        Set<UUID> sessionIds = new HashSet<>();
        for (AlgorithmSession session : sessions) {
            if (!sessionIds.add(session.sessionId())) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "Duplicate session id: " + session.sessionId()
                ));
            }
            if (session.minTAs() > session.maxTAs()) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "Session minTAs cannot exceed maxTAs for session: " + session.sessionId()
                ));
            }
        }

        for (AlgorithmHardSessionConstraint constraint : request.hardConstraints()) {
            if (!taIds.contains(constraint.taAssignmentId())) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "Hard constraint refers to unknown TA assignment: " + constraint.taAssignmentId()
                ));
            }
        }

        for (AlgorithmSoftSessionConstraint constraint : request.softConstraints()) {
            if (!taIds.contains(constraint.taAssignmentId())) {
                return Result.error(ErrorCode.BAD_REQUEST.toError(
                        "Soft constraint refers to unknown TA assignment: " + constraint.taAssignmentId()
                ));
            }
        }

        return Result.ok();
    }

    protected Map<UUID, Integer> buildTAIndex(List<AlgorithmTA> tas) {
        Map<UUID, Integer> taIndexByAssignmentId = new HashMap<>();
        for (int t = 0; t < tas.size(); t++) {
            taIndexByAssignmentId.put(tas.get(t).taAssignmentId(), t);
        }
        return taIndexByAssignmentId;
    }

    protected BoolVar[][] createAssignmentVariables(
            Model model,
            List<AlgorithmTA> tas,
            List<AlgorithmSession> sessions
    ) {
        BoolVar[][] x = new BoolVar[tas.size()][sessions.size()];
        for (int t = 0; t < tas.size(); t++) {
            for (int s = 0; s < sessions.size(); s++) {
                x[t][s] = model.boolVar("x_" + t + "_" + s);
            }
        }
        return x;
    }

    protected void postSessionCoverageConstraints(
            Model model,
            BoolVar[][] x,
            List<AlgorithmTA> tas,
            List<AlgorithmSession> sessions
    ) {
        for (int s = 0; s < sessions.size(); s++) {
            BoolVar[] assigned = new BoolVar[tas.size()];
            for (int t = 0; t < tas.size(); t++) {
                assigned[t] = x[t][s];
            }

            model.sum(assigned, ">=", sessions.get(s).minTAs()).post();
            model.sum(assigned, "<=", sessions.get(s).maxTAs()).post();
        }
    }

    protected void postTAHourConstraints(
            Model model,
            BoolVar[][] x,
            List<AlgorithmTA> tas,
            List<AlgorithmSession> sessions
    ) {
        IntVar[] taHours = new IntVar[tas.size()];

        for (int t = 0; t < tas.size(); t++) {
            int[] durations = new int[sessions.size()];
            BoolVar[] vars = new BoolVar[sessions.size()];

            for (int s = 0; s < sessions.size(); s++) {
                durations[s] = sessions.get(s).timeInterval().getDurationMinutes();
                vars[s] = x[t][s];
            }

            taHours[t] = model.intVar(
                    "hours_" + t,
                    tas.get(t).minHours() * 60,
                    tas.get(t).maxHours() * 60
            );
            model.scalar(vars, durations, "=", taHours[t]).post();
        }
    }

    protected void postNoOverlapConstraints(
            Model model,
            BoolVar[][] x,
            List<AlgorithmTA> tas,
            List<AlgorithmSession> sessions
    ) {
        for (int t = 0; t < tas.size(); t++) {
            for (int s1 = 0; s1 < sessions.size(); s1++) {
                for (int s2 = s1 + 1; s2 < sessions.size(); s2++) {
                    if (sessions.get(s1).timeInterval().isOverlappingWith(sessions.get(s2).timeInterval())) {
                        model.sum(new BoolVar[]{x[t][s1], x[t][s2]}, "<=", 1).post();
                    }
                }
            }
        }
    }

    protected void postHardConstraints(
            Model model,
            BoolVar[][] x,
            List<AlgorithmHardSessionConstraint> hardConstraints,
            List<AlgorithmSession> sessions,
            Map<UUID, Integer> taIndexByAssignmentId
    ) {
        for (AlgorithmHardSessionConstraint constraint : hardConstraints) {
            int t = taIndexByAssignmentId.get(constraint.taAssignmentId());

            for (int s = 0; s < sessions.size(); s++) {
                if (constraint.timeInterval().isOverlappingWith(sessions.get(s).timeInterval())) {
                    model.arithm(x[t][s], "=", 0).post();
                }
            }
        }
    }

    protected PenaltyModel buildPenaltyModel(
            Model model,
            BoolVar[][] x,
            AlgorithmRequest request,
            List<AlgorithmTA> tas,
            List<AlgorithmSession> sessions,
            Map<UUID, Integer> taIndexByAssignmentId
    ) {
        List<BoolVar> penaltyBools = new ArrayList<>();
        List<Integer> penaltyWeights = new ArrayList<>();

        for (AlgorithmSoftSessionConstraint constraint : request.softConstraints()) {
            int t = taIndexByAssignmentId.get(constraint.taAssignmentId());

            for (int s = 0; s < sessions.size(); s++) {
                if (constraint.timeInterval().isOverlappingWith(sessions.get(s).timeInterval())) {
                    penaltyBools.add(x[t][s]);
                    penaltyWeights.add(constraint.weight());
                }
            }
        }

        for (int t = 0; t < tas.size(); t++) {
            for (int s = 0; s < sessions.size(); s++) {
                int weight = tas.get(t).sessionTypePreferences().indexOf(sessions.get(s).type()) * 10;

                if (weight > 0) {
                    penaltyBools.add(x[t][s]);
                    penaltyWeights.add(weight);
                }
            }
        }

        BoolVar[] penaltyVars = penaltyBools.toArray(new BoolVar[0]);
        int[] weights = penaltyWeights.stream().mapToInt(Integer::intValue).toArray();

        IntVar totalPenalty;
        if (penaltyVars.length == 0) {
            totalPenalty = model.intVar("totalPenalty", 0);
        } else {
            int maxPenalty = Arrays.stream(weights).sum();
            totalPenalty = model.intVar("totalPenalty", 0, maxPenalty);
            model.scalar(penaltyVars, weights, "=", totalPenalty).post();
        }

        return new PenaltyModel(totalPenalty);
    }

    private List<AlgorithmSessionAllocation> extractAllocations(
            Solution sol,
            BoolVar[][] x,
            List<AlgorithmTA> tas,
            List<AlgorithmSession> sessions
    ) {
        List<AlgorithmSessionAllocation> allocations = new ArrayList<>();

        for (int s = 0; s < sessions.size(); s++) {
            List<UUID> allocatedTAs = new ArrayList<>();
            for (int t = 0; t < tas.size(); t++) {
                if (sol.getIntVal(x[t][s]) == 1) {
                    allocatedTAs.add(tas.get(t).taAssignmentId());
                }
            }
            allocations.add(new AlgorithmSessionAllocation(
                    sessions.get(s).sessionId(),
                    allocatedTAs
            ));
        }

        return allocations;
    }

    protected SessionWeekMapping buildSessionWeekMapping(List<AlgorithmSession> sessions) {
        Map<LocalDate, Integer> weekIndexByMonday = new LinkedHashMap<>();
        int[] sessionWeekIndex = new int[sessions.size()];

        for (int s = 0; s < sessions.size(); s++) {
            LocalDate monday = sessions.get(s)
                    .timeInterval()
                    .getStart()
                    .toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            Integer weekIndex = weekIndexByMonday.computeIfAbsent(monday, k -> weekIndexByMonday.size());

            sessionWeekIndex[s] = weekIndex;
        }

        return new SessionWeekMapping(sessionWeekIndex, weekIndexByMonday.size());
    }

    protected Solver getBaseSolver(Model model, PenaltyModel penaltyModel) {
        Solver solver = model.getSolver();

        AtomicInteger bestPenalty = new AtomicInteger(Integer.MAX_VALUE);
        AtomicLong lastImprovementMs = new AtomicLong(System.currentTimeMillis());

        solver.plugMonitor((IMonitorSolution) () -> {
            int current = penaltyModel.totalPenalty().getValue();
            if (current < bestPenalty.get()) {
                bestPenalty.set(current);
                lastImprovementMs.set(System.currentTimeMillis());
                System.out.println("Improved penalty to " + current + " at " + solver.getTimeCount() + "s");
            }
        });

        solver.limitSearch(() ->
                System.currentTimeMillis() - lastImprovementMs.get() > plateauMs
        );
        return solver;
    }

    protected record PenaltyModel(IntVar totalPenalty) {}

    public record SessionWeekMapping(
            int[] sessionWeekIndex,
            int weekCount
    ) {}
}
