package se.de.hu_berlin.informatik.vtdbg.coverage.runner.listener;

import com.intellij.coverage.CoverageSuite;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.EventListener;

public interface TraceDataListener extends EventListener {

    Topic<TraceDataListener> TOPIC = Topic.create("trace data events", TraceDataListener.class);

    /**
     * Invoked when new trace data has been generated
     * @param sessionDataFile path to the coverage data file, used to access the trace data
     * @param baseCoverageSuite the selected coverage suite
     */
    void newTraceData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite);

}
