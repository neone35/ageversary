package com.amaslov.android.ageversary;

import android.accounts.Account;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;
import com.amaslov.android.ageversary.databinding.ActivityMainBinding;
import com.amaslov.android.ageversary.fragments.BirthdayDialogFragment;
import com.amaslov.android.ageversary.utilities.CircleTransform;
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
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 141;
    ActivityMainBinding mainBinding;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setActionBar();
        setYearProgress(mainBinding.saYearProgress, mainBinding.tvYearProgress);
        setAnimations(mainBinding.saYearProgress);

        // Profile sign in button
        mainBinding.ivProfileHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if (account != null)
//            updateProfileUI(account);
    }

    // Prompt to choose account
    private void signIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        showBirthdayDialog();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showBirthdayDialog() {
        FragmentManager fm = getSupportFragmentManager();
        BirthdayDialogFragment birthdayDialogFragment = BirthdayDialogFragment.newInstance("Pick your birth date");
        birthdayDialogFragment.show(fm, "fragment_edit_name");
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
            // set birthday into view
            // TODO: get answer from dialog fragment
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

    private void setAnimations(SeekArc sa) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setStartOffset(500);
        fadeIn.setDuration(750);
        fadeIn.setRepeatMode(Animation.REVERSE);
        sa.setAnimation(fadeIn);
    }
}
