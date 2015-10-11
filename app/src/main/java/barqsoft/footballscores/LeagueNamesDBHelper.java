package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.league_table;

public class LeagueNamesDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Leagues.db";
    private static final int DATABASE_VERSION = 1;

    public LeagueNamesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(final SQLiteDatabase db) {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.LEAGUE_NAMES_TABLE + " ("
                + league_table._ID + " INTEGER PRIMARY KEY,"
                + league_table.LEAGUE_URL_COL + " TEXT NOT NULL,"
                + league_table.LEAGUE_NAME_COL + " TEXT,"
                + league_table.LEAGUE_ID_COL + " TEXT NOT NULL,"
                + " UNIQUE (" + league_table.LEAGUE_ID_COL + ") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreateTable(db);
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        recreateTable(db);
    }

    private void recreateTable(final SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.LEAGUE_NAMES_TABLE);
        createTable(db);
    }

}
