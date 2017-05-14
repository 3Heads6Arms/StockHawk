package com.udacity.stockhawk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by anh.hoang on 5/14/17.
 */

public class HighlightMarkerView extends MarkerView {
    private MPPointF mOffset;
    @BindView(R.id.textView)
    TextView textView;

    public HighlightMarkerView(Context context){
        super(context, R.layout.highlight_view);
        ButterKnife.bind(this);

        textView.setTextColor(ContextCompat.getColor(context, R.color.chart_label));
    }

    @Override
    public MPPointF getOffset() {

        if(mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        textView.setText("" + e.getY());

        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }
}
