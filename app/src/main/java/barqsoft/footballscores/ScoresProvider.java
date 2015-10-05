package barqsoft.footballscores;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class ScoresProvider extends ContentProvider {
    private static final String TAG = "ScoresProvider";
    private static ScoresDBHelper mScoresOpenHelper;
    private static final int MATCHES = 100;
    private static final int MATCHES_WITH_LEAGUE = 101;
    private static final int MATCHES_WITH_ID = 102;
    private static final int MATCHES_WITH_DATE = 103;
    private static final int ICON_URLS_WITH_TEAM_NAMES = 201;
    private static final int ICON_URLS = 202;
    private UriMatcher muriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder ScoreQuery =
            new SQLiteQueryBuilder();
    private static final String SCORES_BY_LEAGUE = DatabaseContract.scores_table.LEAGUE_COL + " = ?";
    private static final String SCORES_BY_DATE =
            DatabaseContract.scores_table.DATE_COL + " LIKE ?";
    private static final String SCORES_BY_ID =
            DatabaseContract.scores_table.MATCH_ID + " = ?";

    private static final String ICON_BY_TEAM_NAME = DatabaseContract.icons_table.TEAM_NAME_COL + " = ?";
    private static final String ICON_BY_ID = DatabaseContract.icons_table._ID + " = ?";
    private IconUrlsDBHelper mIconsOpenHelper;


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.BASE_CONTENT_URI.toString();
        matcher.addURI(authority, null, MATCHES);
        matcher.addURI(authority, "league", MATCHES_WITH_LEAGUE);
        matcher.addURI(authority, "id", MATCHES_WITH_ID);
        matcher.addURI(authority, "date", MATCHES_WITH_DATE);
        matcher.addURI(authority, "teamName", ICON_URLS_WITH_TEAM_NAMES);
        matcher.addURI(authority, "teamIconUrl", ICON_URLS);
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
            }
        }
        return -1;
    }

    @Override
    public boolean onCreate() {
        mScoresOpenHelper = new ScoresDBHelper(getContext());
        mIconsOpenHelper = new IconUrlsDBHelper(getContext());
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        final int match = muriMatcher.match(uri);
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
                retCursor = mScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, null, null, null, null, sortOrder);
                break;
            case MATCHES_WITH_DATE:
                Log.v(TAG, "args=" + (selectionArgs == null ? "null" : Arrays.toString(selectionArgs)));
                retCursor = mScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_DATE, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_ID:
                retCursor = mScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_ID, selectionArgs, null, null, sortOrder);
                break;
            case MATCHES_WITH_LEAGUE:
                retCursor = mScoresOpenHelper.getReadableDatabase().query(
                        DatabaseContract.SCORES_TABLE,
                        projection, SCORES_BY_LEAGUE, selectionArgs, null, null, sortOrder);
                break;
            case ICON_URLS_WITH_TEAM_NAMES:
                retCursor = mIconsOpenHelper.getReadableDatabase().query(
                        DatabaseContract.ICON_URLS_TABLE,
                        projection, ICON_BY_TEAM_NAME, selectionArgs, null, null, sortOrder);
                break;
            case ICON_URLS:
                retCursor = mIconsOpenHelper.getReadableDatabase().query(
                        DatabaseContract.ICON_URLS_TABLE,
                        projection, null, null, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        switch (match_uri(uri))
        {
            case ICON_URLS_WITH_TEAM_NAMES:
            case ICON_URLS:
                SQLiteDatabase db = mIconsOpenHelper.getWritableDatabase();
                db.beginTransaction();
                try
                {
                    long _id = db.insertWithOnConflict(DatabaseContract.ICON_URLS_TABLE, null, values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
        }
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mScoresOpenHelper.getWritableDatabase();
        //db.delete(DatabaseContract.SCORES_TABLE,null,null);
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(muriMatcher.match(uri)));
        switch (match_uri(uri)) {
            case MATCHES:
                db.beginTransaction();
                int returncount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(DatabaseContract.SCORES_TABLE, null, value,
                                SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returncount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returncount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
}
