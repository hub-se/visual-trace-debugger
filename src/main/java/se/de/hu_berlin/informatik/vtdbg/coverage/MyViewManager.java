package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.coverage.CoverageBundle;
import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageOptionsProvider;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.view.CoverageView;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.icons.AllIcons;
import com.intellij.ide.impl.ContentManagerWatcher;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.View;

public class MyViewManager {
    private final Project myProject;
    public static final String ID = "MyToolWindowFactory";
    private final ContentManager myContentManager;
    private boolean myReady;
    JPanel content;

    public MyViewManager(@NotNull Project project) {
        myProject = project;
        RegisterToolWindowTask registerToolWindowTask = RegisterToolWindowTask.closableSecondary(
                ID,
                CoverageBundle.messagePointer("coverage.view.title"),
                AllIcons.General.Modified,
                ToolWindowAnchor.RIGHT
        );

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(registerToolWindowTask);
        myContentManager = toolWindow.getContentManager();
        ContentManagerWatcher.watchContentManager(toolWindow, myContentManager);
    }

    public void createToolWindow(boolean defaultFileProvider) {
        TraceWindow view = new TraceWindow(myProject);
        Content content = myContentManager.getFactory().createContent(view.getContent(), "Trace", true);
        myContentManager.addContent(content);
        myContentManager.setSelectedContent(content);
        if (CoverageOptionsProvider.getInstance(myProject).activateViewOnRun() && defaultFileProvider) {
            activateToolwindow(view, true);
        }
    }

    public void activateToolwindow(TraceWindow traceWindow, boolean requestFocus) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(ID);
        if (requestFocus) {
            myContentManager.setSelectedContent(myContentManager.getContent(traceWindow.getContent()));
            toolWindow.activate(null, false);
           // traceWindow.setTextPane1(IDEATraceCoverageRunner.result);
//            MessageBus messageBus = myProject.getMessageBus();
//            messageBus.connect().subscribe(ChangeActionNotifier.CHANGE_ACTION_TOPIC, new ChangeActionNotifier() {
//                @Override
//                public void changeTrace(String text) {
//                    traceWindow.setTextPane1(text);
//                }
//            });
        }
    }

    public static MyViewManager getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, MyViewManager.class);
    }

    void closeView() {
        JPanel oldView = content;
        if (oldView != null) {
            Content content = myContentManager.getContent(oldView);
            ApplicationManager.getApplication().invokeLater(() -> {
                if (content != null) {
                    myContentManager.removeContent(content, false);
                }
            });
        }
        setReady(false);
    }


    public boolean isReady() {
        return myReady;
    }

    public void setReady(boolean ready) {
        myReady = ready;
    }
}
