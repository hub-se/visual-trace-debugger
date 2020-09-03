package se.de.hu_berlin.informatik.vtdbg.coverage.view;

import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYDataset;

import java.awt.*;

public class SBFLScoreBarRenderer extends XYBarRenderer {

    private final float range;
    private float lowestScore;
    private float highestScore;
    public float[] scores;

    public SBFLScoreBarRenderer(float lowestScore, float highestScore, float[] scores) {
        this.lowestScore = lowestScore;
        this.highestScore = highestScore;
        this.scores = scores;
        this.range = highestScore - lowestScore;
    }

    /*public SBFLScoreBarRenderer(float lowestScore, float highestScore, XYDataset dataset){
        this.range = highestScore - lowestScore;
        this.lowestScore = lowestScore;
        this.highestScore = highestScore;
        scores = new float[dataset.getItemCount(0)];
        for (int i = 0; i < dataset.getItemCount(0); i++){
            scores[i] = (float) dataset.getYValue(0,i);
        }
    }*/

    public Paint getItemPaint(final int row, final int column) {
        // returns color depending on score, if existing
        if (column >= 0 && column < scores.length) {
            return getColor(scores[column]);
        } else {
            return Color.BLUE;
        }
    }

    // linear interpolation from red to yellow to green
    private Color getColor(float score) {
        float normalizedScore = (score - lowestScore) / range;

        return getColorForNormalizedScore(normalizedScore);
    }

    // linear interpolation from red to yellow to green
    public static Color getColorForNormalizedScore(float normalizedScore) {
        return new Color(
                // red: change from 0 (green) to 1 (yellow) to 1 (red)
                getInterpolatedRGBValue(normalizedScore, 0, 1, 1),
                // green: change from 1 (green) to 1 (yellow) to 0 (red)
                getInterpolatedRGBValue(normalizedScore, 1, 1, 0),
                // blue: always 0
                0.0f);
    }

    private static float getInterpolatedRGBValue(float normalizedScore, float colorValue1, float colorValue2, float colorValue3) {
        float colorValueResult = 0;
        if (normalizedScore < 0.5) {
            colorValueResult = colorValue1 * (0.5f - normalizedScore) * 2.0f + (colorValue2 * normalizedScore * 2.0f);
        } else {
            colorValueResult = colorValue2 * (1.0f - normalizedScore) * 2.0f + colorValue3 * (normalizedScore - 0.5f) * 2.0f;
        }
        return colorValueResult;
    }
}