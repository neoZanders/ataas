package com.chalmers.atas.algorithm.choco;

import com.chalmers.atas.algorithm.AlgorithmType;
import com.chalmers.atas.algorithm.model.AlgorithmRequest;
import com.chalmers.atas.algorithm.model.AlgorithmResult;
import com.chalmers.atas.algorithm.model.AlgorithmSession;
import com.chalmers.atas.algorithm.model.AlgorithmTA;
import com.chalmers.atas.common.Result;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.search.loop.lns.neighbors.AdaptiveNeighborhood;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChocoCustomLNSAlgorithmService extends ChocoNaiveAlgorithmService {

    public ChocoCustomLNSAlgorithmService(
            @Value("${app.alg.timeout-ms}") long timeoutMs,
            @Value("${app.alg.plateau-ms}") long plateauMs,
            @Value("${app.alg.should-optimize:true}") boolean shouldOptimize) {
        super(timeoutMs, plateauMs, shouldOptimize);
    }

    @Override
    public AlgorithmType getType() {
        return AlgorithmType.CUSTOM_LNS_CHOCO;
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

        List<IntVar> decisionVarsList = new ArrayList<>();
        for (int t = 0; t < tas.size(); t++) {
            for (int s = 0; s < sessions.size(); s++) {
                decisionVarsList.add(x[t][s]);
            }
        }
        IntVar[] decisionVars = decisionVarsList.toArray(new IntVar[0]);

        SessionWeekMapping weekMapping = buildSessionWeekMapping(sessions);

        solver.setLNS(
                new AdaptiveNeighborhood(
                        1L,
                        INeighborFactory.random(decisionVars),
                        new TANeighbor(
                                decisionVars,
                                tas.size(),
                                sessions.size(),
                                Math.min(tas.size(), 2),
                                1
                        ),
                        new WeekNeighbor(
                                decisionVars,
                                tas.size(),
                                sessions.size(),
                                weekMapping.sessionWeekIndex(),
                                weekMapping.weekCount(),
                                Math.min(weekMapping.weekCount(), 1),
                                1
                        )
                ),
                new FailCounter(solver, 1000)
        );

        return solve(solver, penaltyModel, x, tas, sessions);
    }
}
