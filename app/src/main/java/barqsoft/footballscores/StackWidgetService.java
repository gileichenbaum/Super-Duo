package barqsoft.footballscores;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import barqsoft.footballscores.service.FetchService;
import barqsoft.footballscores.service.MatchData;

/**
 * Created by GIL on 05/10/2015 for Football_Scores-master.
 */
public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("StackWidgetService","StackWidgetService onGetViewFactory");
        return new StackRemoteViewsFactory(getApplicationContext(),intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("StackWidgetService", "StackWidgetService onCreate");
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
        Log.i("StackWidgetService", "StackWidgetService onStart");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.i("StackWidgetService","StackWidgetService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "StackRemoteViewsFactory";
    private Context mContext;
    private int mAppWidgetId;
    private final List<MatchData> mMatchList = new ArrayList<MatchData>();

    public StackRemoteViewsFactory(Context context, Intent intent) {
        Log.i(TAG,"StackRemoteViewsFactory constructor");
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        Log.i(TAG,"StackRemoteViewsFactory onCreate");
        onDataSetChanged();
        update_scores();
    }

    private void update_scores()
    {
        Intent service_start = new Intent(mContext, FetchService.class);
        mContext.startService(service_start);
    }

    @Override
    public void onDataSetChanged() {

        mMatchList.clear();

        final Cursor cursor = mContext.getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,null,null,null,null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    mMatchList.add(new MatchData(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        Log.i(TAG,"StackRemoteViewsFactory onDataSetChanged, size=" + mMatchList.size());
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {

        Log.i(TAG,"StackRemoteViewsFactory getCount()=" + mMatchList.size());

        return mMatchList.size();
    }

    /**
     *   home_name = (TextView) view.findViewById(R.id.home_name);
     away_name = (TextView) view.findViewById(R.id.away_name);
     score     = (TextView) view.findViewById(R.id.score_textview);
     date      = (TextView) view.findViewById(R.id.data_textview);
     home_crest = (ImageView) view.findViewById(R.id.home_crest);
     away_crest = (ImageView) view.findViewById(R.id.away_crest);
     */
    public RemoteViews getViewAt(int position) {

        final RemoteViews row = new RemoteViews(mContext.getPackageName(),R.layout.scores_widget_list_item);

        final MatchData matchData = mMatchList.get(position);
        row.setTextViewText(R.id.home_name, matchData.mHomeTeamName);
        row.setTextViewText(R.id.away_name, matchData.mAwayTeamName);
        row.setTextViewText(R.id.score_textview, matchData.mHomeTeamScore + " - " + matchData.mAwayTeamScore);
        row.setTextViewText(R.id.data_textview, matchData.mDate + " " + matchData.mTime);

        Intent i=new Intent();
        Bundle extras=new Bundle();

//        extras.putString(FootballScoresWidget.EXTRA_ITEM, matchData.mId);
        i.putExtras(extras);
        row.setOnClickFillInIntent(android.R.id.text1, i);

        return(row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}