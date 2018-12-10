package com.github.neone35.ageversary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.stetho.Stetho;
import com.github.neone35.ageversary.utils.CircleTransform;
import com.github.neone35.ageversary.utils.PrefUtils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.triggertrap.seekarc.SeekArc;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_BIRTH_DATE = "user_birth_date";
    private static final String KEY_USER_PICTURE = "user_picture";
    @BindView(R.id.iv_year_holder)
    ImageView ivYearHolder;
    @BindView(R.id.iv_share_holder)
    ImageView ivShareHolder;
    @BindView(R.id.iv_profile_holder)
    ImageView ivProfileHolder;
    @BindView(R.id.fb_login_button)
    LoginButton fbLoginButton;
    @BindView(R.id.tv_profile_name)
    TextView tvProfileName;
    @BindView(R.id.tv_profile_age)
    TextView tvProfileAge;
    @BindView(R.id.tv_year_progress)
    TextView tvYearProgress;
    @BindView(R.id.tv_year_progress_label)
    TextView tvYearProgressLabel;
    @BindView(R.id.sa_year_progress)
    SeekArc saYearProgress;
    @BindView(R.id.tv_days_anniversary)
    TextView tvDaysAnniversary;
    @BindView(R.id.tv_days_to_go)
    TextView tvDaysToGo;
    @BindView(R.id.tv_days)
    TextView tvDays;
    private CallbackManager callbackManager;
    private PrefUtils mUserPrefs;
    private boolean mIsLoggedIn;
    private long mBirthMillis;
    private long mNowMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setUpActivity();
        mNowMillis = System.currentTimeMillis();
        SharedPreferences userSharedPreferences = this.getSharedPreferences(
                PrefUtils.PREF_FILE_NAME, Context.MODE_PRIVATE);
        mUserPrefs = PrefUtils.getInstance(userSharedPreferences);

        setYearProgress(saYearProgress, tvYearProgress);
        startAnimations(saYearProgress);
        loadUserInfoFromPrefs();
        setUpListeners();
        // if user is logged off & no preferences are set
        if (!mIsLoggedIn && mUserPrefs.getString(KEY_USER_PICTURE).isEmpty()) {
            blinkAnim(ivProfileHolder);
            showAgeWidgets(false);
        } else {
            // set global birthMillis
            Logger.d(mUserPrefs.getString(KEY_USER_BIRTH_DATE));
            getUserAge(mUserPrefs.getString(KEY_USER_BIRTH_DATE));
            // find ms interval between birth & now
            Duration dur = new Duration(mBirthMillis, mNowMillis);
            long daysAge = dur.getStandardDays();
            setupAgeWidgets(daysAge);
            Logger.d(mNowMillis);
            Logger.d(mBirthMillis);

            int nextDaysAnniv = (int) roundUp(daysAge, 100);
            tvDaysAnniversary.setText(String.valueOf(nextDaysAnniv));
            tvDaysToGo.setText(String.valueOf(nextDaysAnniv - daysAge));
        }
    }

    private void setupAgeWidgets(long daysAge) {
        // duration in ms between two instants
        tvDays.setText(String.valueOf(daysAge));
    }

    // finds next i number dividable by v number
    double roundUp(double i, int v) {
        return Math.ceil(i / v) * v;
    }

    private void showAgeWidgets(boolean show) {
        View incDays = findViewById(R.id.inc_days);
        if (!show) {
            incDays.setVisibility(View.GONE);
        } else {
            incDays.setVisibility(View.VISIBLE);
        }
    }

    // needed for facebook 3rd party login activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void blinkAnim(View view) {
        Animation anim = new AlphaAnimation(0.7f, 1.0f);
        anim.setDuration(1000); // You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

    private void setUpListeners() {
        // social profile sign in button
        ivProfileHolder.setOnClickListener(view -> fbLogInOff());
        fbLoginButton.setOnClickListener(v -> {
            fbLoginButton.setReadPermissions(Arrays.asList(
                    "public_profile", "email", "user_birthday", "user_friends"));
            callbackManager = CallbackManager.Factory.create();
            // Callback registration
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // App code
                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            (object, response) -> {
                                Logger.v("LoginActivity", response.toString());
                                // Application code
                                fbLoginButton.setVisibility(View.GONE);
                                updateProfileUI(response);
                                ivProfileHolder.getAnimation().cancel();
                                showAgeWidgets(true);
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender,birthday,picture.type(large)");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    // App code
                    Logger.v("LoginActivity", "cancel");
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                    Logger.v("LoginActivity", exception.getCause().toString());
                }
            });
        });
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

    private void fbLogInOff() {
        fbLoginButton.performClick();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        mIsLoggedIn = accessToken != null && !accessToken.isExpired();
        Logger.d("fbLogInOff: " + mIsLoggedIn);
        if (mIsLoggedIn) {
            LoginManager.getInstance().logOut();
            // show facebook login button
            fbLoginButton.setVisibility(View.VISIBLE);
            // erase & load empty user preferences
            mUserPrefs.clear();
            loadUserInfoFromPrefs();
            // enable blinking again (logger off)
            blinkAnim(ivProfileHolder);
            showAgeWidgets(false);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // restore user info on switch
//        loadUserInfoFromPrefs();
    }

    // load user info from preferences
    private void loadUserInfoFromPrefs() {
        if (mUserPrefs != null) {
            // load user prefs if they exist
            if (!mUserPrefs.getString(KEY_USER_BIRTH_DATE).isEmpty())
                tvProfileAge.setText(getUserAge(mUserPrefs.getString(KEY_USER_BIRTH_DATE)));
            else {
                tvProfileAge.setText(getString(R.string.default_age));
            }
            if (!mUserPrefs.getString(KEY_USER_NAME).isEmpty())
                tvProfileName.setText(mUserPrefs.getString(KEY_USER_NAME));
            else {
                tvProfileName.setText(getString(R.string.no_name));
            }
            if (!mUserPrefs.getString(KEY_USER_PICTURE).isEmpty())
                loadUserPicture(mUserPrefs.getString(KEY_USER_PICTURE));
            else {
                loadUserPicture(String.valueOf(R.drawable.account_circle_holder));
            }

            // show facebook login button only if user info is empty
            if (mUserPrefs.getString(KEY_USER_BIRTH_DATE).isEmpty() &&
                    mUserPrefs.getString(KEY_USER_NAME).isEmpty() &&
                    mUserPrefs.getString(KEY_USER_PICTURE).isEmpty()) {
                fbLoginButton.setVisibility(View.VISIBLE);
            }
        }
    }

    // save info to preferences
    private void saveUserInfoToPrefs(String name, String birthDate, String pictureUrl) {
        mUserPrefs.clear();
        if (tvProfileAge.getText() != getString(R.string.default_age)) {
            if (mUserPrefs.getString(KEY_USER_BIRTH_DATE).isEmpty())
                mUserPrefs.putString(KEY_USER_BIRTH_DATE, birthDate);
        }
        if (tvProfileName.getText() != getString(R.string.no_name)) {
            if (mUserPrefs.getString(KEY_USER_NAME).isEmpty())
                mUserPrefs.putString(KEY_USER_NAME, name);
        }
        if (ivProfileHolder.getDrawable() != null) {
            if (mUserPrefs.getString(KEY_USER_PICTURE).isEmpty())
                mUserPrefs.putString(KEY_USER_PICTURE, pictureUrl);
        }
    }

    // load profile photo into view
    private void loadUserPicture(String pictureUrl) {
        Picasso.get().load(pictureUrl)
                .placeholder(R.drawable.account_circle_holder)
                .fit()
                .centerCrop()
                .transform(new CircleTransform())
                .into(ivProfileHolder);
    }

    private void updateProfileUI(GraphResponse response) {
        if (response != null) {
            // turn off login listener
//            ivProfileHolder.setOnClickListener(null);
            String userName;
            String userBirthDate;
            String userPhotoUrl;
            try {
                JSONObject resObj = response.getJSONObject();
                userName = resObj.getString("name");
                userBirthDate = resObj.getString("birthday"); // 01/31/1980 format
                userPhotoUrl = resObj.getJSONObject("picture").getJSONObject("data").getString("url");

                // load user picture
                loadUserPicture(userPhotoUrl);
                // set profile name into view
                tvProfileName.setText(userName);
                // find & set user age into view
                String userAge = getUserAge(userBirthDate);
                tvProfileAge.setText(userAge);
                // save set user info to prefs file
                saveUserInfoToPrefs(userName, userBirthDate, userPhotoUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtils.showShort("Failed to login. Try again");
        }
    }

    private String getUserAge(String userBirthDate) { // 01/31/1980 format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        mBirthMillis = TimeUtils.string2Millis(userBirthDate, dateFormat);
        Period period = new Period(mBirthMillis, mNowMillis);
        return getString(R.string.age_holder, period.getYears(), period.getMonths(), period.getDays());
    }

    private void setYearProgress(SeekArc sa, TextView tv) {
        final int DAYS_YEAR = 365;
        final int ANGLE_MAX = 360;
        int currentDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int percentOfYear = 100 * currentDayOfYear / DAYS_YEAR;
        if (currentDayOfYear > ANGLE_MAX) {
            sa.setSweepAngle(360);
        } else {
            sa.setSweepAngle(currentDayOfYear);
        }
        String percentFinal =
                String.valueOf(percentOfYear) + getResources().getString(R.string.percent);
        tv.setText(percentFinal);
    }

    private void startAnimations(SeekArc sa) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setStartOffset(500);
        fadeIn.setDuration(750);
        fadeIn.setRepeatMode(Animation.REVERSE);
        sa.setAnimation(fadeIn);
    }
}
