package barqsoft.footballscores.service;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
    public final Bitmap mHomeTeamIconBmp;
    public final Bitmap mAwayTeamIconBmp;
    private Date mMatchDate;
    private int mCurrentMinute;
    private int mDayDiff;

    public MatchData(final Context context, final Cursor cursor) {
        mId = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.MATCH_ID));
        mHomeTeamName = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
        mAwayTeamName = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
        mAwayTeamScore = String.valueOf(Math.max(cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL)), 0));
        mHomeTeamScore = String.valueOf(Math.max(cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL)), 0));
        mDate = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.DATE_COL));
        mTime = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL));

        mHomeTeamIconBmp = getTeamIcon(context, mHomeTeamName);
        mAwayTeamIconBmp = getTeamIcon(context, mAwayTeamName);

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

    private Bitmap getTeamIcon(final Context context, final String teamName) {

        Bitmap bmp = null;

        final Cursor c = context.getContentResolver().query(DatabaseContract.icons_table.buildIconUrlsWithTeamNames(), null, null, new String[]{teamName}, null);

        if (c.moveToFirst()) {
            byte[] imageBytes = c.getBlob(c.getColumnIndex(DatabaseContract.icons_table.IMAGE_BLOB));
            if (imageBytes != null && imageBytes.length > 0) {
                bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }
            c.close();
        }

        return bmp;
    }

    public int getCurrentMinute() {
        return mCurrentMinute;
    }

    public int getDayDiff() {
        return mDayDiff;
    }

    public Date getMatchDate() {
        return mMatchDate;
    }
}
