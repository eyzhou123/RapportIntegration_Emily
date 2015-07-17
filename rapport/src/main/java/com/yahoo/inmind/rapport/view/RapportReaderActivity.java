package com.yahoo.inmind.rapport.view;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.yahoo.inmind.control.reader.ReaderController;
import com.yahoo.inmind.control.util.Constants;
import com.yahoo.inmind.rapport.R;
import com.yahoo.inmind.view.reader.ReaderMainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;


public class RapportReaderActivity extends ReaderMainActivity implements DataListener {

    private static final int MAX_BUFFER = 15;
    public static CameraPreview mPreview;
    public static CameraManager mCameraManager;
    public static boolean android_is_streaming = true;
    //private static String server_ip = "128.237.221.118";
//	public static String server_ip = "10.0.0.8";
    public static String server_ip = "128.237.208.154";
    public static String path = "/Users/eyzhou/Desktop/";
    static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
    static Context context;
    static Button news_button;
    static Button assistant_button;
    static Button news_mode_button;
    static Button stream_button;
    private static UnityPlayer mUnityPlayer;
    private static String msg = null;
    private static String timestamp = null;
    private static Date date;
    AnimationDrawable frameAnimation;
    //private boolean news_mode_on = false;
    private boolean assistant_button_clicked = false;
    private boolean news_button_clicked = false;
    private boolean news_mode_clicked = false;
    private boolean stream_button_clicked = false;
    private boolean mIsOn = true;
    private SocketClientAndroid mThread;
    private Button mButton;
    private int mPort = 8880;
    private boolean on_start_page = true;
    private EditText newIP;
    private LinkedList<Bitmap> mQueue = new LinkedList<Bitmap>();
    private Bitmap mLastFrame;
    private ImageView imageView;
    private Handler handler;
    private SocketClient socketclient;
    private AudioClient audioclient;
    private ReaderController readerController;


    // Handle switching views from the slide out drawer (called from RapportDrawerManager.java)
    public static void clicked_news_view() {
        Toast.makeText(context, "NEWS VIEW",
                Toast.LENGTH_SHORT).show();
        date = new Date();
        timestamp = sdf.format(date);
        msg = "Pressed NEWS VIEW at " + timestamp;
        Log.d("USER DATA", msg);

        news_button.performClick();
    }

    public static void clicked_assistant_view() {
        Toast.makeText(context, "ASSISTANT VIEW",
                Toast.LENGTH_SHORT).show();
        date = new Date();
        timestamp = sdf.format(date);
        msg = "Pressed ASSISTANT VIEW at " + timestamp;
        Log.d("USER DATA", msg);

        assistant_button.performClick();
    }

    public static void clicked_both_view() {
        Toast.makeText(context, "BOTH VIEW",
                Toast.LENGTH_SHORT).show();
        date = new Date();
        timestamp = sdf.format(date);
        msg = "Pressed BOTH VIEW at " + timestamp;
        Log.d("USER DATA", msg);

        news_mode_button.performClick();
    }

