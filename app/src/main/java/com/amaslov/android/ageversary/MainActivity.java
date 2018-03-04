package com.amaslov.android.ageversary;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.amaslov.android.ageversary.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.triggertrap.seekarc.SeekArc;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 141;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setActionBar();
        setYearProgress(mainBinding.saYearProgress, mainBinding.tvYearProgress);
        setAnimations();

        this.mainBinding.ivProfileHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
                signIn();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(getLocalClassName(), "handleSignInResult: " + account.getPhotoUrl());
            Picasso.with(this)
                    .load(account.getPhotoUrl())
                    .resize(85, 85)
                    .centerCrop()
                    .into(mainBinding.ivProfileHolder);
            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(getLocalClassName(), "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void setActionBar () {
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

    private void setAnimations(){
        View saYear = mainBinding.saYearProgress;
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setStartOffset(500);
        fadeIn.setDuration(750);
        fadeIn.setRepeatMode(Animation.REVERSE);
        saYear.setAnimation(fadeIn);
    }
}
