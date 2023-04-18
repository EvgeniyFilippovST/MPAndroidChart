package com.github.mikephil.charting.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

/**
 * Created by philipp on 12/06/15.
 */
public abstract class ChartTouchListener<T extends Chart<?>> extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    public enum ChartGesture {
        NONE, DRAG, X_ZOOM, Y_ZOOM, PINCH_ZOOM, ROTATE, SINGLE_TAP, DOUBLE_TAP, LONG_PRESS, FLING
    }

    /**
     * the last touch gesture that has been performed
     **/
    protected ChartGesture mLastGesture = ChartGesture.NONE;

    // states
    protected static final int NONE = 0;
    protected static final int DRAG = 1;
    protected static final int X_ZOOM = 2;
    protected static final int Y_ZOOM = 3;
    protected static final int PINCH_ZOOM = 4;
    protected static final int POST_ZOOM = 5;
    protected static final int ROTATE = 6;

    /**
     * integer field that holds the current touch-state
     */
    protected int mTouchMode = NONE;

    /**
     * the last highlighted object (via touch)
     */
    protected Highlight mLastHighlighted;

    protected Highlight mLastHighlightedSecond;

    protected Highlight mLastLineTapped;

    /**
     * the gesturedetector used for detecting taps and longpresses, ...
     */
    protected GestureDetector mGestureDetector;

    /**
     * the chart the listener represents
     */
    protected T mChart;

    public ChartTouchListener(T chart) {
        this.mChart = chart;

        mGestureDetector = new GestureDetector(chart.getContext(), this);
    }

    /**
     * Calls the OnChartGestureListener to do the start callback
     *
     * @param me
     */
    public void startAction(MotionEvent me) {

        OnChartGestureListener l = mChart.getOnChartGestureListener();

        if (l != null)
            l.onChartGestureStart(me, mLastGesture);
    }

    /**
     * Calls the OnChartGestureListener to do the end callback
     *
     * @param me
     */
    public void endAction(MotionEvent me) {

        OnChartGestureListener l = mChart.getOnChartGestureListener();

        if (l != null)
            l.onChartGestureEnd(me, mLastGesture);
    }

    /**
     * Sets the last value that was highlighted via touch.
     *
     * @param high
     */
    public void setLastHighlighted(Highlight high) {
        mLastHighlighted = high;
    }

    /**
     * returns the touch mode the listener is currently in
     *
     * @return
     */
    public int getTouchMode() {
        return mTouchMode;
    }

    /**
     * Returns the last gesture that has been performed on the chart.
     *
     * @return
     */
    public ChartGesture getLastGesture() {
        return mLastGesture;
    }


    /**
     * Perform a highlight operation.
     *
     * @param e
     */
    protected void performHighlight(Highlight h, MotionEvent e) {

        if (h == null || h.equalTo(mLastHighlighted)) {
            mChart.highlightValue(null, true);
            mLastHighlighted = null;
        } else {
            mChart.highlightValue(h, true);
            mLastHighlighted = h;
        }
    }

    protected void performHighlightSection(Highlight h, int highLightColor, int activeHighLightColor) {
        if (h != null) {
            if (mLastHighlighted == null) {
                mChart.highlightValue(h, true);
                mLastHighlighted = h;
            } else if (mLastHighlightedSecond == null) {
                mLastHighlightedSecond = h;
                mLastLineTapped = mLastHighlightedSecond;
                mLastHighlightedSecond.setColor(activeHighLightColor);
                mChart.highlightValues(new Highlight[] { mLastHighlighted, mLastHighlightedSecond });
                fillSection();
            } else if (h.equalTo(mLastHighlighted)) {
                mLastLineTapped = h;
                mLastHighlighted.setColor(activeHighLightColor);
                mLastHighlightedSecond.setColor(highLightColor);
                mChart.highlightValues(new Highlight[] { mLastHighlighted, mLastHighlightedSecond });
            } else if (h.equalTo(mLastHighlightedSecond)) {
                mLastLineTapped = h;
                mLastHighlightedSecond.setColor(activeHighLightColor);
                mLastHighlighted.setColor(highLightColor);
                mChart.highlightValues(new Highlight[] { mLastHighlighted, mLastHighlightedSecond });
            } else if (mLastLineTapped.equalTo(mLastHighlighted) && h.getX() < mLastHighlightedSecond.getX()) {
                mLastHighlighted = h;
                mLastLineTapped = h;
                mLastHighlighted.setColor(activeHighLightColor);
                mChart.highlightValues(new Highlight[] { mLastHighlighted, mLastHighlightedSecond });
                fillSection();
            }else if (mLastLineTapped.equalTo(mLastHighlightedSecond) && h.getX() > mLastHighlighted.getX()) {
                mLastHighlightedSecond = h;
                mLastLineTapped = h;
                mLastHighlightedSecond.setColor(activeHighLightColor);
                mChart.highlightValues(new Highlight[]{mLastHighlighted, mLastHighlightedSecond});
                fillSection();
            }
        }
    }

    protected void fillSection() {
        if (mLastHighlighted != null && mLastHighlightedSecond != null) {
            List<ILineDataSet> dataSets = (List<ILineDataSet>) mChart.getData().getDataSets();
            int filledStartIndex = (int) (mLastHighlighted.getX() - mChart.getXAxis().mAxisMinimum);
            int filledEndIndex = (int) (mLastHighlightedSecond.getX() - mChart.getXAxis().mAxisMinimum);
            for (ILineDataSet set: dataSets) {
                set.setDrawFilledSection(filledStartIndex, (int) (filledEndIndex));
            }
        }
    }

    /**
     * returns the distance between two points
     *
     * @param eventX
     * @param startX
     * @param eventY
     * @param startY
     * @return
     */
    protected static float distance(float eventX, float startX, float eventY, float startY) {
        float dx = eventX - startX;
        float dy = eventY - startY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
