package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.icons_table;

public class IconUrlsDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Icons.db";
    private static final int DATABASE_VERSION = 1;

    public IconUrlsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(final SQLiteDatabase db) {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.ICON_URLS_TABLE + " ("
                + icons_table._ID + " INTEGER PRIMARY KEY,"
                + icons_table.TEAM_NAME_COL + " TEXT NOT NULL,"
                + icons_table.ICON_URL_COL + " TEXT,"
                + icons_table.IMAGE_BLOB + " BLOB,"
                + icons_table.TEAM_DATA_LINK_COL + " TEXT,"
                + " UNIQUE (" + icons_table.TEAM_NAME_COL + ") ON CONFLICT REPLACE"
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
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ICON_URLS_TABLE);
        createTable(db);
    }

}
