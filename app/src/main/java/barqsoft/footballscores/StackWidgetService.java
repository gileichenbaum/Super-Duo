package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import barqsoft.footballscores.service.MatchData;

/**
 * Created by GIL on 05/10/2015 for Football_Scores-master.
 */
public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
//        Log.i("StackWidgetService","StackWidgetService onGetViewFactory");
        return new StackRemoteViewsFactory(getApplicationContext(), intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.i("StackWidgetService", "StackWidgetService onCreate");
    }

    @Override
    public void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
//        Log.i("StackWidgetService", "StackWidgetService onStart");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
//        Log.i("StackWidgetService","StackWidgetService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "StackRemoteViewsFactory";
    private final static int WIDGET_TEXT_COLOR = Color.argb(200, 0, 0, 0);
    private final int mWinnerColor;
    private final int mLoserColor;
    private final int mDrawColor;
    private final List<MatchData> mMatchList = new ArrayList<MatchData>();
    private Context mContext;

    public StackRemoteViewsFactory(Context context, Intent intent) {
//        Log.i(TAG,"StackRemoteViewsFactory constructor");
        mContext = context;
        mWinnerColor = mContext.getResources().getColor(R.color.green01);
        mLoserColor = mContext.getResources().getColor(R.color.red01);
        mDrawColor = mContext.getResources().getColor(R.color.blue01);
    }

    @Override
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
//        Log.i(TAG,"StackRemoteViewsFactory onCreate");
        onDataSetChanged();
    }

    @Override
    public void onDataSetChanged() {

        mMatchList.clear();

        for (int i = 0; i < 2; i++) {

            final Date date = new Date(System.currentTimeMillis() + ((i - 1) * 86400000));

            final Cursor cursor = mContext.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, ScoresProvider.SCORES_BY_DATE, new String[]{Utilies.DATE_FORMAT.format(date)}, null);
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        mMatchList.add(new MatchData(mContext, cursor));
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        Collections.sort(mMatchList, new Comparator<MatchData>() {
            @Override
            public int compare(final MatchData lhs, final MatchData rhs) {
                if (lhs.getMatchDate() == null) {
                    return 1;
                }
                if (rhs.getMatchDate() == null) {
                    return -1;
                }
                return -lhs.getMatchDate().compareTo(rhs.getMatchDate());
            }
        });

//        Log.i(TAG,"StackRemoteViewsFactory onDataSetChanged, size=" + mMatchList.size());
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {

//        Log.i(TAG,"StackRemoteViewsFactory getCount()=" + mMatchList.size());

        return mMatchList.size();
    }

    /**
     * home_name = (TextView) view.findViewById(R.id.home_name);
     * away_name = (TextView) view.findViewById(R.id.away_name);
     * score     = (TextView) view.findViewById(R.id.score_textview);
     * date      = (TextView) view.findViewById(R.id.data_textview);
     * home_crest = (ImageView) view.findViewById(R.id.home_crest);
     * away_crest = (ImageView) view.findViewById(R.id.away_crest);
     */
    public RemoteViews getViewAt(int position) {

        final RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.scores_list_item);

        final MatchData matchData = mMatchList.get(position);

        row.setTextColor(R.id.home_name, WIDGET_TEXT_COLOR);
        row.setTextColor(R.id.away_name, WIDGET_TEXT_COLOR);
        row.setTextColor(R.id.score_textview, WIDGET_TEXT_COLOR);
        row.setTextColor(R.id.data_textview, WIDGET_TEXT_COLOR);

        row.setTextViewText(R.id.home_name, matchData.mHomeTeamName);
        row.setTextViewText(R.id.away_name, matchData.mAwayTeamName);
        row.setTextViewText(R.id.score_textview, matchData.mHomeTeamScore + " - " + matchData.mAwayTeamScore);

        row.setImageViewBitmap(R.id.home_crest, matchData.mHomeTeamIconBmp);
        row.setImageViewBitmap(R.id.away_crest, matchData.mAwayTeamIconBmp);

        final int homeScore = Integer.parseInt(matchData.mHomeTeamScore);
        final int awayScore = Integer.parseInt(matchData.mAwayTeamScore);

        final int gameMinute = matchData.getCurrentMinute();

        if (gameMinute > 0 && gameMinute < 105) {
            if (gameMinute > 45 && gameMinute < 60) {
                row.setTextViewText(R.id.data_textview, mContext.getString(R.string.half_time));
            } else if (gameMinute >= 60) {
                row.setTextViewText(R.id.data_textview, "'" + (gameMinute - 15));
            } else {
                row.setTextViewText(R.id.data_textview, "'" + gameMinute);
            }
        } else {
            final String matchDay = Utilies.getDayName(mContext, System.currentTimeMillis() + ((matchData.getDayDiff()) * 86400000));
            final String dateText = gameMinute < 0 ? (matchDay + " " + matchData.mTime) : matchDay;
            row.setTextViewText(R.id.data_textview, dateText);
        }

        if (gameMinute > 0) {
            if (homeScore > awayScore) {
                row.setTextColor(R.id.home_name, mWinnerColor);
                row.setTextColor(R.id.away_name, mLoserColor);
            } else if (homeScore < awayScore) {
                row.setTextColor(R.id.home_name, mLoserColor);
                row.setTextColor(R.id.away_name, mWinnerColor);
            } else {
                row.setTextColor(R.id.home_name, mDrawColor);
                row.setTextColor(R.id.away_name, mDrawColor);
            }
        }

        //  Log.i(TAG,"home=" + matchData.mHomeTeamName + ", minute=" + gameMinute);

        final Bundle extras = new Bundle();
        extras.putInt(FootballScoresWidget.MATCH_DAY, matchData.getDayDiff());
        final Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.list_item_container, fillInIntent);

        return (row);
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