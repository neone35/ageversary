package com.github.neone35.ageversary;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.neone35.ageversary.fragments.DatePickerDialogFragment;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.triggertrap.seekarc.SeekArc;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final int RC_SIGN_IN = 141;
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
    @BindView(R.id.constraintLayout)
    ConstraintLayout constraintLayout;
    private SharedPreferences ageSharedPreferences;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpActivity();
        ageSharedPreferences = this.getSharedPreferences(
                getString(R.string.age_preferences_key), Context.MODE_PRIVATE);

        setYearProgress(saYearProgress, tvYearProgress);
        startAnimations(saYearProgress);

        // social profile sign in button
        ivProfileHolder.setOnClickListener(view -> fbLoginButtonClick());
    }

    private void setUpActivity() {
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
        // set up action bar
        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_custom);
        }
    }

    private void fbLoginButtonClick() {
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
                                try {
                                    String email = object.getString("email");
                                    String birthday = object.getString("birthday"); // 01/31/1980 format
                                    Logger.d("onCompleted email: " + email);
                                    Logger.d("onCompleted birthday: " + birthday);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender,birthday");
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
        fbLoginButton.performClick();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        Logger.d("fbLoginButtonClick: " + isLoggedIn);
        if (isLoggedIn) {
            LoginManager.getInstance().logOut();
            fbLoginButton.performClick();
        }
    }

//    private void updateProfileUI(GoogleSignInAccount account) {
//        if (account != null) {
//            // turn off login listener
//            mainBinding.ivProfileHolder.setOnClickListener(null);
//            Uri photoUrl = account.getPhotoUrl();
//            String displayName = account.getDisplayName();
//            // load profile photo into view
//            Picasso picasso = Picasso.with(this);
//            picasso.load(photoUrl)
//                    .placeholder(R.drawable.account_circle_holder)
//                    .fit()
//                    .centerCrop()
//                    .transform(new CircleTransform())
//                    .into(mainBinding.ivProfileHolder);
//            // set profile name into view
//            mainBinding.tvProfileName.setText(displayName);
//        } else {
//            Toast.makeText(this, "Failed to login. Try again", Toast.LENGTH_SHORT).show();
//        }
//    }

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

    private void showBirthdayDialog() {
        FragmentManager fm = getSupportFragmentManager();
        DatePickerDialogFragment datePickerDialogFragment =
                DatePickerDialogFragment.newInstance("Choose your birthday");
        datePickerDialogFragment.show(fm, "birthdayPicker");
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        year = datePicker.getYear();
        monthOfYear = datePicker.getMonth();

        // years and months between now and birth
        // for tvProfileAge
        int yearNow = Calendar.getInstance().get(Calendar.YEAR);
        int monthNow = Calendar.getInstance().get(Calendar.MONTH);
        int yearAge = yearNow - year;
        int monthAge = monthNow + (12 - monthOfYear);
        Logger.d("onDateSet: " + monthNow + " " + monthOfYear);
        String userAgeYearsMonths = yearAge + "yr " + monthAge + "mo";

        // seconds, minutes, hours, days between now and birth
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        birthDate.set(Calendar.MONTH, monthOfYear);
        birthDate.set(Calendar.YEAR, year);
        Calendar today = Calendar.getInstance();
        long diffBirthNow = today.getTimeInMillis() - birthDate.getTimeInMillis();
        long seconds = diffBirthNow / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        // i need days, hours, minutes

        // save time dimensions to sharedPreferences
        ageSharedPreferences = this.getSharedPreferences(
                getString(R.string.age_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = ageSharedPreferences.edit();
        editor.putString(getString(R.string.user_age_key), userAgeYearsMonths);
        editor.apply();

        // update UI after choosing birth date
        tvProfileAge.setText(userAgeYearsMonths);
    }
}
