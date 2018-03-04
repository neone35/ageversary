package com.amaslov.android.ageversary;

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
import com.triggertrap.seekarc.SeekArc;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setActionBar();
        setYearProgress(mainBinding.saYearProgress, mainBinding.tvYearProgress);
        setAnimations();
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
