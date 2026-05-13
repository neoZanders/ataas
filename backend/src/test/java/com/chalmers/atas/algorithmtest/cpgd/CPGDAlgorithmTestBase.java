package com.chalmers.atas.algorithmtest.cpgd;

import com.chalmers.atas.algorithm.modelCpGd.CPScheduler;
import com.chalmers.atas.algorithm.modelCpGd.Greedy;
import com.chalmers.atas.algorithm.modelCpGd.SchedulerChannel;
import com.chalmers.atas.algorithmtest.AlgorithmTestBase;

public abstract class CPGDAlgorithmTestBase extends AlgorithmTestBase {
    protected final CPScheduler cpScheduler = new CPScheduler();
    protected final Greedy greedy = new Greedy();
    protected final SchedulerChannel schedulerChannel = new SchedulerChannel();
}
