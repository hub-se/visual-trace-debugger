package se.de.hu_berlin.informatik.vtdbg.coverage.view;

import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.rt.coverage.traces.ClassLineEncoding;
import com.intellij.rt.coverage.traces.SequiturUtils;
import com.intellij.ui.components.JBScrollPane;
import de.unisb.cs.st.sequitur.input.InputSequence;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceDataManager;
import se.de.hu_berlin.informatik.vtdbg.utils.EditorUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the execution Traces of selected tests
 *
 * @author Dorottya Kregl
 * @author kregldor@hu-berlin.de
 * @version 1.0
 * @since 1.0
 */

public class TraceWindow {

    private JPanel content;
    private JTabbedPane tabs;
    private JButton button1;
    private JButton colorButton;


    List<? extends SMTestProxy> testResults;

    private final Project project;

    private Map<Integer, String> idToClassNameMap;
    private List<Pair<Long,InputSequence<Long>>> sequences;

    private static final Logger LOG = Logger.getInstance(TraceWindow.class);

    public TraceWindow(Project project, TraceDataManager data, String displayName) {
        this.project = project;
        readData(data, displayName);
        fillForm();
        showButtonDemo();
        showColorButtonDemo();
    }

    /**
     * @return everything (that is visible) from the TraceWindow
     */
    public JPanel getContent() {
        return content;
    }


    private void showButtonDemo() {
        button1.addActionListener(e -> EditorUtils.navigateToClass(project, "com.company.Main", 13));

    }

    private void showColorButtonDemo() {
        colorButton.addActionListener(e -> EditorUtils.colorRandomLine(project));

    }

    /**
     * collects and processes trace data
     * @param dataManager the trace data manager which provides the data
     * @param displayName the respective data id
     */
    private void readData(TraceDataManager dataManager, String displayName) {
        // sample processing of provided execution trace
        Pair<Map<Long, byte[]>, Map<Integer, String>> traceData = dataManager.getTraceData(displayName);
        if (traceData != null) {
            idToClassNameMap = traceData.getSecond();
            Map<Long, byte[]> indexedTraces = traceData.getFirst();
            sequences = new ArrayList<>(indexedTraces.size());
            for (Map.Entry<Long, byte[]> entry : indexedTraces.entrySet()) {
                try {
                    sequences.add(new Pair<>(
                            entry.getKey(), // <- this is the thread ID
                            SequiturUtils.getInputSequenceFromByteArray(entry.getValue(), Long.class)));
                } catch (IOException | ClassNotFoundException e) {
                    LOG.error("Could not read trace!", e);
                }
            }
        } else {
            LOG.warn("No trace data...");
        }
    }

    private void fillForm() {
        // this is only for testing and should be replaced by a chart view or something like that @Enrico
        if (sequences != null) {
            for (Pair<Long, InputSequence<Long>> sequence : sequences) {
                StringBuilder sb = new StringBuilder();
                for (Long encodedStatement : sequence.getSecond()) {
                    sb.append(ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap))
                            .append(": ")
                            .append(ClassLineEncoding.getLineNUmber(encodedStatement))
                            .append(System.lineSeparator());
                }

                //printing the traces here only for testing / showing purposes
                JTextPane textPane = new JTextPane();
                textPane.setText(sb.toString());
                JBScrollPane jbScrollPane = new JBScrollPane(textPane);
                tabs.add("Thread " + sequence.getFirst(), jbScrollPane);
            }
        }
    }

}
