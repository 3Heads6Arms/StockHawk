package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.common.collect.Lists;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;


public class StockDetailActivity extends AppCompatActivity {
    private static final String SYMBOL_EXTRA = "StockSymbolExtra";

    private String mSymbol;
    private CSVReader mHistory;

    @BindView(R.id.tv_symbol)
    TextView mSymbolTv;
    @BindView(R.id.tv_current_stock)
    TextView mCurrentStock;
    @BindView(R.id.tv_highlight_label)
    TextView mHighlightLabel;
    @BindView(R.id.tv_highlight)
    TextView mHighlight;
    @BindView(R.id.lc_stock_history)
    LineChart mStockHistoryLc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (!intent.hasExtra(SYMBOL_EXTRA)) {
            throw new IllegalArgumentException("Symbol was not passed to StockDetailActivity.");
        }

        mSymbol = intent.getStringExtra(SYMBOL_EXTRA);
        setStockData();


        try {
            setChartData();
        } catch (IOException e) {
            e.printStackTrace();
            throw new NullPointerException("Unable to read history data");
        }
    }

    private void setChartData() throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        final SparseArrayCompat<String> dateValues = new SparseArrayCompat<>();
        List<Entry> entries = new ArrayList<>();

        List<String[]> histories = mHistory.readAll();
        histories = Lists.reverse(histories);
        for (int i = 0; i < histories.size(); i++) {
            String[] values = histories.get(i);
            long dateValue = Long.parseLong(values[0].trim());
            float stockValue = Float.parseFloat(values[1].trim());

            dateValues.put(i, formatter.format(new Date(dateValue)));
            entries.add(new Entry(i, stockValue));
        }

        LineDataSet dataSet = new LineDataSet(entries, mSymbol);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.05f);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(ContextCompat.getColor(this, R.color.chart_highlight));
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setColor(ContextCompat.getColor(this, R.color.chart_value));
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.chart_label));
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        Description description = new Description();
        description.setText("");

        // Set up chart's values
        mStockHistoryLc.getLegend().setEnabled(false);
        mStockHistoryLc.setDescription(description);
        mStockHistoryLc.setExtraLeftOffset(25); // Bug in default UI, with this much offset we can see full numbers
        mStockHistoryLc.getAxisRight().setTextColor(ContextCompat.getColor(this, R.color.chart_label));
        mStockHistoryLc.getAxisLeft().setEnabled(false);

        mStockHistoryLc.getXAxis().setDrawGridLines(false);
        mStockHistoryLc.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mStockHistoryLc.getXAxis().setTextColor(ContextCompat.getColor(this, R.color.chart_label));

        // TODO: Implement better marker than Highlight text/label
        mStockHistoryLc.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mHighlightLabel.setVisibility(View.VISIBLE);
                mHighlight.setVisibility(View.VISIBLE);

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                String highlightedValue = String.format("%s\n%s", dollarFormat.format(e.getY()), dateValues.get((int) e.getX()));

                mHighlight.setText(highlightedValue);
            }

            @Override
            public void onNothingSelected() {
                mHighlightLabel.setVisibility(View.INVISIBLE);
                mHighlight.setVisibility(View.INVISIBLE);
            }
        });
        mStockHistoryLc
                .getXAxis()
                .setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return dateValues.get((int) value);
                    }
                });

        mStockHistoryLc.setData(lineData);
    }

    private void setStockData() {
        Cursor cursor = getContentResolver()
                .query(
                        Contract.Quote.makeUriForStock(mSymbol),
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[Contract.Quote.QUOTE_COLUMNS.size()]),
                        null,
                        null,
                        null);

        if (cursor == null) {
            throw new NullPointerException("Unable to get stock data from DB");
        }

        cursor.moveToFirst();

        double price = cursor.getDouble(Contract.Quote.POSITION_PRICE);
        double percentageChange = cursor.getDouble(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        double absoluteChange = cursor.getDouble(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        mHistory = new CSVReader(new StringReader(cursor.getString(Contract.Quote.POSITION_HISTORY)));


        DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        int backgroundStyle = absoluteChange >= 0 ? R.drawable.percent_change_pill_green : R.drawable.percent_change_pill_red;
        String currentStock = String.format("%s\n%s (%s)",
                dollarFormat.format(price),
                dollarFormatWithPlus.format(absoluteChange),
                percentageFormat.format(percentageChange / 100));

        mSymbolTv.setText(mSymbol);
        mCurrentStock.setText(currentStock);
        mCurrentStock.setBackgroundResource(backgroundStyle);
        mHighlightLabel.setVisibility(View.INVISIBLE);
        mHighlight.setVisibility(View.INVISIBLE);

        cursor.close();
    }

    public static Intent getStartIntent(Context context, String symbol) {
        Intent intent = new Intent(context, StockDetailActivity.class);
        intent.putExtra(SYMBOL_EXTRA, symbol);

        return intent;
    }
}
