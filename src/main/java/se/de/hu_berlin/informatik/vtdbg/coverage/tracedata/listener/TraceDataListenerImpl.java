package se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import se.de.hu_berlin.informatik.vtdbg.coverage.view.MyViewManager;

public class TraceDataListenerImpl implements TraceDataListener {

    private final Project myProject;

    public TraceDataListenerImpl(@NotNull Project project) {
        myProject = project;
    }

    @Override
    public void newTraceData(String displayName) {
        ApplicationManager.getApplication().invokeLater(() -> {
            MyViewManager myViewManager = MyViewManager.getInstance(myProject);
            // close old content with same name, if any
            myViewManager.closeView(displayName);

            // add new content to UI
            myViewManager.createToolWindow(displayName);
        });
    }
}
