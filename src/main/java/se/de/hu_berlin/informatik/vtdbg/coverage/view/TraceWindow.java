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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceDataManager;
import se.de.hu_berlin.informatik.vtdbg.coverage.tracedata.TraceIterator;
import se.de.hu_berlin.informatik.vtdbg.utils.EditorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
    private JFreeChart barChart;
    private XYPlot plot;
    XYSeriesCollection dataset;
    XYSeries series;
    SBFLScoreBarRenderer barRenderer;
    XYSeries tmpseries;
    private JButton buttonLeft;
    private JButton buttonRight;
    private JButton zOutButton;
    private JButton zInButton;
    private JButton rnavButton;
    private JButton lnavButton;
    private Color category_1;   //for coloring purposes
    private Color category_2;   //for coloring purposes
    private Color category_3;   //for coloring purposes
    private int category_count; //for coloring purposes
    private Color category_temp;//for coloring purposes
    private int navstart;
    private int navend;
    private int navmid;
    private int counter;
    private int navrange;

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
        registerzInButton();
        registerzOutButton();
        registerlnavButton();
        registerrnavButton();
    }

    /**
     * @return everything (that is visible) from the TraceWindow
     */
    public JPanel getContent() {
        return content;
    }

    //update the bar chart after pressing a navigational button
    private void updateXY(){
        try {
            tmpseries = series.createCopy(navstart,navend);
        } catch (CloneNotSupportedException cnsex) {tmpseries = series;}
        dataset.removeAllSeries();
        dataset.addSeries(tmpseries);
        plot.setDataset(dataset);
        barRenderer.scstart = navstart;
        barRenderer.scend = navend;
    }

    //button for zooming in
    private void registerzInButton() {
        zInButton.addActionListener(e -> {
                navrange = navrange / 2;
                navstart = navmid - navrange;
                navend = navmid + navrange;
                updateXY();
            }
            );
    }

    //button for zooming out
    private void registerzOutButton() {
        zOutButton.addActionListener(e -> {
            navrange = navrange * 2;
            if (navrange * 2 > counter) {
                navstart = 0; navend = counter;
                navmid = counter / 2;
                navrange = counter / 2;
            }
            else
            {
                if (navmid + navrange > counter)
                {navmid = counter - navrange;}
                if (navmid - navrange < 0)
                {navmid = navrange;}
                navstart = navmid - navrange;
                navend = navmid + navrange;
            }
            updateXY();
                }
        );
    }

    //button for navigating to the left
    private void registerlnavButton() {
        lnavButton.addActionListener(e -> {
            if (navmid-(navrange*1.25) < 0) {
                    navmid = 0 + navrange;
                }
            else {
                    navmid -= (navrange * 0.25);
                }
            navstart = navmid - navrange;
            navend = navmid + navrange;
            updateXY();
            }
        );
    }

    //button for navigating to the right
    private void registerrnavButton() {
        rnavButton.addActionListener(e -> {
            if (navmid+(navrange*1.25) > counter) {
                    navmid = counter - navrange;
                }
            else {
                    navmid += (navrange * 0.25);
                }
            navstart = navmid - navrange;
            navend = navmid + navrange;
            updateXY();
            }
        );
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
    private void colorOpenedClass(Map<String, Map<Integer, Float>> map) {
        if (!coloring) {
            MessageBus messageBus = project.getMessageBus();
            messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                @Override
                public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                    // VirtualHelper.getShortPath(project, file)
                    EditorUtils.colorAllClassesSBFL(project, map, false);
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

                series = new XYSeries("lines");

                Map<Long, Float> scoreMap = new HashMap<>();

                ListIterator<Long> iterator = getIndexedTraceIterator(sequence, false);
                counter = 0;
                while (iterator.hasNext()) {
                    long next = iterator.next();
                    indices[counter] = next;

                    // get random score (for testing)
                    float value = scoreMap.computeIfAbsent(next, k -> dice.nextFloat());

                    highestScore = Math.max(highestScore, value);
                    lowestScore = Math.min(lowestScore, value);
                    scores[counter] = value;

                    series.add(counter, value, false);

                    ++counter;
                }
                counter -= 1;
                navstart = 0;
                navend = counter;
                navrange = counter / 2;
                navmid = (navstart + navend) / 2;
                dataset = new XYSeriesCollection();
                dataset.addSeries(series);
                /* use with buttons to navigate the chart

                 */

                createBarChart(sequence.getKey(), dataset, indices, lowestScore, highestScore, scores);
            }
        }
    }

    private void createBarChart(long threadId, IntervalXYDataset dataset, long[] indices,
                                float lowestScore, float highestScore, float[] scores) {
        barChart = ChartFactory.createXYBarChart(null, "Line",
                false, "Score", dataset, PlotOrientation.VERTICAL,
                false, true, false);

        /* Get instance of Plot */
        plot = barChart.getXYPlot();

        /* Change Bar colors */
        barRenderer = new SBFLScoreBarRenderer(lowestScore, highestScore, scores);
        barRenderer.scstart = 0;
        barRenderer.scend = counter;
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


//        chartPanel.addChartMouseListener(getJumpToCodeMouseListener(indices));
        chartPanel.addMouseListener(getCodeMarkerMouseListener(chartPanel, indices, scores));

        tabs.addTab(TAB_TITLE_PREFIX + threadId, chartPanel);
    }

    @NotNull
    private MouseAdapter getCodeMarkerMouseListener(ChartPanel panel, long[] indices, float[] scores) {
        return new MouseAdapter() {
            private Marker marker;
            private Double markerStart = Double.NaN;
            private Double markerEnd = Double.NaN;
            private final JFreeChart chart = panel.getChart();
            private final XYPlot plot = (XYPlot) chart.getPlot();

            private void updateMarker() {
                if (DumbService.isDumb(project)) {
                    // ignore when not ready
                    showNoIndexWarning();
                    return;
                }

                if (marker != null) {
                    plot.removeDomainMarker(marker, Layer.BACKGROUND);
                }
                if (!(markerStart.isNaN() || markerEnd.isNaN())) {
                    if (markerEnd < markerStart) {
                        double temp = markerEnd;
                        markerEnd = markerStart;
                        markerStart = temp;
                    }
//                if (markerEnd > markerStart) {
                    marker = new IntervalMarker(markerStart, markerEnd);
                    marker.setPaint(new Color(0xDD, 0xFF, 0xDD, 0x80));
                    marker.setAlpha(0.5f);
                    plot.addDomainMarker(marker, Layer.BACKGROUND);
//                }

                    // maps class names to map of line numbers to respective line scores
                    Map<String, Map<Integer, Float>> scoreMap = new HashMap<>();

                    int startIndex = markerStart.intValue();
                    int endIndex = markerEnd.intValue();
                    for (int i = startIndex; i <= endIndex; ++i) {
                        if (i < 0 || i >= indices.length) {
                            continue;
                        }
                        long encodedStatement = indices[i];
                        String className = ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap);
                        int line = ClassLineEncoding.getLineNUmber(encodedStatement);

                        scoreMap.computeIfAbsent(className, k -> new HashMap<>())
                                .put(line, scores[i]);
                    }

                    // color selected lines based on scores in editor
                    EditorUtils.colorAllClassesSBFL(project, scoreMap, true);

                    // jump to first selected line in editor, if there is a valid one
                    for (int i = startIndex; i <= endIndex; ++i) {
                        if (i < 0 || i >= indices.length) {
                            continue;
                        }
                        long encodedStatement = indices[i];
                        String className = ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap);
                        int line = ClassLineEncoding.getLineNUmber(encodedStatement);

                        EditorUtils.navigateToClass(project, className, line);
                        break;
                    }
                }
            }

            private Double getPosition(MouseEvent e) {
                Point2D p = panel.translateScreenToJava2D(e.getPoint());
                Rectangle2D plotArea = panel.getScreenDataArea();
                XYPlot plot = (XYPlot) chart.getPlot();
                return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                markerEnd = getPosition(e);
                if (e.isControlDown()) {
                    updateMarker();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isControlDown()) {
                    panel.setRangeZoomable(false);
                    panel.setDomainZoomable(false);
                } else {
                    panel.setRangeZoomable(true);
                    panel.setDomainZoomable(true);
                }
                markerStart = getPosition(e);
            }
        };
    }
}
