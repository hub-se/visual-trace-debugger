package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Factory class for class ToolWindow, built on the model of Factory-pattern
 * since the ToolWindow class cannot anticipate the type of objects it needs to create
 * beforehand directly because it should be the forfeit of a Java Swing object (TraceWindow.form)
 * and so TraceWindow can be connected to the plugin through the Factory
 *
 * @author Dorottya Kregl
 * @author kregldor@hu-berlin.de
 * @version 1.0
 * @since 1.0
 */

public class MyToolWindowFactory implements ToolWindowFactory {
    public static final String ID = "MyToolWindowFactory";
    TraceWindow myToolWindow;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        myToolWindow = new TraceWindow(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(ChangeActionNotifier.CHANGE_ACTION_TOPIC, new ChangeActionNotifier() {
            @Override
            public void changeTrace(Map<Long, byte[]> traces, Map<Integer, String> idToClassNameMap) {

                //traces and idToClassMap should be delivered to ToolWindow
                //so that this data is accessible also from the plugin
                myToolWindow.setTrace(traces, idToClassNameMap);
            }
        });

        messageBus.connect().subscribe(SMTRunnerEventsListener.TEST_STATUS, new MyAnalyticsTestRunnerEventsListener(myToolWindow));
    }
}