package barqsoft.footballscores;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract
{
    public static final String SCORES_TABLE = "scores_table";
    public static final String ICON_URLS_TABLE = "icon_urls_table";
    public static final String LEAGUE_NAMES_TABLE = "league_names_table";
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String SCORES_PATH = "scores";
    public static final String ICONS_PATH = "icons";
    public static final String LEAGUE_PATH = "league";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    public static final class scores_table implements BaseColumns {
        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";

        //public static Uri SCORES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(SCORES_PATH)
        //.build();

        //Types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + SCORES_PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + SCORES_PATH;

        public static Uri buildScoreWithLeague() {
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }

        public static Uri buildScoreWithId() {
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }

        public static Uri buildScoreWithDate() {
            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
        }
    }

    public static final class icons_table implements BaseColumns {
        //Table data
        public static final String TEAM_NAME_COL = "name";
        public static final String ICON_URL_COL = "icon_url";
        public static final String TEAM_DATA_LINK_COL = "team_data_link";
        public static final String IMAGE_BLOB = "image_blob";

        //Types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + ICONS_PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + ICONS_PATH;

        public static Uri buildIconUrls() {
            return BASE_CONTENT_URI.buildUpon().appendPath("teamIconUrl").build();
        }

        public static Uri buildIconUrlsWithTeamNames() {
            return BASE_CONTENT_URI.buildUpon().appendPath("teamName").build();
        }
    }

    public static final class league_table implements BaseColumns {
        //Table data
        public static final String LEAGUE_NAME_COL = "name";
        public static final String LEAGUE_URL_COL = "url";
        public static final String LEAGUE_ID_COL = "league_id";

        //Types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + LEAGUE_PATH;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + LEAGUE_PATH;

        public static Uri buildLeagues() {
            return BASE_CONTENT_URI.buildUpon().appendPath("league_names").build();
        }
    }
}
