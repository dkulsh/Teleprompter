package com.example.deep.teleprompter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private int scrollDelay = 100;
    private int pixelDrag = 1; // This should depend on the alphabet size.
//    Higher pixel drag gives a hitch type feeling to the scroll
    private int textSize = 40;

    private static final String LOGTAG = "Teleprompter app";
    private static final int DEFAULT_SPEED = 10;
    private static boolean isPlaying = false;
    private static boolean isScrollDown = true;

    private Camera camera;
    FrameLayout previewLayout;
    CameraPreview cameraPreview;
    private ScrollView scrollView;
    private TextView textView;
    private Handler handler;
//    SeekBar seekBar;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
//                scrollView.fullScroll(View.FOCUS_DOWN);
//                scrollView.smoothScrollTo(0, scrollView.getBottom());
            scrollView.smoothScrollBy(0, pixelDrag);
            handler.postDelayed(runnable, scrollDelay);
        }
    };

    ImageButton slowScrollButton;
    ImageButton fastScrollButton;
    ImageButton playPauseButton;
    ImageButton directionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        textView = (TextView) findViewById(R.id.scrollText);
//        textView.setText(Html.fromHtml(this.getString(R.string.scrollText)));

//        setTextSize();
//        textView.setScroller();
//        textView.setMovementMethod(new ScrollingMovementMethod());

        scrollView = (ScrollView) findViewById(R.id.teleScroll);
        handler = new Handler();

        cameraPreview = new CameraPreview(this);
        previewLayout = (FrameLayout) findViewById(R.id.camera_preview);

        setupScrollControls();

        Log.v(LOGTAG, getIntent().getStringExtra(FirstScreen.TELEPROMPT_TEXT));
    }

    private void setupScrollControls(){

        slowScrollButton = (ImageButton) findViewById(R.id.slowScroll);
        fastScrollButton = (ImageButton) findViewById(R.id.fastScroll);
        playPauseButton = (ImageButton) findViewById(R.id.playButton);
        directionButton = (ImageButton) findViewById(R.id.directionButton);
//        seekBar = (SeekBar) findViewById(R.id.changeScrollSpeed);

        slowScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDelay = scrollDelay + 10;
//                seekBar.setProgress(seekBar.getProgress() - 1);
            }
        });

        fastScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDelay = scrollDelay - 10;
//                seekBar.setProgress(seekBar.getProgress() + 1);
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    handler.removeCallbacks(runnable);
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                    isPlaying = false;
                } else {
                    handler.postDelayed(runnable, scrollDelay);
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    isPlaying = true;
                }
            }
        });

        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScrollDown) {

                    Log.v(LOGTAG, "Setting scroll to up");
                    pixelDrag = -1;
                    directionButton.setImageResource(R.drawable.down);
                    isScrollDown = false;
                } else {
                    Log.v(LOGTAG, "Setting scroll to down");
                    pixelDrag = 1;
                    directionButton.setImageResource(R.drawable.up);
                    isScrollDown = true;
                }
            }
        });

/*        seekBar.setMax(20);
        seekBar.setProgress(DEFAULT_SPEED); // Starting default progress from 10
        seekBar.setClickable(false);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTextProperties();
    }

    private void setTextProperties(){

        boolean mirrorMode = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(this.getString(R.string.mirror_mode_key), false);

        View view = findViewById(R.id.scrollText);

        if(mirrorMode) {
            scrollView.removeAllViews();
            textView = new MirrorTextview(this);
            String promptText = getIntent().getStringExtra(FirstScreen.TELEPROMPT_TEXT);
            textView.setText("" + promptText);

            scrollView.addView(textView);
//            textView = (MirrorTextview) findViewById(R.id.scrollText);
        }else {
            scrollView.removeAllViews();
            textView = new TextView(this);
            String promptText = getIntent().getStringExtra(FirstScreen.TELEPROMPT_TEXT);
            textView.setText( "" + promptText);

            scrollView.addView(textView);
//            textView = (TextView) findViewById(R.id.scrollText);
        }

        textSize = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(this.getString(R.string.text_size_key), SettingsActivity.DEFAULT_TEXT_SIZE);
        textView.setTextSize(textSize);

        String color = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(this.getString(R.string.choose_color_key), "-1" );
        Log.v(LOGTAG, "Value of color is : " + color);
        color = TextUtils.isDigitsOnly(color) ? color: "-1";

        int colorInt = Color.BLACK;
        switch (Integer.parseInt(color)){

            case -1: colorInt = Color.BLACK; break;
            case 0: colorInt = Color.BLUE; break;
            case 1: colorInt = Color.CYAN; break;
            case 2: colorInt = Color.GREEN; break;
            case 3: colorInt = Color.YELLOW; break;
            case 4: colorInt = Color.RED; break;
            default: colorInt = Color.BLACK;
        }

        textView.setTextColor(colorInt);

    }

    private void getCamera(){

        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){

            int cameraNumber = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            for(int i = 0; i < cameraNumber; i++){

                Camera.getCameraInfo(i, cameraInfo);
                if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    try { camera = Camera.open(i); }
                    catch (Exception e) { Toast.makeText(this, R.string.camera_open_error, Toast.LENGTH_SHORT).show(); }
                }/*else {
                    Toast.makeText(this, R.string.no_camera, Toast.LENGTH_SHORT).show();
                }*/
            }
