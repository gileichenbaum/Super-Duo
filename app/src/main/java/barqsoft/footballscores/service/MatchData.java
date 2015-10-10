package barqsoft.footballscores.service;

import android.database.Cursor;

import barqsoft.footballscores.DatabaseContract;

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
    }
}
