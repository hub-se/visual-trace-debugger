package se.de.hu_berlin.informatik.vtdbg.samples.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import org.jetbrains.annotations.NotNull;
import se.de.hu_berlin.informatik.vtdbg.samples.services.MyProjectService;

public class MyToolwindowListener implements ToolWindowManagerListener {
    private final Project project;

    private int changeCounter = 0;

    public MyToolwindowListener(Project project) {
        this.project = project;
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        // handle the state change
//        final FindManager findmanager = FindManager.getInstance(project);
//        FindModel findmodel = findmanager.getFindNextModel();
//        if (findmodel == null) {
//            findmodel = findmanager.getFindInFileModel();
//        }
//        findmodel.setSearchHighlighters(true);
//        findmanager.setFindWasPerformed();
//        findmanager.setFindNextModel(findmodel);

        ++changeCounter;
        // comment

        toolWindowManager.invokeLater(() -> {
            final WindowManager windowManager = WindowManager.getInstance();
            final StatusBar statusBar = windowManager.getStatusBar(project);
            if (statusBar != null) {
                statusBar.setInfo(String.format("Status Change Count: %d", changeCounter));
            }
        });
    }

    @Override
    public void toolWindowShown(@NotNull String id, @NotNull ToolWindow toolWindow) {
        MyProjectService service = project.getService(MyProjectService.class);

        service.someServiceMethod("Tool Window " + id, toolWindow.isVisible());
    }

}