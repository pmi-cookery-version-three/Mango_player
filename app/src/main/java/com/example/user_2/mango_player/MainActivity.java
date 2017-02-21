package com.example.user_2.mango_player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.DecimalFormat;
import java.util.Random;

import io.github.nikhilbhutani.analyzer.DataAnalyzer;

public class MainActivity extends AppCompatActivity {

    static Activity activity;

    int isShow = 0;
    // Screen background photo
    static ImageView background;

    //Strings for showing song data and detecting old album photo
    static String artist, track, album, albumOld;

    static TextView title_tv, artist_tv, track_tv, data_tv;
    static CircularSeekBar volumeChanger;

    // Animation on bottom of the screen when stream is loaded
    static AVLoadingIndicatorView playing_animation;

    // Stram loading animation on the center of screen
    static AVLoadingIndicatorView loading_animation;

    // Button for start/stop playing audio
    static ImageButton control_button;

    // Boolean for check if play/pause button is activated
    static boolean controlIsActivated = false;
    //static DataAnalyzer dataAnalyzer;
    static ApplicationInfo app;

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        try {
            app = this.getPackageManager().getApplicationInfo("mgc.rockabillyradio", 0);

        } catch (PackageManager.NameNotFoundException e) {
            Toast toast = Toast.makeText(this, "error in getting icon", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }
        initialise();
        setCustomFont();
        startListenVolume();
        new GetTrackInfo().execute();
        startRefreshing();
        startCounting();
    }

    // Initialise all views, animations and set click listeners
    void initialise() {
        background = (ImageView) findViewById(R.id.bckg);
        title_tv = (TextView) findViewById(R.id.title_tv);
        track_tv = (TextView) findViewById(R.id.track_tv);
        artist_tv = (TextView) findViewById(R.id.artist_tv);
        volumeChanger = (CircularSeekBar) findViewById(R.id.circularSeekBar1);
        playing_animation = (AVLoadingIndicatorView) findViewById(R.id.playing_anim);
        playing_animation.setVisibility(View.GONE);
        loading_animation = (AVLoadingIndicatorView) findViewById(R.id.load_animation);
        control_button = (ImageButton) findViewById(R.id.control_button);
        control_button.setOnClickListener(controlButtonListener);
        data_tv = (TextView) findViewById(R.id.data_tv);
        data_tv.setVisibility(View.VISIBLE);
    }

    // Function for set custom font to title text view in toolbar
    void setCustomFont() {
        Typeface tf = Typeface.createFromAsset(this.getAssets(), "radio.ttf");
        title_tv.setTypeface(tf);
    }

    // Function for listen and change volume from seek bar to player
    void startListenVolume() {
        volumeChanger.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                Player.setVolume((100 - circularSeekBar.getProgress()) / 100f);
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }

        });
    }

    // Function to set track and artist names to text views and change background photo
    public static void setSongData() {
        artist_tv.setText(artist);
        track_tv.setText(track);
        if (album != null) {
            if (album.length() < Const.SERVER_PHOTOS_COUNT) {
                Random rn = new Random();
                Bitmap bitmap = ((BitmapDrawable) background.getDrawable()).getBitmap();
                Drawable d = new BitmapDrawable(activity.getResources(), bitmap);
                Picasso.with(activity).load(Const.SERVER_PHOTO_PATH + (rn.nextInt(Const.SERVER_PHOTOS_COUNT) + 1) + Const.PHOTO_TYPE).placeholder(d).into(background);
                //    Picasso.with(activity).load(Const.SERVER_PHOTO_PATH + 14 + Const.PHOTO_TYPE).placeholder(d).into(background);

            } else {
                Bitmap bitmap = ((BitmapDrawable) background.getDrawable()).getBitmap();
                Drawable d = new BitmapDrawable(activity.getResources(), bitmap);
                albumOld = album;
                Picasso.with(activity).load(album).placeholder(d).into(background);
            }
        }
    }

    // Service for background audio playing
    public void startPlayerService() {
        Intent serviceIntent = new Intent(MainActivity.this, NotificationService.class);
        serviceIntent.setAction(Const.ACTION.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    // Vibrate when click on control button
    public void vibrate() {
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(Const.VIBRATE_TIME);
    }

    public void startRefreshing()
    {
        DataAnalyzer dataAnalyzer = new DataAnalyzer(this);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(Const.PHOTO_LOAD_REFRESH_TIME);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new GetTrackInfo().execute();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }


    public void startCounting()
    {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(dataAnalyzer!=null) {
                                    Log.e("DATA USAGE", convertToStringRepresentation(Long.valueOf(MainActivity.dataAnalyzer.getReceivedData(MainActivity.app))));
                                    Log.e("DATA USAGE", "------");
                                    MainActivity.data_tv.setText(convertToStringRepresentation(Long.valueOf(MainActivity.dataAnalyzer.getReceivedData(MainActivity.app))));
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }

    View.OnClickListener controlButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (controlIsActivated == false) {
                startPlayerService();
                control_button.setImageResource(R.drawable.pause);
                playing_animation.setVisibility(View.GONE);
                loading_animation.setVisibility(View.VISIBLE);
                control_button.setVisibility(View.GONE);
                controlIsActivated = true;
                vibrate();
            } else {
                Player.stop();
                control_button.setImageResource(R.drawable.play);
                playing_animation.setVisibility(View.GONE);
                loading_animation.setVisibility(View.VISIBLE);
                control_button.setVisibility(View.VISIBLE);
                controlIsActivated = false;
                vibrate();
            }
        }
    };

    public static String convertToStringRepresentation(final long value){
        final long[] dividers = new long[] { T, G, M, K, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
        if(value < 1)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for(int i = 0; i < dividers.length; i++){
            final long divider = dividers[i];
            if(value >= divider){
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value,
                                 final long divider,
                                 final String unit){
        final double result =
                divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void enterReveal() {
        isShow = 1;
        // previously invisible view
        final CardView myView = (CardView) findViewById(R.id.analyzer);
        myView.setVisibility(View.VISIBLE);
        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() - 100;
        int cy = -100;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight())*2 ;

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius).setDuration(700);

        // make the view visible and start the animation
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void exitReveal() {
        // previously visible view
        isShow = 0;
        final CardView myView = (CardView) findViewById(R.id.analyzer);

        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() - 100;
        int cy = -100;

        // get the initial radius for the clipping circle
        int initialRadius = myView.getWidth();

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0).setDuration(700);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
    }

    @Override
    public void onBackPressed() {
        Player.stop();
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void enter(View view) {
//        if(isShow == 1)
//        {
//            exitReveal();
//        }
//        else enterReveal();
    }
}
