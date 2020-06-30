package se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.listener;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface TraceDataListener extends EventListener {

    Topic<TraceDataListener> TOPIC = Topic.create("trace data events", TraceDataListener.class);

    /**
     * Invoked when new trace data has been added to the TraceDataManager
     *
     * @param displayName the ID of the newly added data
     */
    void newTraceData(String displayName);

}
