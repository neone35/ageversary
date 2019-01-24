package com.github.neone35.ageversary;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import com.github.neone35.ageversary.pojo.User;
import com.github.neone35.ageversary.utils.CircleTransform;
import com.github.neone35.ageversary.utils.MathUtils;
import com.github.neone35.ageversary.utils.PrefUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.triggertrap.seekarc.SeekArc;

import org.joda.time.Duration;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainFragment extends Fragment {

    private static final String ARG_PARAM = "param";
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
    @BindView(R.id.sa_year_progress)
    SeekArc saYearProgress;
    @BindView(R.id.tv_days_anniversary)
    TextView tvDaysAnniversary;
    @BindView(R.id.tv_days)
    TextView tvDays;
    @BindView(R.id.tv_days_percent)
    TextView tvDaysPercent;
    @BindView(R.id.tv_days_date)
    TextView tvDaysDate;
    @BindView(R.id.sa_days_anniversary)
    SeekArc saDaysAnniversary;
    @BindView(R.id.sa_hours_anniversary)
    SeekArc saHoursAnniversary;
    @BindView(R.id.tv_hours_anniversary)
    TextView tvHoursAnniversary;
    @BindView(R.id.tv_hours)
    TextView tvHours;
    @BindView(R.id.tv_hours_percent)
    TextView tvHoursPercent;
    @BindView(R.id.tv_hours_date)
    TextView tvHoursDate;
    @BindView(R.id.sv_widgets)
    ScrollView svWidgets;
    @BindView(R.id.sa_mins_anniversary)
    SeekArc saMinsAnniversary;
    @BindView(R.id.tv_mins_anniversary)
    TextView tvMinsAnniversary;
    @BindView(R.id.tv_mins)
    TextView tvMins;
    @BindView(R.id.tv_mins_percent)
    TextView tvMinsPercent;
    @BindView(R.id.tv_mins_date)
    TextView tvMinsDate;
    private PrefUtils mUserPrefs;
    private FirebaseFirestore mFireDb;
    private long mBirthMillis;


    public MainFragment() {
        // Required empty public constructor
    }

    static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserPrefs = MainActivity.mUserPrefs;
        mFireDb = FirebaseFirestore.getInstance();
        setHasOptionsMenu(true);
//        if (getArguments() != null) {
//            mParam2 = getArguments().getString(ARG_PARAM);
//        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        startAnimations();
        setYearProgress(saYearProgress, tvYearProgress);

        loadUserInfoFromPrefs();
        setUpListeners();

        // if no preferences are set, user is logged off
        if (mUserPrefs.getString(MainActivity.KEY_USER_BIRTH_DATE).isEmpty()) {
            blinkAnim(ivProfileHolder);
            switchAgeWidgets(false);
            MainActivity.mIsLoggedIn = false;
        } else {
            setupAgeWidgets();
            MainActivity.mIsLoggedIn = true;
            getAllUsers();
        }

        return rootView;
    }

    private void startAnimations() {
        startAnimation(saYearProgress);
        startAnimation(saDaysAnniversary);
        startAnimation(saHoursAnniversary);
        startAnimation(saMinsAnniversary);
    }

    private void setYearProgress(SeekArc sa, TextView tv) {
        final int DAYS_YEAR = 365;
        final int ANGLE_MAX = 360;
        int currentDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int percentOfYear = 100 * currentDayOfYear / DAYS_YEAR;
        if (currentDayOfYear > ANGLE_MAX) {
            sa.setProgress(100);
        } else {
            sa.setProgress(currentDayOfYear * 100 / DAYS_YEAR);
        }
        String percentFinal =
                String.valueOf(percentOfYear) + getResources().getString(R.string.percent);
        tv.setText(percentFinal);
    }

    private void startAnimation(SeekArc sa) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setStartOffset(500);
        fadeIn.setDuration(750);
        fadeIn.setRepeatMode(Animation.REVERSE);
        sa.setAnimation(fadeIn);
    }

    // load user info from preferences
    private void loadUserInfoFromPrefs() {
        if (mUserPrefs != null) {
            // load user prefs if they exist
            if (!mUserPrefs.getString(MainActivity.KEY_USER_BIRTH_DATE).isEmpty())
                tvProfileAge.setText(getUserAge(mUserPrefs.getString(MainActivity.KEY_USER_BIRTH_DATE)));
            else {
                tvProfileAge.setText(getString(R.string.default_age));
            }
            if (!mUserPrefs.getString(MainActivity.KEY_USER_NAME).isEmpty())
                tvProfileName.setText(mUserPrefs.getString(MainActivity.KEY_USER_NAME));
            else {
                tvProfileName.setText(getString(R.string.no_name));
            }
            if (!mUserPrefs.getString(MainActivity.KEY_USER_PICTURE).isEmpty())
                loadUserPicture(mUserPrefs.getString(MainActivity.KEY_USER_PICTURE));
            else {
                loadUserPicture(String.valueOf(R.drawable.account_circle_holder));
            }

            // show facebook login button only if user info is empty
            if (mUserPrefs.getString(MainActivity.KEY_USER_BIRTH_DATE).isEmpty() &&
                    mUserPrefs.getString(MainActivity.KEY_USER_NAME).isEmpty() &&
                    mUserPrefs.getString(MainActivity.KEY_USER_PICTURE).isEmpty()) {
                fbLoginButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setUpListeners() {
        // social profile sign in button
        ivProfileHolder.setOnClickListener(view -> fbLogInOff());
        // login with facebook button
        fbLoginButton.setOnClickListener(v -> {
            fbLoginButton.setReadPermissions(Arrays.asList(
                    "public_profile", "email", "user_birthday", "user_friends"));
            MainActivity.callbackManager = CallbackManager.Factory.create();
            // Callback registration
            LoginManager.getInstance().registerCallback(MainActivity.callbackManager, new FacebookCallback<LoginResult>() {
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
                                switchAgeWidgets(true);
                                MainActivity.mIsLoggedIn = true;
                                Logger.d(response);
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender,picture.type(large),birthday,friends");
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
        // share button
        ivShareHolder.setOnClickListener(view -> {
            File screenshotFile = getScreenShot();
            Uri screenshotUri = Uri.fromFile(screenshotFile);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Look at my age differently!");
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Share screenshot"));
        });
    }

    private void fbLogInOff() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        MainActivity.mIsLoggedIn = accessToken != null && !accessToken.isExpired();
        Logger.d("fbLogInOff: " + MainActivity.mIsLoggedIn);
        if (MainActivity.mIsLoggedIn) {
            LoginManager.getInstance().logOut();
            // show facebook login button
            fbLoginButton.setVisibility(View.VISIBLE);
            // erase firestore document with saved id
            String userName = mUserPrefs.getString(MainActivity.KEY_USER_NAME);
            mFireDb.collection("users")
                    .document(userName)
                    .delete()
                    .addOnSuccessListener(aVoid ->
                            Logger.d("User with name " + userName + " successfully deleted."))
                    .addOnFailureListener(e ->
                            Logger.d("Error deleting user with id " + userName));
            // erase & load empty user preferences
            mUserPrefs.clear();
            loadUserInfoFromPrefs();
            // enable blinking again (logger off)
            blinkAnim(ivProfileHolder);
            switchAgeWidgets(false);
            MainActivity.mIsLoggedIn = false;
        } else {
            fbLoginButton.performClick();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                if (MainActivity.mIsLoggedIn)
                    fbLogInOff();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                JSONArray userFriends = resObj.getJSONObject("friends").getJSONArray("data");
                Logger.d(userFriends);
                // load user picture
                loadUserPicture(userPhotoUrl);
                // set profile name into view
                tvProfileName.setText(userName);
                // find & set user age into view
                String userAge = getUserAge(userBirthDate);
                tvProfileAge.setText(userAge);
                // save set user info to prefs & firestore
                saveUserInfoToPrefs(userName, userBirthDate, userPhotoUrl);
                saveUserInfoToFirestore(userName, userBirthDate, userPhotoUrl);
                // update widgets data with from saved birth date
                setupAgeWidgets();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtils.showShort("Failed to login. Try again");
        }
    }

    // save info to preferences
    private void saveUserInfoToPrefs(String name, String birthDate, String pictureUrl) {
        mUserPrefs.clear();
        if (tvProfileAge.getText() != getString(R.string.default_age)) {
            if (mUserPrefs.getString(MainActivity.KEY_USER_BIRTH_DATE).isEmpty())
                mUserPrefs.putString(MainActivity.KEY_USER_BIRTH_DATE, birthDate);
        }
        if (tvProfileName.getText() != getString(R.string.no_name)) {
            if (mUserPrefs.getString(MainActivity.KEY_USER_NAME).isEmpty())
                mUserPrefs.putString(MainActivity.KEY_USER_NAME, name);
        }
        if (ivProfileHolder.getDrawable() != null) {
            if (mUserPrefs.getString(MainActivity.KEY_USER_PICTURE).isEmpty())
                mUserPrefs.putString(MainActivity.KEY_USER_PICTURE, pictureUrl);
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


    private void saveUserInfoToFirestore(String userName, String userBirthDate, String userPhotoUrl) {
        // Create a new user
        Map<String, Object> user = new HashMap<>();
        user.put("username", userName);
        user.put("birthDate", userBirthDate);
        user.put("photoUrl", userPhotoUrl);

        // Add a new document with a generated ID
        mFireDb.collection("users")
                .document(userName)
                .set(user)
                .addOnSuccessListener(documentReference ->
                        Logger.d("DocumentSnapshot added with name: " + userName))
                .addOnFailureListener(e -> Logger.w("Error adding document", e));
    }

    private String getUserAge(String userBirthDate) { // 01/31/1980 format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        mBirthMillis = TimeUtils.string2Millis(userBirthDate, dateFormat);
        Period period = new Period(mBirthMillis, MainActivity.mNowMillis);
        return getString(R.string.age_holder, period.getYears(), period.getMonths(), period.getDays());
    }

    private void setupAgeWidgets() {
        // set global birthMillis
        getUserAge(mUserPrefs.getString(MainActivity.KEY_USER_BIRTH_DATE));
        // find ms interval between birth & now
        Duration msDuration = new Duration(mBirthMillis, MainActivity.mNowMillis);
        int daysAge = (int) msDuration.getStandardDays();
        int hoursAge = (int) msDuration.getStandardHours();
        int minsAge = (int) msDuration.getStandardMinutes();
        setupDaysWidget(daysAge);
        setupHoursWidget(hoursAge);
        setupMinsWidget(minsAge);
    }

    private void setupMinsWidget(long minsAge) {
        int annivEvery = 100000;
        // set current mins age
        tvMins.setText(NumberFormat.getInstance().format(minsAge));
        // find next anniversary & how far it is
        int nextMinsAnniv = (int) MathUtils.roundUp(minsAge, annivEvery);
        tvMinsAnniversary.setText(NumberFormat.getInstance().format(nextMinsAnniv));
        int minsToNextAnniv = nextMinsAnniv - (int) minsAge;
        // find percentage between two anniveraries
        int percentOfMinsAnniv = 100 - (minsToNextAnniv * 100 / annivEvery);
        tvMinsPercent.setText(getString(R.string.percent_holder, percentOfMinsAnniv));
        saMinsAnniversary.setProgress(percentOfMinsAnniv);
        // find next anniversary date in format month.day hour:mins
        MutableDateTime annivDateTime = new MutableDateTime(MainActivity.mNowMillis);
        annivDateTime.addMinutes(minsToNextAnniv);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd HH:mm", Locale.getDefault());
        tvMinsDate.setText(TimeUtils.millis2String(annivDateTime.getMillis(), dateFormat));
    }

    private void setupHoursWidget(long hoursAge) {
        int annivEvery = 1000;
        // set current hours age
        tvHours.setText(NumberFormat.getInstance().format(hoursAge));
        // find next anniversary & how far it is
        int nextHoursAnniv = (int) MathUtils.roundUp(hoursAge, annivEvery);
        tvHoursAnniversary.setText(NumberFormat.getInstance().format(nextHoursAnniv));
        int hoursToNextAnniv = nextHoursAnniv - (int) hoursAge;
        // find percentage between two anniveraries
        int percentOfHoursAnniv = 100 - (hoursToNextAnniv * 100 / annivEvery);
        tvHoursPercent.setText(getString(R.string.percent_holder, percentOfHoursAnniv));
        saHoursAnniversary.setProgress(percentOfHoursAnniv);
        // find next anniversary date in format month.day hour:00
        MutableDateTime annivDateTime = new MutableDateTime(MainActivity.mNowMillis);
        annivDateTime.addHours(hoursToNextAnniv);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd HH:00", Locale.getDefault());
        tvHoursDate.setText(TimeUtils.millis2String(annivDateTime.getMillis(), dateFormat));
    }

    private void setupDaysWidget(long daysAge) {
        int annivEvery = 100;
        // set current days age
        tvDays.setText(NumberFormat.getInstance().format(daysAge));
        // find next anniversary & how far it is
        int nextDaysAnniv = (int) MathUtils.roundUp(daysAge, annivEvery);
        tvDaysAnniversary.setText(NumberFormat.getInstance().format(nextDaysAnniv));
        int daysToNextAnniv = nextDaysAnniv - (int) daysAge;
        // find percentage between two anniveraries
        int percentOfDayAnniv = annivEvery - daysToNextAnniv;
        tvDaysPercent.setText(getString(R.string.percent_holder, percentOfDayAnniv));
        saDaysAnniversary.setProgress(percentOfDayAnniv);
        // find next anniversary date in format month.day
        MutableDateTime annivDateTime = new MutableDateTime(MainActivity.mNowMillis);
        annivDateTime.addDays(daysToNextAnniv);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd", Locale.getDefault());
        tvDaysDate.setText(TimeUtils.millis2String(annivDateTime.getMillis(), dateFormat));
    }

    private void switchAgeWidgets(boolean show) {
//        View incDays = findViewById(R.id.inc_days);
        if (!show) {
            svWidgets.setVisibility(View.GONE);
        } else {
            svWidgets.setVisibility(View.VISIBLE);
        }
    }

    private File getScreenShot() {
        Date now = new Date();
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            if (isExternalStorageWritable()) {
                File externalPicDir = Objects.requireNonNull(getActivity()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (externalPicDir != null) {
                    boolean pastScrErased = erasePastScreenshots(externalPicDir);
                }
                // image naming and path to include sd card appending name
                String mPath = externalPicDir + "/" + now + "-ageversary-screenshot.jpg";

                // create bitmap screen capture
                View v1 = Objects.requireNonNull(getActivity()).getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);

                File imageFile = new File(mPath);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                int quality = 85;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();

                return imageFile;
            } else {
                ToastUtils.showShort("No external storage for screenshot found");
                return null;
            }
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
        return null;
    }

    private boolean erasePastScreenshots(File screenshotDir) {
        File file = new File(screenshotDir.toString());
        String[] scrFiles;
        scrFiles = file.list();
        // if more than one screenshot has been found, erase all
        if (scrFiles.length > 1) {
            for (String scrFile1 : scrFiles) {
                File scrFile = new File(file, scrFile1);
                boolean deleted = scrFile.delete();
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void getAllUsers() {
        mFireDb.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Logger.d("Error getting documents: ", task.getException());
                    }
                })
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = queryDocumentSnapshots.toObjects(User.class);
                    MainActivity.mUserList.clear();
                    MainActivity.mUserList.addAll(users);
                    Logger.d(MainActivity.mUserList);
                })
                .addOnFailureListener(e -> Logger.e(e.getMessage()));
    }

    private void blinkAnim(View view) {
        Animation anim = new AlphaAnimation(0.7f, 1.0f);
        anim.setDuration(1000); // You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        view.startAnimation(anim);
    }

}
