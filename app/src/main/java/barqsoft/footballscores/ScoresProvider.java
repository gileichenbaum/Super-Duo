package barqsoft.footballscores;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider {

    public static final String SCORES_BY_DATE = DatabaseContract.scores_table.DATE_COL + " LIKE ?";
    private static final String TAG = "ScoresProvider";
    private static final String SCORES_BY_LEAGUE = DatabaseContract.scores_table.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_ID = DatabaseContract.scores_table.MATCH_ID + " = ?";
    private static final String ICON_BY_TEAM_NAME = DatabaseContract.icons_table.TEAM_NAME_COL + " = ?";
    private static final String LEAGUE_NAME_BY_LEAGUE_NUMBER = DatabaseContract.league_table.LEAGUE_ID_COL + " = ?";

    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int ICON_URLS_WITH_TEAM_NAMES = 201;
    private static final int ICON_URLS = 202;
    private static final int LEAGUE_NAMES = 300;

    private static ScoresDBHelper sScoresOpenHelper;
    private static IconUrlsDBHelper sIconsOpenHelper;
    private static LeagueNamesDBHelper sLeaguesOpenHelper;
    private static UriMatcher sUriMatcher = buildUriMatcher();


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.BASE_CONTENT_URI.toString();
        matcher.addURI(authority, null, MATCHES);
        matcher.addURI(authority, "league", MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, "id", MATCHES_WITH_ID);
        matcher.addURI(authority, "date", MATCHES_WITH_DATE);
        matcher.addURI(authority, "teamName", ICON_URLS_WITH_TEAM_NAMES);
        matcher.addURI(authority, "teamIconUrl", ICON_URLS);
        matcher.addURI(authority, "league_names", LEAGUE_NAMES);
        return matcher;
    }

    private int match_uri(Uri uri) {
        String link = uri.toString();
        {
            if (link.contentEquals(DatabaseContract.BASE_CONTENT_URI.toString())) {
                return MATCHES;
            } else if (link.contentEquals(DatabaseContract.scores_table.buildScoreWithDate().toString())) {
                return MATCHES_WITH_DATE;
            } else if (link.contentEquals(DatabaseContract.scores_table.buildScoreWithId().toString())) {
                return MATCHES_WITH_ID;
            } else if (link.contentEquals(DatabaseContract.scores_table.buildScoreWithLeague().toString())) {
                return MATCHES_WITH_LEAGUE;
            } else if (link.contentEquals(DatabaseContract.icons_table.buildIconUrlsWithTeamNames().toString())) {
                return ICON_URLS_WITH_TEAM_NAMES;
            } else if (link.contentEquals(DatabaseContract.icons_table.buildIconUrls().toString())) {
                return ICON_URLS;
            } else if (link.contentEquals(DatabaseContract.league_table.buildLeagues().toString())) {
                return LEAGUE_NAMES;
            }
        }
        return -1;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext().getApplicationContext();
        sScoresOpenHelper = new ScoresDBHelper(context);
        sIconsOpenHelper = new IconUrlsDBHelper(context);
        sLeaguesOpenHelper = new LeagueNamesDBHelper(context);
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MATCHES:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case MATCHES_WITH_LEAGUE:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return DatabaseContract.scores_table.CONTENT_ITEM_TYPE;
            case MATCHES_WITH_DATE:
                return DatabaseContract.scores_table.CONTENT_TYPE;
            case ICON_URLS_WITH_TEAM_NAMES:
                return DatabaseContract.icons_table.CONTENT_TYPE;
            case ICON_URLS:
                return DatabaseContract.icons_table.CONTENT_ITEM_TYPE;
            case LEAGUE_NAMES:
                return DatabaseContract.league_table.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri :" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        //Log.v(FetchScoreTask.LOG_TAG,uri.getPathSegments().toString());
        int match = match_uri(uri);
        //Log.v(FetchScoreTask.LOG_TAG,SCORES_BY_LEAGUE);
        //Log.v(FetchScoreTask.LOG_TAG,selectionArgs[0]);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(match));
        switch (match) {
            case MATCHES:
                retCursor = sScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, null, null, null, null, sortOrder);
                break;
            case MATCHES_WITH_DATE:
                retCursor = sScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_DATE, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_ID:
                retCursor = sScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_LEAGUE:
                retCursor = sScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_LEAGUE, selectionArgs, null, null, sortOrder);
                break;
            case ICON_URLS_WITH_TEAM_NAMES:
                retCursor = sIconsOpenHelper.getReadableDatabase().query(
                        DatabaseContract.ICON_URLS_TABLE,
                        projection, ICON_BY_TEAM_NAME, selectionArgs, null, null, sortOrder);
                break;
            case ICON_URLS:
                retCursor = sIconsOpenHelper.getReadableDatabase().query(
                        DatabaseContract.ICON_URLS_TABLE,
                        projection, null, null, null, null, sortOrder);
                break;
            case LEAGUE_NAMES:
                retCursor = sLeaguesOpenHelper.getReadableDatabase().query(
                        DatabaseContract.LEAGUE_NAMES_TABLE,
                        projection, LEAGUE_NAME_BY_LEAGUE_NUMBER, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        switch (match_uri(uri)) {
            case LEAGUE_NAMES:
                insertValues(sLeaguesOpenHelper.getWritableDatabase(), DatabaseContract.LEAGUE_NAMES_TABLE, uri, values);
                break;
            case ICON_URLS_WITH_TEAM_NAMES:
            case ICON_URLS:
                insertValues(sIconsOpenHelper.getWritableDatabase(), DatabaseContract.ICON_URLS_TABLE, uri, values);
        }
        return null;
    }

    private void insertValues(final SQLiteDatabase sqLiteDatabase, final String tableName, final Uri uri, final ContentValues values) {
        final SQLiteDatabase db = sqLiteDatabase;
        db.beginTransaction();
        try {
            long _id = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = sScoresOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.SCORES_TABLE,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(sUriMatcher.match(uri)));
        switch (match_uri(uri)) {
            case MATCHES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