    public static void woz_view() {
        Toast.makeText(context, "WOZ VIEW",
                Toast.LENGTH_SHORT).show();
        date = new Date();
        timestamp = sdf.format(date);
        msg = "Pressed WOZ VIEW at " + timestamp;
        Log.d("USER DATA", msg);

        stream_button.performClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        //layout activity_browser
        ReaderController.news_browser_layout_id = R.layout.rapport_activity_browser;
        ReaderController.news_pB1 = R.id.rapport_pB1;
        ReaderController.news_wv = R.id.rapport_wv;
        // layout drawer_list_item
        ReaderController.news_drawer_list_item_layout_id =  R.layout.rapport_drawer_list_item;
        ReaderController.news_drawer_icon =  R.id.rapport_drawer_icon;
        ReaderController.text1 = R.id.rapport_text1;
        // layout fragment_news_flipview and fragment_news_listview
        ReaderController.news_fragment_flipview_layout_id = R.layout.rapport_fragment_news_flipview;
        ReaderController.news_fragment_listview = R.layout.rapport_fragment_news_listview;
        ReaderController.news_swipe_container = R.id.rapport_swipe_container;
        ReaderController.news_newslist = R.id.rapport_newslist;
        // layout list_item_flat and list_item
        ReaderController.landscape_layout = R.layout.rapport_list_item_flat;
        ReaderController.portrait_layout = R.layout.rapport_news_list_item;
        //like
        //dislike
        //comments
        //recommendation1
        //recommendation2

        // layout news_main
        ReaderController.news_main = R.layout.rapport_news_main;
        ReaderController.news_content_frame = R.id.rapport_news_content_frame;

        //layout outside_layout
        ReaderController.news_drawer_layout = R.id.rapport_news_drawer_layout;
        ReaderController.news_left_drawer = R.id.rapport_news_left_drawer;



        int layout_id = R.layout.rapport_news_outside_layout;
        Bundle instanceCopy = savedInstanceState;
        if (instanceCopy == null) {
            instanceCopy = new Bundle();
        }
        instanceCopy.putInt( Constants.BUNDLE_MAIN_LAYOUT_ID, layout_id);
        instanceCopy.putBoolean( Constants.BUNDLE_RESET_SAVED_INSTANCE, true );

        super.onCreate(instanceCopy);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.rapport_test_layout, new PlaceholderFragment())
                    .commit();
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int height = size.y;
        final int button_margin = 10;
        final int news_height = height/2;
        final int assistant_height = height - news_height - 200;
        final int small_assistant_height = height/3;
        final int small_assistant_width = width/4;
        final int assistant_button_width = width/3 - button_margin;
        final int news_button_width = width/3 - button_margin;
        final int news_mode_button_width = width/3 - button_margin;
        final int button_panel_height = height/16;

        //TODO: ojrl
        mCameraManager = new CameraManager(this);
	    mPreview = new CameraPreview(this, mCameraManager.getCamera());
	    FrameLayout preview = (FrameLayout) findViewById(R.id.rapport_camera_preview);
        preview.addView(mPreview);

        // EZ: Created an "outside" layout that contains all the other layouts, so that
        // it can also have a sliding drawer
        DrawerLayout layoutOutside = (DrawerLayout) findViewById( ReaderController.news_drawer_layout );
        final FrameLayout layoutFrame = (FrameLayout) findViewById( R.id.rapport_news_layout_frame );
        final RelativeLayout layoutRel = (RelativeLayout) findViewById(R.id.rapport_news_layout_rel);
        final LinearLayout layoutMain = (LinearLayout) findViewById(R.id.rapport_news_layout_main);
        layoutMain.setBackgroundColor(Color.parseColor("#000000"));
        layoutMain.setOrientation(LinearLayout.VERTICAL);
        setContentView(layoutOutside);

        // Button panel holds 'tabs' for switching views
        // NOTE: this is currently not visible (it is from an older version)
        // It is still here to preserve the onClickListeners, and in case we wish to use it again
        final LinearLayout button_panel = new LinearLayout(this);
        button_panel.setOrientation(LinearLayout.HORIZONTAL);
        button_panel.setBackgroundColor(Color.parseColor("#000000"));

        final LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Assistant button changes view to full screen assistant
        assistant_button = new Button(this);
        assistant_button.setTextSize(12);
        assistant_button.setTextColor(Color.parseColor("#000000"));
        assistant_button.setBackgroundColor(Color.parseColor("#0ABEF5"));
        assistant_button.setBackgroundResource(R.drawable.assistant_button);
        LinearLayout.LayoutParams assistant_button_params = new LinearLayout.LayoutParams(assistant_button_width,
                button_panel_height);
        assistant_button_params.setMargins(5, 5, 0, 5);
        assistant_button.setLayoutParams(assistant_button_params);

        button_panel.addView(assistant_button, assistant_button_params);

        // News button changes view to full screen news app
        news_button = new Button(this);
        news_button.setTextSize(12);
        news_button.setTextColor(Color.parseColor("#000000"));
        news_button.setBackgroundColor(Color.parseColor("#0ABEF5"));
        news_button.setBackgroundResource(R.drawable.news_button);
        LinearLayout.LayoutParams news_button_params = new LinearLayout.LayoutParams(news_button_width,
                button_panel_height);
        news_button_params.setMargins(10, 5, 0, 5);
        news_button.setLayoutParams(news_button_params);
        button_panel.addView(news_button, news_button_params);

