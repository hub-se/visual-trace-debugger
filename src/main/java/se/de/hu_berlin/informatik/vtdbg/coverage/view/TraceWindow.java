package se.de.hu_berlin.informatik.vtdbg.coverage.view;

import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.rt.coverage.traces.SequiturUtils;
import com.intellij.util.messages.MessageBus;
import de.unisb.cs.st.sequitur.input.InputSequence;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import se.de.hu_berlin.informatik.vtdbg.coverage.Score;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceDataManager;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceIterator;
import se.de.hu_berlin.informatik.vtdbg.utils.EditorUtils;
import se.de.hu_berlin.informatik.vtdbg.utils.VirtualHelper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Represents the execution Traces of selected tests
 *
 * @author Dorottya Kregl
 * @author kregldor@hu-berlin.de
 * @version 1.0
 * @since 1.0
 */

public class TraceWindow {

    public static final String TAB_TITLE_PREFIX = "Thread ";
    private JPanel content;
    private JTabbedPane tabs;
    private JButton button1;
    private JButton colorButton;
    private JButton buttonLeft;
    private JButton buttonRight;
    private Color category_1;   //for coloring purposes
    private Color category_2;   //for coloring purposes
    private Color category_3;   //for coloring purposes
    private int category_count; //for coloring purposes
    private Color category_temp;//for coloring purposes
    private String[] tclass;    //for navigation purposes
    private int tlength;        //for navigation purposes
    private int[] tline;        //for navigation purposes

    List<? extends SMTestProxy> testResults;

    private final Project project;

    private Map<Integer, String> idToClassNameMap;
    private Map<Long, InputSequence<Long>> sequences = new HashMap<>();
    private Map<Long, TraceIterator> iterators = new HashMap<>();

    private boolean coloring = false;

    private static final Logger LOG = Logger.getInstance(TraceWindow.class);

    public TraceWindow(Project project, TraceDataManager data, String displayName) {
        this.project = project;
        readData(data, displayName);
        fillForm();
        showButtonDemo();
        showColorButtonDemo();

        registerNextButton();
        registerPreviousButton();
    }

    /**
     * @return everything (that is visible) from the TraceWindow
     */
    public JPanel getContent() {
        return content;
    }

