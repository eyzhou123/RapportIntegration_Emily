package com.yahoo.inmind.rapport.view;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

//import android.util.Log;


public class AndroidAudioClient extends Thread {
	public static Socket mSocket;
	private CameraPreview mCameraPreview;
	private static final String TAG = "socket";
	private String mIP = RapportReaderActivity.server_ip;
	private int mPort = 50005;

//	private Button startButton,stopButton;

	public byte[] buffer;

	public static AudioRecord recorder;
	BufferedOutputStream outputStream;
	BufferedInputStream inputStream;

	private int sampleRate = 44100 ; // 44100 for music
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
	public boolean sendingAndroidAudio = true;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		try {
            sendingAndroidAudio = true;
			ByteArrayOutputStream byteArray = null;
			mSocket = new Socket();

			Log.d("ERRORCHECK", "creating audio socket");
			mSocket.connect(new InetSocketAddress(mIP, mPort), 0); // hard-code server address
			outputStream = new BufferedOutputStream(mSocket.getOutputStream());
			inputStream = new BufferedInputStream(mSocket.getInputStream());


			byte[] buffer = new byte[minBufSize];

			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
			Log.d("VS", "Recorder initialized");

			recorder.startRecording();


			while(sendingAndroidAudio == true) {
				//reading data from MIC into buffer
				int read = recorder.read(buffer, 0, buffer.length);
				Log.d("ERRORCHECK", "read: " + read);

				outputStream.write(intToBytes(read));
				outputStream.write(buffer);
				outputStream.flush();
//                Log.d("ERRORCHECK", "SENT");
				if (Thread.currentThread().isInterrupted())
				{
					System.out.println("thread interrupted");
					break;
				}
			}

		} catch(UnknownHostException e) {
			Log.e("VS", "UnknownHostException");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("VS", "IOException");
		}
		try {
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		recorder.stop();
//		recorder.release();

	}

	public void close() {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sendingAndroidAudio = false;

		recorder.stop();
		recorder.release();

	}

	public static byte[] intToBytes(int yourInt) throws IOException {
		return ByteBuffer.allocate(4).putInt(yourInt).array();
	}

}