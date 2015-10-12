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

import barqsoft.footballscores.service.FetchService;

/**
 * Implementation of App Widget functionality.
 */
public class FootballScoresWidget extends AppWidgetProvider {

    public static final String MATCH_DAY = "match_day";
    private static final String TAG = "FootballScoresWidget";
    private static final float DEFAULT_REFRESH_INTERVAL = Utilies.MILLIS_PER_HOUR * 6f;
    private static final float DEFAULT_REFRESH_INTERVAL_IN_MINUTES = Utilies.MILLIS_PER_HOUR * 6f / Utilies.MILLIS_PER_MINUTE;
    private static final String OPEN_ACTIVITY = "open_activity";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        Intent svcIntent = new Intent(context, StackWidgetService.class);

        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.football_scores_widget);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            widget.setRemoteAdapter(R.id.widget_list_view, svcIntent);
        } else {
            widget.setRemoteAdapter(appWidgetId, R.id.widget_list_view, svcIntent);
        }

        Intent clickIntent = new Intent(context, MainActivity.class);
        PendingIntent clickPI = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        widget.setPendingIntentTemplate(R.id.widget_list_view, clickPI);
        widget.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view);

        Intent toastIntent = new Intent(context, FootballScoresWidget.class);
        // Set the action for the intent.
        // When the user touches a particular view, it will have the effect of
        // broadcasting OPEN_ACTIVITY.
        toastIntent.setAction(FootballScoresWidget.OPEN_ACTIVITY);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setPendingIntentTemplate(R.id.widget_list_view, toastPendingIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);

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

            final Cursor a = context.getContentResolver().query(DatabaseContract.BASE_CONTENT_URI, null, null, null, DatabaseContract.scores_table.DATE_COL + " desc");

            int nextGameStartTimeDiff = Integer.MAX_VALUE;
            boolean hasOngoingMatch = false;

            if (null != a) {

                while (a.moveToNext()) {

                    final String matchDateString = a.getString(a.getColumnIndex(DatabaseContract.scores_table.DATE_COL));
                    final String matchTimeString = a.getString(a.getColumnIndex(DatabaseContract.scores_table.TIME_COL));

                    final Date matchDate = new_date.parse(matchDateString + ":" + matchTimeString);

                    final int matchStartDiffInMinutes = (int) ((matchDate.getTime() - now.getTime() * 1f) / Utilies.MILLIS_PER_MINUTE);

                    if (matchStartDiffInMinutes > 0) {
                        nextGameStartTimeDiff = Math.min(nextGameStartTimeDiff, matchStartDiffInMinutes);
                    } else if (matchStartDiffInMinutes > -130) {
                        hasOngoingMatch = true;
                    }
//                    Log.i(TAG, "date=" + now + ", matchDate=" + matchDate + ", diffMinutes=" + matchStartDiffInMinutes);
                }
                a.close();
            }

            int nextUpdateIntervalInMinutes = hasOngoingMatch ? 2 : (int) (nextGameStartTimeDiff < DEFAULT_REFRESH_INTERVAL_IN_MINUTES ? nextGameStartTimeDiff - 5 : DEFAULT_REFRESH_INTERVAL_IN_MINUTES);

//            Log.i(TAG,"next game starts in " + nextGameStartTimeDiff + " minutes, hasOngoingMatch=" + hasOngoingMatch + ", nextUpdateInterval minutes=" + nextUpdateIntervalInMinutes);

            return (int) (nextUpdateIntervalInMinutes * Utilies.MILLIS_PER_MINUTE);
        } catch (Exception e) {
            Log.e(TAG, "error getting todays games", e);
        }

        return (int) DEFAULT_REFRESH_INTERVAL;
    }

    @Override
    public void onAppWidgetOptionsChanged(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
//        Log.i(TAG, "onReceive " + intent.getAction());
        if (intent != null) {
            if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                final ComponentName thisAppWidget = new ComponentName(context.getPackageName(), FootballScoresWidget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                onUpdate(context, appWidgetManager, appWidgetIds);
            } else if (intent.getAction().equals(OPEN_ACTIVITY)) {
                final int matchDay = intent.getIntExtra(MATCH_DAY, Integer.MIN_VALUE);
                if (matchDay > Integer.MIN_VALUE) {
                    final Intent activityIntent = new Intent(context,MainActivity.class);
                    activityIntent.putExtra(MATCH_DAY, matchDay + 2);
                    activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(activityIntent);
                }
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final Context applicationContext = context.getApplicationContext();

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(applicationContext, appWidgetManager, appWidgetIds[i]);
        }

        final Intent service_start = new Intent(applicationContext, FetchService.class);
        applicationContext.startService(service_start);

        WidgetAlarmUtils.startAlarm(applicationContext, getRefreshRate(context));

        super.onUpdate(context, appWidgetManager, appWidgetIds);
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
}