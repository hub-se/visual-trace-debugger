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
import com.intellij.rt.coverage.traces.ClassLineEncoding;
import com.intellij.rt.coverage.traces.SequiturUtils;
import com.intellij.util.messages.MessageBus;
import de.unisb.cs.st.sequitur.input.InputSequence;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.de.hu_berlin.informatik.vtdbg.coverage.Score;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceDataManager;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceIterator;
import se.de.hu_berlin.informatik.vtdbg.utils.EditorUtils;
import se.de.hu_berlin.informatik.vtdbg.utils.VirtualHelper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
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
    private JButton buttonLeft;
    private JButton buttonRight;
    private Color category_1;   //for coloring purposes
    private Color category_2;   //for coloring purposes
    private Color category_3;   //for coloring purposes
    private int category_count; //for coloring purposes
    private Color category_temp;//for coloring purposes

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

        registerNextButton();
        registerPreviousButton();
    }

    /**
     * @return everything (that is visible) from the TraceWindow
     */
    public JPanel getContent() {
        return content;
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
                    EditorUtils.colorLineInEditor(project, next.second, true);
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
                    EditorUtils.colorLineInEditor(project, previous.second, true);
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

    @NotNull
    private ListIterator<Long> getIndexedTraceIterator(Map.Entry<Long, InputSequence<Long>> sequence, boolean reverse) {
        InputSequence<Long> inputSequence = sequences.get(sequence.getKey());
        return inputSequence.iterator(reverse ? inputSequence.getLength() : 0);
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
        if (sequences != null) {
            for (Map.Entry<Long, InputSequence<Long>> sequence : sequences.entrySet()) {
                long tlength = sequence.getValue().getLength();
                if (tlength > Integer.MAX_VALUE) {
                    throw new IllegalStateException("trace too long!");
                }

                // store the "SBFL" scores
                float[] scores = new float[(int) tlength];
                float highestScore = Float.MIN_VALUE;
                float lowestScore = Float.MAX_VALUE;

                // store the element indices
                long[] indices = new long[(int) tlength];

                Random dice = new Random();

                XYSeries series = new XYSeries("lines");

                ListIterator<Long> iterator = getIndexedTraceIterator(sequence, false);
                int counter = 0;
                while (iterator.hasNext()) {
                    long next = iterator.next();
                    indices[counter] = next;

                    // get random score (for testing)
                    float value = dice.nextFloat();
                    highestScore = Math.max(highestScore, value);
                    lowestScore = Math.min(lowestScore, value);
                    scores[counter] = value;

                    series.add(counter, value, false);

                    ++counter;
                }
                XYSeriesCollection dataset = new XYSeriesCollection();
                dataset.addSeries(series);

                createBarChart(sequence.getKey(), dataset, indices, lowestScore, highestScore, scores);
            }
        }
    }

    private void createBarChart(long threadId, IntervalXYDataset dataset, long[] indices,
                                float lowestScore, float highestScore, float[] scores) {
        JFreeChart barChart = ChartFactory.createXYBarChart(null, "Line",
                false, "Score", dataset, PlotOrientation.VERTICAL,
                false, true, false);

        /* Get instance of Plot */
        XYPlot plot = barChart.getXYPlot();

        /* Change Bar colors */
        SBFLScoreBarRenderer barRenderer = new SBFLScoreBarRenderer(lowestScore, highestScore, scores);
        barRenderer.setBarPainter(new StandardXYBarPainter());

        /* generate tooltips */
        final StandardXYToolTipGenerator generator = new StandardXYToolTipGenerator(
                "{1}, score: {2}",
                // class and line
                new TooltipFormat(idToClassNameMap, indices),
                // score
                new DecimalFormat("0.00")
        );
        barRenderer.setSeriesToolTipGenerator(0, generator);

        plot.setRenderer(barRenderer);

        ChartPanel chartPanel = new ChartPanel(barChart);
//        chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ) );
        chartPanel.setPreferredSize(null);

        chartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                if (DumbService.isDumb(project)) {
                    // ignore when not ready
                    showNoIndexWarning();
                    return;
                }

                ChartEntity entity = event.getEntity();
//                System.out.println(entity);
                XYItemEntity itemEntity = (XYItemEntity) entity;
                XYDataset data = itemEntity.getDataset();
                double x = data.getXValue(0, itemEntity.getItem());

                int index = (int) x;
                long encodedStatement = indices[index];
                String className = ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap);
                int line = ClassLineEncoding.getLineNUmber(encodedStatement);

                // jump to respective line in editor
                EditorUtils.navigateToClass(project, className, line);
                EditorUtils.colorLineInEditor(project, line, true);
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
            }
        });

        tabs.addTab(TAB_TITLE_PREFIX + threadId, chartPanel);
    }

}