//            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
//            manager.openCamera();

        } else {
            Toast.makeText(this, R.string.no_camera, Toast.LENGTH_SHORT).show();
        }
    }

    private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private SurfaceHolder holder;

        public CameraPreview(Context context) {

            super(context);
            holder = getHolder();
            holder.addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                camera.stopPreview();
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseCamera();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

/*
        MenuItem menuItemScroll = menu.findItem(R.id.scrollControl);
        LinearLayout linearLayoutScroll = (LinearLayout) MenuItemCompat.getActionView(menuItemScroll);
*/

//        MenuItem menuItemSize = menu.findItem(R.id.sizeControl);
//        LinearLayout linearLayoutPlay = (LinearLayout) MenuItemCompat.getActionView(menuItemSize);

/*
        slowScrollButton = (ImageButton) linearLayoutScroll.findViewById(R.id.slowScroll);
        fastScrollButton = (ImageButton) linearLayoutScroll.findViewById(R.id.fastScroll);
        playPauseButton = (ImageButton) linearLayoutScroll.findViewById(R.id.playButton);
        directionButton = (ImageButton) linearLayoutScroll.findViewById(R.id.directionButton);
        seekBar = (SeekBar) linearLayoutScroll.findViewById(R.id.changeScrollSpeed);

        slowScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDelay = scrollDelay + 10;
                seekBar.setProgress(seekBar.getProgress() - 1);
            }
        });

        fastScrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDelay = scrollDelay - 10;
                seekBar.setProgress(seekBar.getProgress() + 1);
            }
        });

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    handler.removeCallbacks(runnable);
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                    isPlaying = false;
                } else {
                    handler.postDelayed(runnable, scrollDelay);
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    isPlaying = true;
                }
            }
        });

        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isScrollDown) {

                    Log.v(LOGTAG, "Setting scroll to up");
                    pixelDrag = -1;
                    directionButton.setImageResource(R.drawable.down);
                    isScrollDown = false;
                } else {
                    Log.v(LOGTAG, "Setting scroll to down");
                    pixelDrag = 1;
                    directionButton.setImageResource(R.drawable.up);
                    isScrollDown = true;
                }
            }
        });

        seekBar.setMax(20);
        seekBar.setProgress(DEFAULT_SPEED); // Starting default progress from 10
        seekBar.setClickable(false);
*/

//        return true;
        return super.onCreateOptionsMenu(menu);
    }

    private void releaseCamera(){

        if(camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
//            return true;
        } else if(id == R.id.video){

            if(camera == null) {
                getCamera();
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                switch (rotation) {
                    case Surface.ROTATION_0:
                        camera.setDisplayOrientation(90);
                        break;
                    case Surface.ROTATION_90:
                        Log.v(LOGTAG, "Rotation is 90");
                        break;
                    case Surface.ROTATION_180:
                        Log.v(LOGTAG, "Rotation is 180");
                        break;
                    case Surface.ROTATION_270:
                        camera.setDisplayOrientation(180);
                        break;
                }

                previewLayout.addView(cameraPreview);
            } else {

                releaseCamera();
                previewLayout.removeAllViews();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if(camera != null){
//            camera.release();
//            camera = null;
//        }
    }
}
