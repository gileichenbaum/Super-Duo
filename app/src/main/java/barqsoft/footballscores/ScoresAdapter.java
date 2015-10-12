package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    private static final String TAG = "ScoresAdapter";
    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View item = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(item);
        item.setTag(mHolder);
        return item;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        final String homeTeamName = cursor.getString(COL_HOME);
        final String awayTeamName = cursor.getString(COL_AWAY);

        mHolder.home_name.setText(homeTeamName);
        mHolder.away_name.setText(awayTeamName);
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);

        mHolder.home_crest.setImageDrawable(null);
        mHolder.away_crest.setImageDrawable(null);

        final int homeTeamCrestByTeamName = Utilies.getTeamCrestByTeamName(homeTeamName);
        if (homeTeamCrestByTeamName == 0) {
            setupImageFromDb(context, homeTeamName, mHolder.home_crest);
        } else {
            mHolder.home_crest.setImageResource(homeTeamCrestByTeamName);
        }

        final int awayTeamCrestByTeamName = Utilies.getTeamCrestByTeamName(awayTeamName);
        if (awayTeamCrestByTeamName == 0) {
            setupImageFromDb(context, awayTeamName, mHolder.away_crest);
        } else {
            mHolder.away_crest.setImageResource(awayTeamCrestByTeamName);
        }

        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if (mHolder.match_id == detail_match_id) {
//            Log.v(TAG, "will insert extraView, league=" + cursor.getInt(COL_LEAGUE));

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilies.getMatchDay(cursor.getInt(COL_MATCHDAY), cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilies.getLeague(context, cursor.getInt(COL_LEAGUE)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.home_name.getText() + " " + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    private void setupImageFromDb(final Context context, final String teamName, final ImageView imageView) {

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        final Cursor c = context.getContentResolver().query(DatabaseContract.icons_table.buildIconUrlsWithTeamNames(), null, null, new String[]{teamName}, null);

        if (c.moveToFirst()) {

            byte[] imageBytes = c.getBlob(c.getColumnIndex(DatabaseContract.icons_table.IMAGE_BLOB));
            if (imageBytes != null && imageBytes.length > 0) {
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
                c.close();
                return;
            }

            final String iconUrl = c.getString(c.getColumnIndex(DatabaseContract.icons_table.ICON_URL_COL));
            if (TextUtils.isEmpty(iconUrl)) {
                final String teamDataUrl = c.getString(c.getColumnIndex(DatabaseContract.icons_table.TEAM_DATA_LINK_COL));
                if (!TextUtils.isEmpty(teamDataUrl)) {
                    Utilies.getTeamIconUrlAsync(context, teamName, teamDataUrl);
                }
            } else if (iconUrl.endsWith(".svg")) {
                Utilies.getTeamSVGIconAsync(context, teamName, iconUrl);
            } else {
                Picasso.with(context)
                        .load(iconUrl)
                        .placeholder(R.drawable.no_icon)
                        .resize(imageView.getLayoutParams().width, imageView.getLayoutParams().height)
                        .stableKey(teamName)
                        .into(imageView);
            }
        }

        c.close();
    }

    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }
}
