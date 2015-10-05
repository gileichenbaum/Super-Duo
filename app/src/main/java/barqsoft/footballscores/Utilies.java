package barqsoft.footballscores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import barqsoft.footballscores.service.FetchService;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies {
    private static final String TAG = "Utilies";

    //JSON data
    // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
    // be updated. Feel free to use the codes
    /*private static final String BUNDESLIGA1 = "394";
    private static final String BUNDESLIGA2 = "395";
    private static final String LIGUE1 = "396";
    private static final String LIGUE2 = "397";
    private static final String PREMIER_LEAGUE = "398";
    private static final String PRIMERA_DIVISION = "399";
    private static final String SEGUNDA_DIVISION = "400";
    private static final String SERIE_A = "401";
    private static final String PRIMERA_LIGA = "402";
    private static final String Bundesliga3 = "403";
    private static final String EREDIVISIE = "404";*/

    public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;
    private static RequestQueue mQueue;
    private static HashMap<String, AsyncTask> mIconTasks = new HashMap<>();

    public static String getLeague(int league_num) {
        switch (league_num) {
            case SERIE_A:
                return "Seria A";
            case PREMIER_LEGAUE:
                return "Premier League";
            case CHAMPIONS_LEAGUE:
                return "UEFA Champions League";
            case PRIMERA_DIVISION:
                return "Primera Division";
            case BUNDESLIGA:
                return "Bundesliga";
            default:
                return "Not known League Please report";
        }
    }

    public static String getMatchDay(int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return "Group Stages, Matchday : 6";
            } else if (match_day == 7 || match_day == 8) {
                return "First Knockout round";
            } else if (match_day == 9 || match_day == 10) {
                return "QuarterFinal";
            } else if (match_day == 11 || match_day == 12) {
                return "SemiFinal";
            } else {
                return "Final";
            }
        } else {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals) {
        if (home_goals < 0 || awaygoals < 0) {
            return " - ";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname) {
        if (teamname == null) {
            return 0;
        }
        switch (teamname) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC":
                return R.drawable.arsenal;
            case "Manchester United FC":
                return R.drawable.manchester_united;
            case "Swansea City":
                return R.drawable.swansea_city_afc;
            case "Leicester City":
                return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC":
                return R.drawable.everton_fc_logo1;
            case "West Ham United FC":
                return R.drawable.west_ham;
            case "Tottenham Hotspur FC":
                return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion":
                return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC":
                return R.drawable.sunderland;
            case "Stoke City FC":
                return R.drawable.stoke_city;
            default:
                return 0;
        }
    }

    public static boolean isConnected(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static Toast showNotConnected(final Context context) {
        final Toast toast = Toast.makeText(context, R.string.no_connection, Toast.LENGTH_LONG);
        toast.show();
        return toast;
    }

    public static ContentValues buildTeamIconUrlContentValues(final String teamName, final String iconUrl, final String teamDataLink) {

        if (TextUtils.isEmpty(teamName)) {
            return null;
        }

        final ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.icons_table.TEAM_NAME_COL, teamName);

        if (!TextUtils.isEmpty(teamDataLink)) {
            cv.put(DatabaseContract.icons_table.TEAM_DATA_LINK_COL, teamDataLink);
        }

        if (!TextUtils.isEmpty(iconUrl)) {
            cv.put(DatabaseContract.icons_table.ICON_URL_COL, iconUrl);
        }

        return cv;
    }

    public static void insertTeamIconLinksToDb(final Context context, final String teamName, final String teamDataLink, final String apiKey) {

        final Cursor a = context.getContentResolver().query(DatabaseContract.icons_table.buildIconUrlsWithTeamNames(), null, null, new String[]{teamName}, null);

        if (a == null) {
            return;
        }

        if (!a.moveToFirst() || TextUtils.isEmpty(a.getString(a.getColumnIndex(DatabaseContract.icons_table.ICON_URL_COL)))) {

            final String responseContent = getUrlContent(Uri.parse(teamDataLink), apiKey);

            if (TextUtils.isEmpty(responseContent)) {
                a.close();
                return;
            }

            String iconUrl = null;
            try {
                final JSONObject teamDataJson = new JSONObject(responseContent);
                iconUrl = teamDataJson.optString(FetchService.TEAM_ICON_URL_JSON_KEY, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ContentValues teamIconUrlValues = Utilies.buildTeamIconUrlContentValues(teamName, iconUrl, teamDataLink);

            if (iconUrl != null && iconUrl.endsWith(".svg")) {
                addByteArraySvgFromUrl(context, iconUrl, teamIconUrlValues);
            }
            if (teamIconUrlValues != null && teamIconUrlValues.size() > 1) {
                context.getContentResolver().insert(DatabaseContract.icons_table.buildIconUrls(), teamIconUrlValues);
            }
        }

        a.close();
    }

    public static String getUrlContent(final Uri uri, final String apiKey) {

        HttpURLConnection m_connection = null;
        BufferedReader reader = null;
        String responseContent = null;
        //Opening Connection
        try {
            URL fetch = new URL(uri.toString());
            m_connection = (HttpURLConnection) fetch.openConnection();
            m_connection.setRequestMethod("GET");
            m_connection.setConnectTimeout(20000);
            m_connection.setReadTimeout(20000);
            m_connection.setDoOutput(false);

            if (apiKey != null) {
                m_connection.addRequestProperty("X-Auth-Token", apiKey);
            }
            m_connection.connect();

            // Read the input stream into a String
            InputStream inputStream = m_connection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            final StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            responseContent = buffer.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error reading url content from " + uri.toString() + ", e= " + e.getMessage(), e);
        } finally {
            if (m_connection != null) {
                m_connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error Closing Stream");
                }
            }
        }
        return responseContent;
    }

    public static void getTeamIconUrlAsync(final Context context, final String teamName, final String teamDataUrl) {

        AsyncTask task = mIconTasks.get(teamName);

        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {

            task = new AsyncTask<Object, Integer, String>() {
                @Override
                protected String doInBackground(final Object... params) {

                    final Context context = (Context) params[0];
                    final String teamName = (String) params[1];
                    final String teamDataUrl = (String) params[2];

                    if (Utilies.isConnected(context)) {
                        Utilies.insertTeamIconLinksToDb(context, teamName, teamDataUrl, context.getString(R.string.api_key));
                    }
                    return null;
                }
            }.execute(context.getApplicationContext(), teamName, teamDataUrl);

            mIconTasks.put(teamName, task);

        }
    }

    public static void getTeamSVGIconAsync(final Context context, final String teamName, final String iconUrl) {

        new AsyncTask<Object, Integer, String>() {
            @Override
            protected String doInBackground(final Object... params) {

                final Context context = (Context) params[0];
                final String teamName = (String) params[1];
                final String iconUrl = (String) params[2];

                if (Utilies.isConnected(context)) {
                    final ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.icons_table.TEAM_NAME_COL, teamName);
                    addByteArraySvgFromUrl(context, iconUrl, cv);
                    if (cv.size() > 1) {
                        context.getContentResolver().insert(DatabaseContract.icons_table.buildIconUrls(), cv);
                    }
                }
                return null;
            }
        }.execute(context.getApplicationContext(), teamName, iconUrl);
    }

    private static void addByteArraySvgFromUrl(final Context context, final String url, final ContentValues values) {
        if (url != null && url.endsWith(".svg")) {

            final StringRequest stringRequest = new StringRequest(Request.Method.GET, url.replace("http:", "https:"),

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {

                            try {
                                if (!TextUtils.isEmpty(response)) {

                                    final SVG svg = SVG.getFromString(response);

                                    if (svg != null) {
                                        final int imageSize = (int) context.getResources().getDimension(R.dimen.team_crest_size);
                                        final Canvas canvas = new Canvas();
                                        final int width = (int) Math.max(Math.ceil(svg.getDocumentWidth()), imageSize);
                                        final int height = (int) Math.max(Math.ceil(svg.getDocumentHeight()), imageSize);
                                        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                        canvas.setBitmap(bmp);
                                        svg.renderToCanvas(canvas);
                                        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        final Bitmap scaled = Bitmap.createScaledBitmap(bmp, imageSize, imageSize, false);
                                        bmp.recycle();
                                        scaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        scaled.recycle();
                                        values.put(DatabaseContract.icons_table.IMAGE_BLOB, stream.toByteArray());
                                        stream.flush();
                                        stream.close();
                                        if (values.size() > 1) {
                                            context.getContentResolver().insert(DatabaseContract.icons_table.buildIconUrls(), values);
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "url " + url + " returned no content");
                                }
                            } catch (SVGParseException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    ,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(final VolleyError error) {

                        }
                    });

            if (mQueue == null) {
                mQueue = Volley.newRequestQueue(context.getApplicationContext());
            }

            mQueue.add(stringRequest);
        }
    }
}
