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
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_AGE = "user_age";
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
    @BindView(R.id.cl_header)
    ConstraintLayout constraintLayout;
    @BindView(R.id.tv_days_anniversary)
    TextView tvDaysAnniversary;
    @BindView(R.id.tv_days_to_go)
    TextView tvToGo;
    @BindView(R.id.tv_days)
    TextView tvDays;
    private CallbackManager callbackManager;
    private PrefUtils mUserPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setUpActivity();
        SharedPreferences userSharedPreferences = this.getSharedPreferences(
                PrefUtils.PREF_FILE_NAME, Context.MODE_PRIVATE);
        mUserPrefs = PrefUtils.getInstance(userSharedPreferences);

        setYearProgress(saYearProgress, tvYearProgress);
        startAnimations(saYearProgress);
        loadUserInfoFromPrefs();
        setUpListeners();
    }

    // needed for facebook login activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        Logger.d("fbLogInOff: " + isLoggedIn);
        if (isLoggedIn) {
            LoginManager.getInstance().logOut();
            // show facebook login button
            fbLoginButton.setVisibility(View.VISIBLE);
            // erase & load empty user preferences
            mUserPrefs.clear();
            loadUserInfoFromPrefs();
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
            if (!mUserPrefs.getString(KEY_USER_AGE).isEmpty())
                tvProfileAge.setText(mUserPrefs.getString(KEY_USER_AGE));
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
            if (mUserPrefs.getString(KEY_USER_AGE).isEmpty() &&
                    mUserPrefs.getString(KEY_USER_NAME).isEmpty() &&
                    mUserPrefs.getString(KEY_USER_PICTURE).isEmpty()) {
                fbLoginButton.setVisibility(View.VISIBLE);
            }
        }
    }

    // save info to preferences
    private void saveUserInfoToPrefs(String name, String age, String pictureUrl) {
        mUserPrefs.clear();
        if (tvProfileAge.getText() != getString(R.string.default_age)) {
            if (mUserPrefs.getString(KEY_USER_AGE).isEmpty())
                mUserPrefs.putString(KEY_USER_AGE, age);
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
                loadUserPicture(userPhotoUrl);
                // set profile name into view
                tvProfileName.setText(userName);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                long birthMillis = TimeUtils.string2Millis(userBirthDate, dateFormat);
                long nowMillis = System.currentTimeMillis();
                Period period = new Period(birthMillis, nowMillis);
                String userAge = getString(R.string.age_holder, period.getYears(), period.getMonths(), period.getDays());
                // set user age into view
                tvProfileAge.setText(userAge);
                // duration in ms between two instants
                Duration dur = new Duration(birthMillis, nowMillis);
                tvDays.setText(String.valueOf(dur.getStandardDays()));
                saveUserInfoToPrefs(userName, userAge, userPhotoUrl);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtils.showShort("Failed to login. Try again");
        }
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
