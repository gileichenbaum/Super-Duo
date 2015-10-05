package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.service.StackWidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class FootballScoresWidget extends AppWidgetProvider {

    private static final String TAG = "FootballScoresWidget";

    @Override
    public void onAppWidgetOptionsChanged(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        Log.i(TAG, "onReceive" + intent.getAction());
        if (intent != null && AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final ComponentName thisAppWidget = new ComponentName(context.getPackageName(), FootballScoresWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }

        getRefreshRate(context);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        WidgetAlarmUtils.startAlarm(context, getRefreshRate(context));
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        WidgetAlarmUtils.stopAlarm(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        final Intent intent = new Intent(context, StackWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // When intents are compared, the extras are ignored, so we need to embed the extras
        // into the data so that the extras will not be ignored.
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.football_scores_widget);
        rv.setRemoteAdapter(appWidgetId, R.id.widget_list_view, intent);

        // The empty view is displayed when the collection has no items. It should be a sibling
        // of the collection view.
        rv.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view);

        // This section makes it possible for items to have individualized behavior.
        // It does this by setting up a pending intent template. Individuals items of a collection
        // cannot set up their own pending intents. Instead, the collection as a whole sets
        // up a pending intent template, and the individual items set a fillInIntent
        // to create unique behavior on an item-by-item basis.
        Intent toastIntent = new Intent(context, StackWidgetProvider.class);
        // Set the action for the intent.
        // When the user touches a particular view, it will have the effect of
        // broadcasting TOAST_ACTION.
        toastIntent.setAction(StackWidgetProvider.TOAST_ACTION);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.widget_list_view, toastPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    private static int getRefreshRate(final Context context) {

        try {

            SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
            new_date.setTimeZone(TimeZone.getDefault());
            final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            final Date now = calendar.getTime();
            String date = new_date.format(now);
            date = date.substring(0, date.indexOf(":"));
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            final int minute = calendar.get(Calendar.MINUTE);


            final Cursor a = context.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, new String[]{date}, DatabaseContract.scores_table.DATE_COL + " desc");

            if (null != a) {

                while (a.moveToNext()) {

                    String matchDateString = a.getString(a.getColumnIndex(DatabaseContract.scores_table.DATE_COL));
                    String matchTimeString = a.getString(a.getColumnIndex(DatabaseContract.scores_table.TIME_COL));

                    final Date matchDate = new_date.parse(matchDateString + ":" + matchTimeString);

                    final int matchStartDiffInMinutes = (int) ((matchDate.getTime() - now.getTime() * 1f) / 1000f / 60f);
                    Log.i(TAG, "date=" + now + ", matchDate=" + matchDate + ", diffMinutes=" + matchStartDiffInMinutes);
                }

//                DatabaseUtils.dumpCursor(a);
                a.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "error getting todays games", e);
        }

        return 1000;
    }
}