        // News mode changes view to assistant on top of news
        news_mode_button = new Button(this);
        news_mode_button.setTextSize(12);
        news_mode_button.setTextColor(Color.parseColor("#000000"));
        news_mode_button.setBackgroundColor(Color.parseColor("#0ABEF5"));
        news_mode_button.setBackgroundResource(R.drawable.news_mode_button);
        LinearLayout.LayoutParams news_mode_button_params = new LinearLayout.LayoutParams(
                news_mode_button_width, button_panel_height);
        news_mode_button_params.setMargins(10, 5, 10, 5);
        news_mode_button.setLayoutParams(news_mode_button_params);
        button_panel.addView(news_mode_button, news_mode_button_params);

        // Camera rapport_test
        stream_button = new Button(this);
        stream_button.setTextSize(12);
        stream_button.setTextColor(Color.parseColor("#000000"));
        stream_button.setBackgroundColor(Color.parseColor("#0ABEF5"));
        LinearLayout.LayoutParams stream_button_params = new LinearLayout.LayoutParams(
                news_mode_button_width, button_panel_height);
        stream_button_params.setMargins(10, 5, 10, 5);
        stream_button.setLayoutParams(stream_button_params);
        button_panel.addView(stream_button, stream_button_params);


        // Add news app View
        final LinearLayout layoutLeft = (LinearLayout) inflate.inflate(
                R.layout.rapport_news_main, null);
        LinearLayout.LayoutParams layout_left_params = new LinearLayout.LayoutParams(
                width, news_height);
        layoutMain.addView(layoutLeft, layout_left_params);

        imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        LinearLayout.LayoutParams image_params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, 400);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        DrawerLayout.LayoutParams drwParam= new DrawerLayout.LayoutParams(
                DrawerLayout.LayoutParams.WRAP_CONTENT,
                DrawerLayout.LayoutParams.WRAP_CONTENT);

        //TODO: ojrl
        mUnityPlayer = new UnityPlayer(this);
        int glesMode = mUnityPlayer.getSettings().getInt("gles_mode", 1);
        mUnityPlayer.init(glesMode, false);


