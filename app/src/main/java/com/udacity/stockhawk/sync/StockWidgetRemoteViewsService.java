package com.udacity.stockhawk.sync;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.StockDetailActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by anh.hoang on 5/18/17.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {

    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;

    public StockWidgetRemoteViewsService() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            Cursor data;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                data = getContentResolver().query(
                        Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[Contract.Quote.QUOTE_COLUMNS.size()]),
                        null,
                        null,
                        null);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                float percentage = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE) / 100;
                int backgroundResource = percentage > 0 ? R.drawable.percent_change_pill_green : R.drawable.percent_change_pill_red;
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));
                views.setTextViewText(R.id.change, percentageFormat.format(percentage));
                views.setInt(R.id.change, "setBackgroundResource", backgroundResource);
                views.setInt(R.id.ll_quote, "setBackgroundColor", ContextCompat.getColor(StockWidgetRemoteViewsService.this, R.color.background_for_white_text));
                views.setTextColor(R.id.symbol, ContextCompat.getColor(StockWidgetRemoteViewsService.this, R.color.chart_label));
                views.setTextColor(R.id.price, ContextCompat.getColor(StockWidgetRemoteViewsService.this, R.color.chart_label));

                Intent intent = StockDetailActivity.getStartIntent(StockWidgetRemoteViewsService.this, symbol);
                views.setOnClickFillInIntent(R.id.ll_quote, intent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;//new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getInt(Contract.Quote.POSITION_ID);
                }

                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
