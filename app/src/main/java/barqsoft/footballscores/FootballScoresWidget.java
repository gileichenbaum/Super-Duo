package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

        super.onUpdate(context,appWidgetManager,appWidgetIds);
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

        Intent svcIntent=new Intent(context, StackWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget=new RemoteViews(context.getPackageName(),R.layout.football_scores_widget);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            widget.setRemoteAdapter(R.id.widget_list_view, svcIntent);
        } else {
            widget.setRemoteAdapter(appWidgetId,R.id.widget_list_view, svcIntent);
        }

        Intent clickIntent=new Intent(context, MainActivity.class);
        PendingIntent clickPI=PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        widget.setPendingIntentTemplate(R.id.widget_list_view, clickPI);
        widget.setEmptyView(R.id.widget_list_view,R.id.widget_empty_view);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.widget_list_view);

        appWidgetManager.updateAppWidget(appWidgetId, widget);
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