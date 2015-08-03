package com.yahoo.inmind.rapport.view;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
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
import com.yahoo.inmind.view.handler.UIHandler;
import com.yahoo.inmind.view.reader.ReaderMainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;


public class RapportReaderActivity extends ReaderMainActivity implements DataListener, TextToSpeech.OnInitListener, RecognitionListener {

    private static final int MAX_BUFFER = 15;
    public static CameraPreview mPreview;
    public static CameraManager mCameraManager;
    public static boolean android_is_streaming = true;
    public static String server_ip = "128.237.208.154";
    public static String path = "/Users/eyzhou/Desktop/";
    static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
    static Context context;
    static Button news_button;
    static Button assistant_button;
    static Button both_mode_button;
    static Button stream_button;
    private static UnityPlayer mUnityPlayer;
    private static String msg = null;
    private static String timestamp = null;
    private static Date date;
    //private boolean both_mode_on = false;
    private boolean assistant_button_clicked = false;
    private boolean news_button_clicked = false;
    private boolean both_mode_clicked = false;
    private boolean stream_button_clicked = false;
    private boolean mIsOn = true;
    private SocketClientAndroid mThread;
    private AndroidAudioClient androidAudioSocket;
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

    private Socket socket_NLG;
    private volatile boolean stop_NLG_thread = false;

    //TTS object
    private TextToSpeech myTTS;
    //status check code
    private final int MY_DATA_CHECK_CODE = 0;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private SpeechRecognizer speechRecognizer;
    private int currentNewsId;
    private int newsItemNum;




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

        both_mode_button.performClick();
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

    class ClientThreadForNLG implements Runnable {
        @Override
        public void run() {
            Log.d("ERRORCHECK", "running NLG thread");
            try {
                socket_NLG = new Socket();
                socket_NLG.connect(new InetSocketAddress("128.237.208.154", 8008), 0);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket_NLG.getInputStream()));
                String s;

                // continuously try to read in requests
                while(!stop_NLG_thread){
                    if(socket_NLG.isConnected()){
                        s = in.readLine();
                        if (s != null) {
                            Log.d("ERRORCHECK", "Server said: " + s);
                            PrintWriter out = new PrintWriter(socket_NLG.getOutputStream(), true);
                            if ("REQUEST_PERSON".equals(s)) {
                                // The following should print out the distributions to logcat too
                                ViewHelper.getInstance().getModelDistributions();
                                // Distribution string is saved to public variable "result"
                                // Send this over the socket
                                out.println(ViewHelper.result);
                            }
                        }
                    } else{
                        break;
                    }
                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(this);
        //speechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
        System.out.println("speechRecognizer listener start");

        currentNewsId = 0;

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        //myTTS = new TextToSpeech(this, this);

        new Thread(new ClientThreadForNLG()).start();

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
        final int both_mode_button_width = width/3 - button_margin;
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

        // Both mode changes view to assistant on top of news
        both_mode_button = new Button(this);
        both_mode_button.setTextSize(12);
        both_mode_button.setTextColor(Color.parseColor("#000000"));
        both_mode_button.setBackgroundColor(Color.parseColor("#0ABEF5"));
        both_mode_button.setBackgroundResource(R.drawable.both_mode_button);
        LinearLayout.LayoutParams both_mode_button_params = new LinearLayout.LayoutParams(
                both_mode_button_width, button_panel_height);
        both_mode_button_params.setMargins(10, 5, 10, 5);
        both_mode_button.setLayoutParams(both_mode_button_params);
        button_panel.addView(both_mode_button, both_mode_button_params);

        // Camera rapport_test
        stream_button = new Button(this);
        stream_button.setTextSize(12);
        stream_button.setTextColor(Color.parseColor("#000000"));
        stream_button.setBackgroundColor(Color.parseColor("#0ABEF5"));
        LinearLayout.LayoutParams stream_button_params = new LinearLayout.LayoutParams(
                both_mode_button_width, button_panel_height);
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

        mic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));

//                if (CameraPreview.recording) {
//                    CameraPreview.recorder.stop();
//                    if (CameraPreview.usecamera) {
//                        try {
//                            CameraPreview.mCamera.reconnect();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    // recorder.release();
//                    CameraPreview.recording = false;
//                    Log.v("ERRORCHECK", "Recording Stopped");
//                    // Let's prepareRecorder so we can record again
//                    CameraPreview.prepareRecorder();
//
//
//                } else {
//                    CameraPreview.recording = true;
//                    CameraPreview.recorder.start();
//                    Log.v("ERRORCHECK", "Recording Started");
//                }
            }
        });

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
                    closeAndroidClient();
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
                if (both_mode_clicked) {
                    both_mode_clicked = false;
                    both_mode_button.setBackgroundResource(R.drawable.both_mode_button);
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
                    closeAndroidClient();
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
                if (both_mode_clicked) {
                    both_mode_clicked = false;
                    both_mode_button.setBackgroundResource(R.drawable.both_mode_button);
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


        both_mode_button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                both_mode_clicked = true;
                both_mode_button.setBackgroundResource(R.drawable.both_mode_button_pressed);
                if (stream_button_clicked) {
                    stream_button_clicked = false;
                    closeSocketClient();
                    closeAndroidClient();
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

        // WoZ mode
        stream_button.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
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
                if (both_mode_clicked) {
                    both_mode_clicked = false;
                    both_mode_button.setBackgroundResource(R.drawable.both_mode_button);
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
                    layoutMain.addView(layoutLeft, DrawerLayout.LayoutParams.MATCH_PARENT, height - small_assistant_height);

                    mThread = new SocketClientAndroid();
                    mThread.start();

                    androidAudioSocket = new AndroidAudioClient();
                    androidAudioSocket.start();
                }
            }
        });

