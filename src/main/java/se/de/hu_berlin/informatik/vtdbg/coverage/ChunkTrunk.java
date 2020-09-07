package se.de.hu_berlin.informatik.vtdbg.coverage;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.de.hu_berlin.informatik.vtdbg.coverage.view.SBFLScoreBarRenderer;

import javax.swing.*;

public class ChunkTrunk {
    private XYSeriesCollection traceCollection;
    private int[] start;
    private int[] current;
    private int[] end;
    private int seriesid;

    //creates chunks of elements from the ChartPanels stored inside a JTabbedPane
    public ChunkTrunk(JTabbedPane tabs, int number, JLabel label) {
        int maxChunkSize = 10000;
        start = new int[number];
        current = new int[number];
        end = new int[number];
        seriesid = 0;
        traceCollection = new XYSeriesCollection();
        for (int i = 0; i < number; i++){
            start[i]=seriesid;
            XYSeries series = ((XYSeriesCollection)((ChartPanel)tabs.getComponentAt(i)).getChart().getXYPlot().getDataset()).getSeries(0);
            while (series.getItemCount() > maxChunkSize){
                try {
                    series.setKey("lines"+seriesid);
                    traceCollection.addSeries(series.createCopy(0,maxChunkSize-1));
                    series = series.createCopy(maxChunkSize, series.getItemCount()-1);
                    seriesid++;
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            if(series.getItemCount()>0){
                series.setKey("lines"+seriesid);
                traceCollection.addSeries(series);
            }
            end[i]=seriesid;
            seriesid++;
            current[i]=start[i];
            updateChunk(tabs, i, label);
        }
    }

    //updates the bar chart with a new chunk
    private void updateChunk(JTabbedPane tabs, int i, JLabel label){
        XYSeriesCollection temp = new XYSeriesCollection();
        temp.addSeries(traceCollection.getSeries(current[i]));
        float[] scores = new float[temp.getSeries(0).getItemCount()];
        for(int j=0; j < temp.getSeries(0).getItemCount();j++){
            scores[j]=temp.getSeries(0).getY(j).floatValue();
        }
        ((SBFLScoreBarRenderer)((ChartPanel)tabs.getComponentAt(i)).getChart().getXYPlot().getRenderer()).scores = scores;
        ((ChartPanel)tabs.getComponentAt(i)).getChart().getXYPlot().setDataset(temp);
        updateLabel(tabs, i, label);
        ((ChartPanel)tabs.getComponentAt(i)).restoreAutoDomainBounds();
    }

    //updates the label that shows which chunk of elements the user currently sees
    public void updateLabel(JTabbedPane tabs, int i, JLabel label){
        int number1 = current[i]-start[i]+1;
        int number2 = end[i]-start[i]+1;
        label.setText("Chunk "+number1+"/"+number2);
    }


    public void nextChunk(JTabbedPane tabs, JLabel label) {
        int i = tabs.getSelectedIndex();
        if (i > -1) {
            if (current[i] < end[i]){
                current[i]++;
                updateChunk(tabs, i, label);
            }
        }
    }

    public void previousChunk(JTabbedPane tabs, JLabel label) {
        int i = tabs.getSelectedIndex();
        if (i > -1) {
            if (current[i] > start[i]){
                current[i]--;
                updateChunk(tabs, i, label);
            }
        }
    }
}
