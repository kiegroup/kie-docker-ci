package org.kie.dockerui.backend.util;

import org.apache.commons.lang.time.StopWatch;

public class Timer {
    private final StopWatch stopWatch = new StopWatch();

    public void start(){
        stopWatch.start();
    }

    public void end() {
        stopWatch.stop();
    }

    public long getTotalTime() {
        return stopWatch.getTime();
    }
}