//        // Let the initial view be the assistant view
        both_mode_button.performClick();

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
        if (audioclient != null && socketclient != null) {
            audioclient.close();
            socketclient.close();
            audioclient = null;
            socketclient = null;
        }
    }

    private void closeAndroidClient() {
        if( mThread != null ) {
            mThread.close();
            mThread = null;
        }

        if (androidAudioSocket != null) {
            androidAudioSocket.close();
            androidAudioSocket = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ERRORCHECK", "Closing android socket client");

        if (CameraPreview.recording) {
            CameraPreview.recorder.stop();
            if (CameraPreview.usecamera) {
                try {
                    CameraPreview.mCamera.reconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // recorder.release();
            CameraPreview.recording = false;
            Log.v("ERRORCHECK", "Recording Stopped");
            // Let's prepareRecorder so we can record again
            CameraPreview.prepareRecorder();
        }

        if (mPreview != null) {
            FrameLayout preview = (FrameLayout) findViewById(R.id.rapport_camera_preview);
            preview.removeView(mPreview);
            mPreview = null;
        }

        closeSocketClient();

//        if( mThread != null ) {
//            mThread.close();
//            mThread = null;
//        }

        if (androidAudioSocket != null) {
            androidAudioSocket.close();
            androidAudioSocket = null;
        }

        stop_NLG_thread = true;
        if (socket_NLG != null) {
            try {
                Log.d("STOPPING", "closing nlg socket");
                socket_NLG.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Log.d("ERRORCHECK", "null nlg socket");
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

//        mThread = new SocketClientAndroid();
//        mThread.start();
//
//        androidAudioSocket = new AndroidAudioClient();
//        androidAudioSocket.start();

        // Start the NLG and DM sockets
        stop_NLG_thread = false;
        new Thread(new ClientThreadForNLG()).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if( mThread != null ) {
//            mThread.close();
//            mThread = null;
//        }
//
//        if (androidAudioSocket != null) {
//            androidAudioSocket.close();
//            androidAudioSocket = null;
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // need a new camera object
//        mCameraManager = new CameraManager(this);
//        mPreview = new CameraPreview(this, mCameraManager.getCamera());
//        FrameLayout preview = (FrameLayout) findViewById(R.id.rapport_camera_preview);
//        preview.addView(mPreview);

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
                Log.d("Stream", "setting imageView");
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
            case R.id.record:
                if(item.getTitle().equals(new String("Start Recording"))){
                    item.setTitle("Stop Recording");
                    if(!CameraPreview.recording) {
                        CameraPreview.recording = true;
                        CameraPreview.recorder.start();
                        Log.v("ERRORCHECK", "Recording Started");
                    }

                }else{
                    item.setTitle("Start Recording");
                    if (CameraPreview.recording) {
                        CameraPreview.recorder.stop();
                        if (CameraPreview.usecamera) {
                            try {
                                CameraPreview.mCamera.reconnect();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.v("ERRORCHECK", "Recording Stopped");
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }



    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        //TODO: ojrl
        mUnityPlayer.windowFocusChanged(hasFocus);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //TODO: ojrl
        try {
//            if (mThread == null) {
//                mThread = new SocketClientAndroid();
//                mThread.start();
//            }
//
//            if (androidAudioSocket != null) {
//                androidAudioSocket.close();
//                androidAudioSocket = null;
//            }

            mCameraManager.onResume();
            mPreview.setCamera(mCameraManager.getCamera());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode){
            case MY_DATA_CHECK_CODE:{
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    //the user has the necessary data - create the TTS
                    myTTS = new TextToSpeech(this, this);
                } else {
                    //no data - install it now
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
            }
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != intent) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String str_result = result.get(0);

                    Toast.makeText(context, str_result, Toast.LENGTH_SHORT).show();
                    Log.d("Speech", "Speech: " + str_result);

                    // scroll news drawer
                    //if(str_result.contains("next")){
                    //	Message msg = new Message();
                    //	msg.what = UIHandler.SCROLL_TO_ITEM;
                    //	msg.obj = getCurrentFrag().getPluggableAdapterView();
                    //	msg.arg1 = 2;
                    //	App.get().getUIHandler().sendMessage(msg);
                    //
                    //}

                    //generate backchannel
                    //String backchannel = backChannelGenerator.generateBackChannel(str_result);
                    //send to TTS
                    //speakWords(str_result);
                    //speakWords(backchannel);

                    //send to Galaxy server
//                    try {
//                        PrintWriter out = new PrintWriter(new BufferedWriter(
//                                new OutputStreamWriter(socket_olympus.getOutputStream())),
//                                true);
//                        out.println(str_result);
//                    } catch (UnknownHostException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }

    }


    public void onInit(int status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBeginningOfSpeech() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onEndOfSpeech() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onError(int error) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResults(Bundle results) {
        //System.out.println("SPEECH!!!!!!!!!");
        Log.d("Speech", "onResults");
        ArrayList<String> strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < strlist.size();i++ ) {
            Log.d("Speech", "result=" + strlist.get(i));
        }
        String result_str = strlist.get(0);

        Toast.makeText(context, result_str, Toast.LENGTH_SHORT).show();
        Log.d("Speech", "Speech: " + result_str);
        //speakWords(strlist.get(0));

        //		AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        //        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        //        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        //        amanager.setStreamMute(AudioManager.STREAM_RING, true);
        //        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

//        try {
//            PrintWriter out = new PrintWriter(new BufferedWriter(
//                    new OutputStreamWriter(socket_olympus.getOutputStream())),
//                    true);
//            out.println(result_str);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        // scroll news drawer
        //if(result_str.contains("next")){
        //	currentNewsId++;
        //	Message msg = new Message();
        //	msg.what = UIHandler.SCROLL_TO_ITEM;
        //	msg.obj = getCurrentFrag().getPluggableAdapterView();
        //	msg.arg1 = currentNewsId;
        //	App.get().getUIHandler().sendMessage(msg);
        //}
        //this.speakWords(String.valueOf(currentNewsId+1));
        //
        //speechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // TODO Auto-generated method stub

    }

}