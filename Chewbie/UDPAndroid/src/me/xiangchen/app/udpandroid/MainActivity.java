package me.xiangchen.app.udpandroid;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	
	
	
	static LinearLayout layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
		
		layout = new LinearLayout(this);
		layout.setBackgroundColor(Color.BLACK);
		layout.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				new NetworkTask().execute();
				return false;
			}
		});
		
		
		
		setContentView(layout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	class NetworkTask extends AsyncTask {
		final static String ipToSend = "10.142.224.68";
		
		@Override
		protected Object doInBackground(Object... arg0) {
			// TODO Auto-generated method stub
			DatagramSocket clientSocket;
			try {
				clientSocket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(ipToSend);
				byte[] sendData = new byte[1024];
				byte[] receiveData = new byte[1024];
				String sentence = "form follows functions";
				sendData = sentence.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, IPAddress, 1027);
				Log.d("UDP Android", sentence + ": " + sendData.length);
				clientSocket.send(sendPacket);
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				clientSocket.receive(receivePacket);
				String modifiedSentence = new String(receivePacket.getData());
				System.out.println("FROM SERVER:" + modifiedSentence);
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
	}

//	private static void sendTestPacket() throws IOException {
////		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
////				System.in));
//		layout.setAlpha(layout.getAlpha() - 0.1f);
//		DatagramSocket clientSocket = new DatagramSocket();
//		InetAddress IPAddress = InetAddress.getByName(ipToSend);
//		byte[] sendData = new byte[1024];
//		byte[] receiveData = new byte[1024];
//		String sentence = "form follows functions";
//		sendData = sentence.getBytes();
//		DatagramPacket sendPacket = new DatagramPacket(sendData,
//				sendData.length, IPAddress, 1017);
//		Log.d("UDP Android", sentence + ": " + sendData.length);
//		clientSocket.send(sendPacket);
//		DatagramPacket receivePacket = new DatagramPacket(receiveData,
//				receiveData.length);
//		clientSocket.receive(receivePacket);
//		String modifiedSentence = new String(receivePacket.getData());
//		System.out.println("FROM SERVER:" + modifiedSentence);
//		clientSocket.close();
//	}
}