    private void createLineChart(CategoryDataset dataset){
        JFreeChart lineChart = ChartFactory.createLineChart(
                null,
                "Line", "Score",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel( lineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        tabs.addTab("Title", chartPanel);
    }

    private void createBarChart(CategoryDataset dataset) {
        JFreeChart barChart = ChartFactory.createBarChart(
                null,                       //title
                "Line",                   //categoryAxisLabel
                "Score",                    //valueAxisLabel
                dataset,                                //dataset
                PlotOrientation.VERTICAL,               //plot orientation
                false,                             //legend
                true,                            //tooltips
                false);                            //urls
        /* Get instance of CategoryPlot */
        CategoryPlot plot = barChart.getCategoryPlot();
        /* Change Bar colors */
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, category_1);
        renderer.setSeriesPaint(1, category_2);
        renderer.setSeriesPaint(2, category_3);
        ChartPanel chartPanel = new ChartPanel( barChart );
        chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ) );
        tabs.addTab("Title", chartPanel);
    }

    private void registerNextButton() {
        buttonRight.addActionListener(e -> {
            if (DumbService.isDumb(project)) {
                // ignore when not ready
                showNoIndexWarning();
                return;
            }

            try {
                TraceIterator iterator = getTraceIterator(false);
//                Map<String, List<Score>> map = new HashMap<>();
//                map.put("com.company.Main", Arrays.asList(new Score(13, 0.5), new Score(15, 0.2)));
//                map.put("com.company.TestClass", Arrays.asList(new Score(13, 0.1), new Score(21, 0.7)));
//                for (Map.Entry<String, List<Score>> item : map.entrySet()) {
//                    navigateToClass(project, item.getKey(), item.getValue());
//                }
                if (iterator.hasNext()) {
                    Pair<String, Integer> next = iterator.next();
                    EditorUtils.navigateToClass(project, next.first, next.second);
                    EditorUtils.colorClassSBFL(project, next.second, true);
                }
            } catch (NumberFormatException x) {
                LOG.error("Can't parse thread ID from tab title: " + tabs.getTitleAt(tabs.getSelectedIndex()));
            }
        });
    }

    private void showNoIndexWarning() {
        Messages.showMessageDialog(project,
                "please wait for indices to be generated",
                "Error", Messages.getWarningIcon());
    }

    private void registerPreviousButton() {
        buttonLeft.addActionListener(e -> {
            if (DumbService.isDumb(project)) {
                // ignore when not ready
                showNoIndexWarning();
                return;
            }

            try {
                TraceIterator iterator = getTraceIterator(true);

                if (iterator.hasPrevious()) {
                    Pair<String, Integer> previous = iterator.previous();
                    EditorUtils.navigateToClass(project, previous.first, previous.second);
                    EditorUtils.colorClassSBFL(project, previous.second, false);
                }
            } catch (NumberFormatException x) {
                LOG.error("Can't parse thread ID from tab title: " + tabs.getTitleAt(tabs.getSelectedIndex()));
            }
        });
    }

    @NotNull
    private TraceIterator getTraceIterator(boolean reverse) {
        String title = tabs.getTitleAt(tabs.getSelectedIndex());
        long threadId = Long.valueOf(title.substring(TAB_TITLE_PREFIX.length()));

        TraceIterator iterator = iterators.get(threadId);
        if (iterator == null) {
            iterator = new TraceIterator(sequences.get(threadId), idToClassNameMap,
                    reverse ? sequences.get(threadId).getLength() : 0);
            iterators.put(threadId, iterator);
        }
        return iterator;
    }


    private void showButtonDemo() {
        button1.addActionListener(e -> EditorUtils.navigateToClass(project, "com.company.Main", 13));

    }

    private void showColorButtonDemo() {
        colorButton.addActionListener(e -> {
            Map<String, List<Score>> map = new HashMap<>();
            map.put("com/company/Main", Arrays.asList(new Score(11, 0.5), new Score(12, 0.9),
                    new Score(13, 0.2), new Score(14, 0.5), new Score(15, 0.2)));
            map.put("com/company/TestClass", Arrays.asList(new Score(13, 0.1), new Score(21, 0.7)));

            EditorUtils.colorAllOpenClassSBFL(project, map, false);
            colorOpenedClass(map);
        });
    }

    /**
     * colors every newly opened editor window as long as SBFL scores are available in the map
     * after the color button has been clicked on at least once
     *
     * @param map includes the coverage data for all the executed tests
     */
    private void colorOpenedClass(Map<String, List<Score>> map) {
        if (!coloring) {
            MessageBus messageBus = project.getMessageBus();
            messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                @Override
                public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                    EditorUtils.colorClassSBFL(project, VirtualHelper.getShortPath(project, file), map, false);
                }
            });
        }
        coloring = true;
    }

    /**
     * collects and processes trace data
     *
     * @param dataManager the trace data manager which provides the data
     * @param displayName the respective data id
     */
    private void readData(TraceDataManager dataManager, String displayName) {
        // sample processing of provided execution trace
        Pair<Map<Long, byte[]>, Map<Integer, String>> traceData = dataManager.getTraceData(displayName);
        if (traceData != null) {
            idToClassNameMap = traceData.getSecond();
            Map<Long, byte[]> indexedTraces = traceData.getFirst();
            for (Map.Entry<Long, byte[]> entry : indexedTraces.entrySet()) {
                try {
                    sequences.put(entry.getKey(), // <- this is the thread ID
                            SequiturUtils.getInputSequenceFromByteArray(entry.getValue(), Long.class));
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
            double greenscore = 0.3;
            double yellowscore = 0.7;
            tlength = 0;
            String low = "low score";
            String medium = "medium score";
            String high = "high score";
            category_1 = Color.green;
            category_2 = Color.yellow;
            category_3 = Color.red;
            String tempString = "";
            Random dice = new Random();

            //prepare dataset for bar chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            double defaultscore = 0.5;
            category_count = 0;

            //create bar chart from trace
            for (Map.Entry<Long, InputSequence<Long>> sequence : sequences.entrySet()) {
                StringBuilder sb = new StringBuilder();
                //cycle through iterator once to get the length of the trace, then define an array of said length
                TraceIterator iterator = new TraceIterator(sequence.getValue(), idToClassNameMap, 0);
                while (iterator.hasNext()) {
                    tlength += 1;
                    iterator.next();
                }
                int tline[] = new int[tlength];
                String[] tclass = new String[tlength];
                tlength = 0;
                iterator = new TraceIterator(sequence.getValue(), idToClassNameMap, 0);
                while (iterator.hasNext()) {
                    sb.setLength(0);
                    Pair<String, Integer> next = iterator.next();
                    sb.append(Integer.valueOf(tlength+1)).append(": ").append(next.first).append(": ").append(next.second).append(System.lineSeparator());
                    double value = dice.nextDouble();
                    //double value = defaultscore;
                    //tclass[tlength] = next.first;
                    //tline[tlength] = next.second;
                    tlength += 1;
                    if (value < greenscore) {
                        dataset.addValue(value, low, sb.toString());
                        category_temp = Color.red;
                    }
                    else {
                        if (value < yellowscore) {
                            dataset.addValue(value, medium , sb.toString());
                            category_temp = Color.yellow;
                        }
                        else {
                            dataset.addValue(value, high , sb.toString());
                            category_temp = Color.green;
                        }
                    }
                    //get the order of colors for the bar chart
                    switch(category_count){
                        case 0:
                            category_1 = category_temp;
                            category_count +=1;
                            break;
                        case 1:
                            if (!category_1.equals(category_temp)) {
                                category_2 = category_temp;
                                category_count += 1;
                            } break;
                        case 2:
                            if (!category_1.equals(category_temp) && !category_2.equals(category_temp)) {
                                category_3 = category_temp;
                                category_count += 1;
                            } break;
                        case 3: break;
                    }
                }

                //printing the traces here only for testing / showing purposes
                //JTextPane textPane = new JTextPane();
                //textPane.setText(sb.toString());

                createBarChart(dataset);
                //createLineChart(dataset);
            }
        }
    }

}
