package com.github.neone35.ageversary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.CallbackManager;
import com.facebook.stetho.Stetho;
import com.github.neone35.ageversary.friends.FriendFragment;
import com.github.neone35.ageversary.pojo.User;
import com.github.neone35.ageversary.preferences.PreferencesFragment;
import com.github.neone35.ageversary.utils.PrefUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements FriendFragment.OnListFragmentInteractionListener {

    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_BIRTH_DATE = "user_birth_date";
    public static final String KEY_USER_PICTURE = "user_picture";
    public static final String KEY_USER_FRIENDS = "user_friends";
    public static final String TAG_MAIN_FRAGMENT = "main_fragment";
    public static final String TAG_FRIENDS_FRAGMENT = "friends_fragment";
    @BindView(R.id.bnv_main)
    BottomNavigationView bnvMain;
    public static PrefUtils mUserPrefs;
    public static boolean mIsLoggedIn;
    public static long mLaunchMillis;
    public static ArrayList<User> mFilteredFriendsList;
    public static ArrayList<String> mFacebookFriendsList;
    public static CallbackManager callbackManager;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpActivity();
        mLaunchMillis = System.currentTimeMillis();
        SharedPreferences userSharedPreferences = this.getSharedPreferences(
                PrefUtils.PREF_FILE_NAME, Context.MODE_PRIVATE);
        mUserPrefs = PrefUtils.getInstance(userSharedPreferences);
        mFilteredFriendsList = new ArrayList<>();
        mFacebookFriendsList = new ArrayList<>();
        mFragmentManager = getSupportFragmentManager();

        // inflate initial fragment
        mFragmentManager.beginTransaction()
                .replace(R.id.fl_main, MainFragment.newInstance(), TAG_MAIN_FRAGMENT)
                .commit();

        setupBottomNavListener();
    }

    private void setupBottomNavListener() {
        // bottom navigation
        bnvMain.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_profile:
                    Fragment mainFragment = MainFragment.newInstance();
                    // replace with main fragment only when friends fragment is active
//                    if (mFragmentManager.findFragmentByTag(TAG_FRIENDS_FRAGMENT) != null)
                        mFragmentManager.beginTransaction()
                                .replace(R.id.fl_main, mainFragment, TAG_MAIN_FRAGMENT)
                                .commit();
                    return true;
                case R.id.action_friends:
                    Fragment friendFragment = FriendFragment.newInstance(1, mFilteredFriendsList);
                    // replace with friends fragment only when main fragment is active
//                    if (mFragmentManager.findFragmentByTag(TAG_MAIN_FRAGMENT) != null)
                        mFragmentManager.beginTransaction()
                                .replace(R.id.fl_main, friendFragment, TAG_FRIENDS_FRAGMENT)
                                .commit();
                    return true;
            }
            return false;
        });
    }

    // needed for facebook 3rd party login activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpActivity() {
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
        // set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_custom);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    // disable menu buttons if user is logged off
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsLoggedIn) {
            menu.findItem(R.id.logout).setEnabled(true);
            menu.findItem(R.id.settings).setEnabled(true);
        } else {
            menu.findItem(R.id.logout).setEnabled(false);
            menu.findItem(R.id.settings).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                mFragmentManager.beginTransaction()
                        .replace(R.id.fl_main, new PreferencesFragment())
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    private void generateKeyHash() {
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String hashKey = new String(Base64.encode(md.digest(), 0));
//                Logger.i("printHashKey() Hash Key: " + hashKey);
//            }
//        } catch (NoSuchAlgorithmException e) {
//            Logger.e("printHashKey()", e);
//        } catch (Exception e) {
//            Logger.e("printHashKey()", e);
//        }
//    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // restore user info on switch
//        loadUserInfoFromPrefs();
    }


    @Override
    public void onListFragmentInteraction(User user) {
        Logger.d(user.getUsername() + " clicked!");
    }
}