//        mUnityPlayer.UnitySendMessage("Brad", arg1, arg2);
//        mUnityPlayer.getView().setBackgroundResource(R.drawable.blue_border);


        final RelativeLayout layoutRight = (RelativeLayout) inflate.inflate(
                R.layout.rapport_fragment_main, null);
        //layoutRight.setId(R.id.layout_right);

        RelativeLayout.LayoutParams relParam = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);


        layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);

        // Add static mic button, layered on top with a frame layout
        final Button mic_button = new Button(this);
        mic_button.setBackgroundResource(R.drawable.mic_button_selector);
        RelativeLayout.LayoutParams mic_button_params = new RelativeLayout.LayoutParams(220, 150);
        mic_button.setLayoutParams(mic_button_params);
        mic_button_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutRel.addView(mic_button, mic_button_params);


        // The following are three onClickListeners for the three view tabs,
        // so that they are mutually exclusive buttons
        assistant_button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                ViewHelper.getInstance().getModelDistributions();
                assistant_button_clicked = true;
                assistant_button.setBackgroundResource(R.drawable.assistant_button_pressed);
                if (stream_button_clicked) {
                    stream_button_clicked = false;
                    closeSocketClient();
                    layoutMain.removeView(imageView);
                    layoutMain.removeView(layoutLeft);
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, news_height );
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                }
                if (news_button_clicked) {
                    news_button_clicked = false;
                    news_button.setBackgroundResource(R.drawable.news_button);
                    layoutMain.removeView(layoutLeft);
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, news_height );
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                }
                if (news_mode_clicked) {
                    news_mode_clicked = false;
                    news_mode_button.setBackgroundResource(R.drawable.news_mode_button);
                    layoutMain.removeView(layoutRight);
                    layoutMain.removeView(layoutLeft);
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, news_height);
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                }
                layoutLeft.setVisibility(View.GONE);
                layoutMain.removeView(layoutRight);
                layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, height - 200);

            }
        });

        news_button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                news_button_clicked = true;
                news_button.setBackgroundResource(R.drawable.news_button_pressed);
                if (stream_button_clicked) {
                    stream_button_clicked = false;
                    closeSocketClient();
                    layoutMain.removeView(imageView);
                    layoutMain.removeView(layoutLeft);
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, news_height );
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                }
                if (assistant_button_clicked) {
                    assistant_button_clicked = false;
                    assistant_button.setBackgroundResource(R.drawable.assistant_button);
                    layoutMain.removeView(layoutRight);
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                    layoutLeft.setVisibility(View.VISIBLE);
                }
                if (news_mode_clicked) {
                    news_mode_clicked = false;
                    news_mode_button.setBackgroundResource(R.drawable.news_mode_button);
                    layoutMain.removeView(layoutRight);
                    layoutMain.removeView(layoutLeft);
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, news_height );
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                }
                layoutMain.removeView(layoutRight);
                layoutMain.removeView(layoutLeft);
                layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, height);

            }
        });


        news_mode_button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                news_mode_clicked = true;
                news_mode_button.setBackgroundResource(R.drawable.news_mode_button_pressed);
                if (stream_button_clicked) {
                    stream_button_clicked = false;
                    closeSocketClient();
                    layoutMain.removeView(imageView);
                    layoutMain.removeView(layoutLeft);
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, news_height );
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                }
                if (assistant_button_clicked) {
                    assistant_button_clicked = false;
                    assistant_button.setBackgroundResource(R.drawable.assistant_button);
                    layoutMain.removeView(layoutRight);
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height );
                    layoutLeft.setVisibility(View.VISIBLE);
                }
                if (news_button_clicked) {
                    news_button_clicked = false;
                    news_button.setBackgroundResource(R.drawable.news_button);
                    layoutMain.removeView(layoutLeft);
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, news_height );
                    layoutMain.addView(layoutRight,DrawerLayout.LayoutParams.MATCH_PARENT, assistant_height);
                }
                layoutMain.removeView(layoutRight);
                layoutMain.removeView(layoutLeft);

                LinearLayout.LayoutParams small_assistant_params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, small_assistant_height);
                layoutRight.setLayoutParams(small_assistant_params);

                layoutMain.addView(layoutRight, small_assistant_params);
                layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, height - small_assistant_height - 200);

            }
        });

        // Woz mode
        stream_button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
//            	if (stream_button_clicked) {
//            		layoutMain.removeView(imageView);
//            		layoutMain.removeView(layoutLeft);
//            	}
                if (assistant_button_clicked) {
                    assistant_button_clicked = false;
                    assistant_button.setBackgroundResource(R.drawable.assistant_button);
                    layoutMain.removeView(layoutRight);
                    layoutLeft.setVisibility(View.VISIBLE);
                    layoutMain.removeView(layoutLeft);
                }
                if (news_button_clicked) {
                    news_button_clicked = false;
                    news_button.setBackgroundResource(R.drawable.news_button);
                    layoutMain.removeView(layoutLeft);
                }
                if (news_mode_clicked) {
                    news_mode_clicked = false;
                    news_mode_button.setBackgroundResource(R.drawable.news_mode_button);
                    layoutMain.removeView(layoutRight);
                    layoutMain.removeView(layoutLeft);
                }

                LinearLayout.LayoutParams image_params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (!stream_button_clicked) {
                    stream_button_clicked = true;
                    layoutMain.addView(imageView, image_params);
                    handler = new Handler();
                    openSocketClient();
                    socketclient.start();
                    openAudioClient();
                    audioclient.start();
                    Log.d("ERRORCHECK", "started clients");
                    layoutMain.addView(layoutLeft,DrawerLayout.LayoutParams.MATCH_PARENT, height - small_assistant_height);

                    mThread = new SocketClientAndroid();
                    mThread.start();
                    FrameLayout cam_view = (FrameLayout) findViewById(R.id.rapport_camera_preview);
                    //       		   cam_view.setVisibility(View.GONE);
                }
            }
        });

