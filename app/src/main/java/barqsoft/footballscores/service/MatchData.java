package barqsoft.footballscores.service;

import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.Utilies;

/**
 * Created by GIL on 06/10/2015 for Football_Scores-master.
 */
public class MatchData {

    public final String mHomeTeamName;
    public final String mAwayTeamName;
    public final String mAwayTeamScore;
    public final String mHomeTeamScore;
    public final String mDate;
    public final String mTime;
    public final String mId;
    private Date mMatchDate;
    private int mCurrentMinute;
    private int mDayDiff;

    /**
     *   home_name = (TextView) view.findViewById(R.id.home_name);
     away_name = (TextView) view.findViewById(R.id.away_name);
     score     = (TextView) view.findViewById(R.id.score_textview);
     date      = (TextView) view.findViewById(R.id.data_textview);
     home_crest = (ImageView) view.findViewById(R.id.home_crest);
     away_crest = (ImageView) view.findViewById(R.id.away_crest);
     */

    public MatchData(final Cursor cursor) {
        mId = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.MATCH_ID));
        mHomeTeamName = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
        mAwayTeamName = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
        mAwayTeamScore = String.valueOf(Math.max(cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL)), 0));
        mHomeTeamScore = String.valueOf(Math.max(cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL)), 0));
        mDate = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.DATE_COL));
        mTime = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL));

        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        final Date now = calendar.getTime();
        final SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
        new_date.setTimeZone(TimeZone.getDefault());
        try {
            mMatchDate = new_date.parse(mDate + ":" + mTime);
            mCurrentMinute = (int) ((now.getTime() - mMatchDate.getTime()) * 1f / Utilies.MILLIS_PER_MINUTE);
            mDayDiff = (int) ((mMatchDate.getTime() - now.getTime() * 1f) / Utilies.MILLIS_PER_DAY);
        } catch (ParseException e) {
            e.printStackTrace();
            mMatchDate = null;
        }
    }

    public int getCurrentMinute() {
        return mCurrentMinute;
    }

    public int getDayDiff() {
        return mDayDiff;
    }
}
