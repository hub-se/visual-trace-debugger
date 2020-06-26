package se.de.hu_berlin.informatik.vtdbg.coverage.runner.listener;

import com.intellij.coverage.CoverageSuite;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.de.hu_berlin.informatik.vtdbg.coverage.TraceDataManager;

import java.io.File;

public class TraceDataListenerImpl implements TraceDataListener {

    private final Project myProject;

    public TraceDataListenerImpl(@NotNull Project project) {
        myProject = project;
    }

    @Override
    public void newTraceData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
        // add new trace data to the manager service
        myProject.getService(TraceDataManager.class).addTraceData(sessionDataFile, baseCoverageSuite);
    }
}