//        // Let the initial view be the assistant view
//        assistant_button.performClick();

//         //-----------------------------------------------------------------------------------

//            View rootView = inflate.inflate(R.layout.rapport_fragment_main,null);
//            FrameLayout layout = (FrameLayout) layoutRight.findViewById( R.id.rapport_frame_layout );
//            LayoutParams lp = new LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//           layout.addView(mUnityPlayer, 0, lp);
//            layoutMain.addView(layoutRight,FrameLayout.LayoutParams.MATCH_PARENT, 800 );
//            mUnityPlayer.resume();

  }

    private void openSocketClient() {
        socketclient = new SocketClient();
        socketclient.setOnDataListener(this);

    }

    private void openAudioClient() {
        audioclient = new AudioClient();
    }

    private void closeSocketClient() {
        audioclient.close();
        socketclient.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ERRORCHECK", "Closing android socket client");
		if (CameraPreview.mCamera != null) {
			CameraPreview.mCamera.stopPreview();
			CameraPreview.mCamera.release();
			CameraPreview.mCamera = null;
	    }
        if( mThread != null ) {
            mThread.close();
            mThread = null;
        }
    }

    // The following two functions are used for the woz view.
    // They update the imageview as images are grabbed on the server
    // side from the webcam
    private void paint() {
        //Draw the image bitmap into the canvas
        //tempCanvas.drawBitmap(mLastFrame, 0, 0, null);
        synchronized (mQueue) {
            if (mQueue.size() > 0) {
                mLastFrame = mQueue.poll();
            }
        }
        handler.post(new Runnable() {

            @Override
            public void run() {
                Log.d("Stream", "setting imageview");
                //mImageView.setImageDrawable(new BitmapDrawable(getResources(), mLastFrame));
                imageView.setImageBitmap(mLastFrame);
            }

        });
    }

    @Override
    public void onDirty(Bitmap bufferedImage) {
        synchronized(mQueue) {
            if (mQueue.size() == MAX_BUFFER) {
                mLastFrame = mQueue.poll();
            }
            mQueue.add(bufferedImage);
        }
//		Log.d("ERRORCHECK", "onDirty called");
        paint();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_main, menu);
        return true; //super.onCreateOptionsMenu(menu);
    }

    // Allow user to change IP address
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.edit_ip:
                // Opens a dialog box for input of new IP address
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.rapport_ip_dialog);
                dialog.setTitle("Edit IP Address");
                TextView text = (TextView) dialog.findViewById(R.id.rapport_enter_ip_text);
                text.setText("Enter new IP address: ");
                Button dialogButton = (Button) dialog.findViewById(R.id.rapport_enter_ip_OK);
                newIP = (EditText) dialog.findViewById(R.id.rapport_enter_ip);
                newIP.setText(server_ip, TextView.BufferType.EDITABLE);

                // if OK button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (newIP.getText().toString() != " ") {
                            server_ip = newIP.getText().toString();
                            Toast.makeText(getApplicationContext(), server_ip,
                                    Toast.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        //TODO: ojrl
        mUnityPlayer.windowFocusChanged(hasFocus);


        if (hasFocus) {
            // Starting the animation when in Focus
            //frameAnimation.start();
        } else {
            // Stoping the animation when not in Focus
//        	frameAnimation.stop();
//        	LinearLayout layoutMain = (LinearLayout)findViewById(2003);
//        	ImageView anim = (ImageView) findViewById(2004);
//        	layoutMain.removeView(anim);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: ojrl
        mCameraManager.onResume();
        mPreview.setCamera(mCameraManager.getCamera());
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.rapport_fragment_main, container, false);

            FrameLayout layout = (FrameLayout) rootView.findViewById( R.id.rapport_frame_layout);
            LayoutParams lp = new LayoutParams (LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

            //TODO:ojrl
            layout.addView(mUnityPlayer, 0, lp);
            mUnityPlayer.resume();

            return rootView;
        }
    }

}