package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.coverage.CoverageSuite;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.rt.coverage.traces.ExecutionTraceCollector;
import com.intellij.rt.coverage.traces.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.de.hu_berlin.informatik.vtdbg.coverage.view.MyViewManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public class TraceDataManager {

    private static final Logger LOG = Logger.getInstance(TraceDataManager.class);

    public static TraceDataManager getInstance(@NotNull Project project) {
        return project.getService(TraceDataManager.class);
    }

    private final Project myProject;

    private Map<String, Pair<Map<Long, byte[]>, Map<Integer, String>>> traceData = new HashMap<>();

    public TraceDataManager(@NotNull Project project) {
        myProject = project;
    }

    public void addTraceData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {

        String displayName = MyViewManager.getDisplayName(sessionDataFile, baseCoverageSuite);
        MyViewManager myViewManager = MyViewManager.getInstance(myProject);
        // close old content with same name, if any
        myViewManager.closeView(displayName);

        // load trace data from file and replace previous data, if any
        String file = FileUtils.getFilePathUniqueToSessionFile(
                sessionDataFile, ExecutionTraceCollector.TRACE_FILE_ID);
        try (FileInputStream streamIn = new FileInputStream(file)) {
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            Map<Long, byte[]> traces = (Map<Long, byte[]>) objectinputstream.readObject();
            Map<Integer, String> idToClassNameMap = (Map<Integer, String>) objectinputstream.readObject();

            // add data to "storage"
            traceData.put(displayName, new Pair<>(traces, idToClassNameMap));

            // add new content to UI
            myViewManager.createToolWindow(displayName);
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Could not read file " + file, e);
        }
    }

    public Pair<Map<Long, byte[]>, Map<Integer, String>> getTraceData(String displayName) {
        return traceData.get(displayName);
    }
}
