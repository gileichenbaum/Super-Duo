package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity
{
    public static int sSelectedMatchId;
    public static int mCurrentFragment = 2;
    private PagerFragment mPagerFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Log.d(LOG_TAG, "Reached MainActivity onCreate");

        final Intent intent = getIntent();

        if (intent != null && intent.hasExtra(FootballScoresWidget.MATCH_DAY)) {
            mCurrentFragment = intent.getIntExtra(FootballScoresWidget.MATCH_DAY,mCurrentFragment);
        }

        if (savedInstanceState == null) {
            mPagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, mPagerFragment).commit();
        }

//        DatabaseUtils.dumpCursor(getContentResolver().query(DatabaseContract.icons_table.buildIconUrls(),null,null,null,null));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
//        Log.v(save_tag,"will save");
//        Log.v(save_tag,"fragment: "+String.valueOf(mPagerFragment.mPagerHandler.getCurrentItem()));
//        Log.v(save_tag,"selected id: "+sSelectedMatchId);
        outState.putInt("Pager_Current", mPagerFragment.mPagerHandler.getCurrentItem());
        outState.putInt("Selected_match", sSelectedMatchId);
        getSupportFragmentManager().putFragment(outState,"mPagerFragment", mPagerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
//        Log.v(save_tag,"will retrive");
//        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt("Pager_Current")));
//        Log.v(save_tag,"selected id: "+savedInstanceState.getInt("Selected_match"));
        mCurrentFragment = savedInstanceState.getInt("Pager_Current");
        sSelectedMatchId = savedInstanceState.getInt("Selected_match");
        mPagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,"mPagerFragment");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
