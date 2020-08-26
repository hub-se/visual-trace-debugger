package se.de.hu_berlin.informatik.vtdbg.coverage.view;

import com.intellij.coverage.CoverageSuite;
import com.intellij.icons.AllIcons;
import com.intellij.ide.impl.ContentManagerWatcher;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.de.hu_berlin.informatik.vtdbg.coverage.MyCoverageBundle;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceDataManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MyViewManager {
    private static final Logger LOG = Logger.getInstance(MyViewManager.class);
    private final Project myProject;
    public static final String TOOLWINDOW_ID = "Trace Coverage";
    private final ContentManager myContentManager;
    private final Map<String, TraceWindow> myViews = new HashMap<>();

    public static MyViewManager getInstance(@NotNull Project project) {
        return project.getService(MyViewManager.class);
    }

    public MyViewManager(@NotNull Project project) {
        myProject = project;
        RegisterToolWindowTask registerToolWindowTask = RegisterToolWindowTask.closableSecondary(
                TOOLWINDOW_ID,
                MyCoverageBundle.messagePointer("coverage.view.title"),
                AllIcons.Toolwindows.ToolWindowCoverage,
                ToolWindowAnchor.BOTTOM
        );

        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
                .registerToolWindow(registerToolWindowTask);
        myContentManager = toolWindow.getContentManager();
        ContentManagerWatcher.watchContentManager(toolWindow, myContentManager);
    }

    public static String getDisplayName(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
        return baseCoverageSuite != null ? generateName(baseCoverageSuite) : sessionDataFile.getName();
    }

    private static String generateName(@NotNull CoverageSuite baseCoverageSuite) {
        String text = baseCoverageSuite.getCoverageDataFileName();
        int i = text.lastIndexOf(File.separatorChar);
        if (i >= 0) text = text.substring(i + 1);
        i = text.lastIndexOf('.');
        if (i >= 0) text = text.substring(0, i);
        return text;
    }

    public void createToolWindow(String displayName) {
        final TraceWindow view = new TraceWindow(myProject, TraceDataManager.getInstance(myProject), displayName);
        myViews.put(displayName, view);
        Content content = myContentManager.getFactory().createContent(view.getContent(), displayName, true);
        myContentManager.addContent(content);
        myContentManager.setSelectedContent(content);

        activateToolwindow(view, true);
    }

    public void activateToolwindow(TraceWindow traceWindow, boolean requestFocus) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(TOOLWINDOW_ID);
        if (requestFocus) {
            myContentManager.setSelectedContent(myContentManager.getContent(traceWindow.getContent()));
            LOG.assertTrue(toolWindow != null);
            toolWindow.activate(null, false);
        }
    }

    public void closeView(String displayName) {
        TraceWindow oldView = myViews.remove(displayName);
        if (oldView != null) {
            Content content = myContentManager.getContent(oldView.getContent());
            ApplicationManager.getApplication().invokeLater(() -> {
                if (content != null) {
                    myContentManager.removeContent(content, false);
                }
            });
        }
    }

}
