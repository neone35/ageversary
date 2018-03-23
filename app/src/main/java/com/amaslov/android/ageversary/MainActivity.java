package com.amaslov.android.ageversary;

import android.accounts.Account;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.amaslov.android.ageversary.databinding.ActivityMainBinding;
import com.amaslov.android.ageversary.fragments.DatePickerDialogFragment;
import com.amaslov.android.ageversary.utilities.CircleTransform;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.Login;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.triggertrap.seekarc.SeekArc;

import java.util.Arrays;
import java.util.Calendar;

import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private final String TAG = "MainActivity";
    private final int RC_SIGN_IN = 141;
    private ActivityMainBinding mainBinding;
    private SharedPreferences ageSharedPreferences;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ageSharedPreferences = this.getSharedPreferences(
                getString(R.string.age_preferences_key), Context.MODE_PRIVATE);

        setActionBar();
        setYearProgress(mainBinding.saYearProgress, mainBinding.tvYearProgress);
        startAnimations(mainBinding.saYearProgress);


        // G+ profile sign in button
        mainBinding.ivProfileHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                googleSignIn();
                loginButton = (LoginButton) view;
                loginButton.setReadPermissions(Arrays.asList(
                        "public_profile", "email", "user_birthday", "user_friends"));
                callbackManager = CallbackManager.Factory.create();
                // Callback registration
                loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());
                                        // Application code
                                        try {
                                            String email = object.getString("email");
                                            String birthday = object.getString("birthday"); // 01/31/1980 format
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

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
                        Log.v("LoginActivity", "cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.v("LoginActivity", exception.getCause().toString());
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null)
            updateProfileUI(account);

        String defaultAge = getString(R.string.default_age);
        String userAge = ageSharedPreferences.getString(getString(R.string.user_age_key), defaultAge);
        if (userAge.equals(defaultAge)) {
            showBirthdayDialog();
        } else {
            mainBinding.tvProfileAge.setText(userAge);
        }
    }

    // Prompt to choose account
    private void googleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(getLocalClassName(), "handleSignInResult: " + account.getAccount());
                updateProfileUI(account);
            } catch (ApiException e) {
                Log.w(getLocalClassName(), "signInResult:failed code=" + e.getStatusCode());
                updateProfileUI(null);
            }
        }
    }

    private void updateProfileUI(GoogleSignInAccount account) {
        if (account != null) {
            // turn off login listener
            mainBinding.ivProfileHolder.setOnClickListener(null);
            Uri photoUrl = account.getPhotoUrl();
            String displayName = account.getDisplayName();
            // load profile photo into view
            Picasso picasso = Picasso.with(this);
            picasso.load(photoUrl)
                    .placeholder(R.drawable.account_circle_holder)
                    .fit()
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(mainBinding.ivProfileHolder);
            // set profile name into view
            mainBinding.tvProfileName.setText(displayName);
        } else {
            Toast.makeText(this, "Failed to login. Try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void setActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_custom);
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
        Log.d(TAG, "onDateSet: " + monthNow + " " + monthOfYear);
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
        mainBinding.tvProfileAge.setText(userAgeYearsMonths);
    }
}
