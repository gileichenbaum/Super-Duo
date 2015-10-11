package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class FetchService extends IntentService {
    public static final String LOG_TAG = "FetchService";
    public static final String TEAM_ICON_URL_JSON_KEY = "crestUrl";
    private static final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
    private static final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
    private static final String FIXTURES = "fixtures";
    private static final String LINKS = "_links";
    private static final String SOCCER_SEASON = "soccerseason";
    private static final String SELF = "self";
    private static final String MATCH_DATE = "date";
    private static final String HOME_TEAM = "homeTeamName";
    private static final String AWAY_TEAM = "awayTeamName";
    private static final String RESULT = "result";
    private static final String HOME_GOALS = "goalsHomeTeam";
    private static final String AWAY_GOALS = "goalsAwayTeam";
    private static final String MATCH_DAY = "matchday";
    private static final String HOME_TEAM_LINK = "homeTeam";
    private static final String AWAY_TEAM_LINK = "awayTeam";
    private String mApiKey;

    public FetchService() {
        super("FetchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApiKey = getString(R.string.api_key);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData("n2");
        getData("p2");
    }

    private void getData(String timeFrame) {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        if (!Utilies.isConnected(this)) {
            Utilies.showNotConnected(this);
            return;
        }

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        final String responseContent = Utilies.getUrlContent(fetch_build,mApiKey);

        try {
            if (!TextUtils.isEmpty(responseContent)) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(responseContent).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
//                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }

                processJSONdata(responseContent, getApplicationContext(), true);
            } else {
                //Could not Connect
//                Log.d(LOG_TAG, "Could not connect to server.");
                if (!Utilies.isConnected(this)) {
                    Utilies.showNotConnected(this);
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,"Error getting data",e);
        }
    }

    private void processJSONdata(String JSONdata, Context mContext, boolean isReal) {
        //Match data
        String league;
        String mDate;
        String mTime;
        String home;
        String away;
        String home_goals;
        String away_goals;
        String match_id;
        String match_day;

        try {

            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());

            for (int i = 0; i < matches.length(); i++) {

                JSONObject match_data = matches.getJSONObject(i);
                final JSONObject links = match_data.getJSONObject(LINKS);
                final String leagueUrl = links.getJSONObject(SOCCER_SEASON).getString("href");
                league = leagueUrl.replace(SEASON_LINK, "");

                Utilies.insertLeagueNameToDb(mContext, league, leagueUrl, mApiKey);

                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
               /* if(     League.equals(PREMIER_LEAGUE)      ||
                        League.equals(SERIE_A)             ||
                        League.equals(BUNDESLIGA1)         ||
                        League.equals(BUNDESLIGA2)         ||
                        League.equals(PRIMERA_DIVISION)     )
                {*/

                match_id = links.getJSONObject(SELF).getString("href");
                match_id = match_id.replace(MATCH_LINK, "");
                if (!isReal) {
                    //This if statement changes the match ID of the dummy data so that it all goes into the database
                    match_id = match_id + Integer.toString(i);
                }

                mDate = match_data.getString(MATCH_DATE);
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0, mDate.indexOf("T"));
                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date parseddate = match_date.parse(mDate + mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0, mDate.indexOf(":"));

                    if (!isReal) {
                        //This if statement changes the dummy data's date to match our current date range.
                        final Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                        mDate = Utilies.DATE_FORMAT.format(fragmentDate);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

                home = match_data.getString(HOME_TEAM);
                away = match_data.getString(AWAY_TEAM);

                final JSONObject homeTeamLinksJson = links.optJSONObject(HOME_TEAM_LINK);
                final JSONObject awayTeamLinksJson = links.optJSONObject(AWAY_TEAM_LINK);
                final String homeTeamLinks = homeTeamLinksJson == null ? null : homeTeamLinksJson.optString("href",null);
                final String awayTeamLinks = awayTeamLinksJson == null ? null : awayTeamLinksJson.optString("href",null);

                Utilies.insertTeamIconLinksToDb(mContext, home, homeTeamLinks,mApiKey);
                Utilies.insertTeamIconLinksToDb(mContext, away, awayTeamLinks,mApiKey);

                home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                match_day = match_data.getString(MATCH_DAY);
                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                match_values.put(DatabaseContract.scores_table.HOME_COL, home);
                match_values.put(DatabaseContract.scores_table.AWAY_COL, away);
                match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, home_goals);
                match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, away_goals);
                match_values.put(DatabaseContract.scores_table.LEAGUE_COL, league);
                match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                //log spam

                //Log.v(LOG_TAG,match_id);
                //Log.v(LOG_TAG,mDate);
                //Log.v(LOG_TAG,mTime);
                //Log.v(LOG_TAG,Home);
                //Log.v(LOG_TAG,Away);
                //Log.v(LOG_TAG,Home_goals);
                //Log.v(LOG_TAG,Away_goals);

                values.add(match_values);
            }
//            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(DatabaseContract.BASE_CONTENT_URI, insert_data);
            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }
}