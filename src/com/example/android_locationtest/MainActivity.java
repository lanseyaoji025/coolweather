package com.example.android_locationtest;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.content.ContentUris;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

	private static final int SHOW_LOCATION=0;
	private TextView positionTextView;
	private	LocationManager locationManager;
	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		positionTextView=(TextView) findViewById(R.id.tv);
		locationManager=  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//获取所有可用的位置提供器
		List<String> list=locationManager.getProviders(true);
		if (list.contains(LocationManager.GPS_PROVIDER)) {
			provider=LocationManager.GPS_PROVIDER;
		}else if(list.contains(LocationManager.NETWORK_PROVIDER)){
			provider=LocationManager.NETWORK_PROVIDER ;
		}else {
			//当没有可用的位置提供器时，弹出Toast提示用户
			Toast.makeText(this, "No Location provider to use", Toast.LENGTH_SHORT).show();
			return ;
		}
		Location location=locationManager.getLastKnownLocation(provider);
		if (location!=null) {
			showLocation(location);
		}
		locationManager.requestLocationUpdates(provider, 5000, 10,locationListener);
	}

	LocationListener locationListener=new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
			showLocation(location);
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (locationManager!=null) {
			locationManager.removeUpdates(locationListener);
		}
	}
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_LOCATION:
				String currentPosition=(String)msg.obj;
				positionTextView.setText(currentPosition);
				break;

			default:
				break;
			}
		};
	};
	
	private void showLocation(final Location location) {
		//		String cu rrentPosition="Latitude is"+location.getLatitude()+"\n"+"Longitude is"+location.getLongitude();
		//		positionTextView.setText(currentPosition);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//组装反向地理编码的接口地址
					StringBuilder url=new StringBuilder();
					url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
					url.append(location.getLatitude()).append(",");
					url.append(location.getLongitude());
					url.append("&sensor=false");
					HttpClient client=new DefaultHttpClient();
					HttpGet httpGet=new HttpGet(url.toString());
					//在请求消息头中指定语言，保证服务器会返回中文数据。
					httpGet.addHeader("Accept-Language","zh-CN");
					HttpResponse response = client.execute(httpGet);
					if (response.getStatusLine().getStatusCode()==200) {
						HttpEntity entity = response.getEntity();
						String respone=EntityUtils.toString(entity,"utf-8");
						JSONObject jsonObject=new JSONObject(respone);
						//获取results节点下的位置信息
						JSONArray resultArray=jsonObject.getJSONArray("results");
						if (resultArray.length()>0) {
							JSONObject subObject=resultArray.getJSONObject(0);
							//获取格式化后的位置信息
							String address=subObject.getString("formatted_address");
							Message message=new Message();
							message.what=SHOW_LOCATION;
							message.obj=address;
							handler.sendMessage(message);
						}
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}
}
