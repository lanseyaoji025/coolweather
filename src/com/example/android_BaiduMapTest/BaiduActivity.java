package com.example.android_BaiduMapTest;

import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.android_locationtest.R;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class BaiduActivity extends Activity {

	private MapView mapView;
	private String provider;
	private boolean isFirstLocation =true;
	private BaiduMap baiduMap;
	private LocationManager locationManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.baidumap);
		mapView=(MapView) findViewById(R.id.map_view);
		baiduMap=mapView.getMap();
		baiduMap.setMyLocationEnabled(true);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providersList = locationManager.getProviders(true);
		if (providersList.contains(LocationManager.NETWORK_PROVIDER)) {
			provider=LocationManager.NETWORK_PROVIDER;
		}else if (providersList.contains(LocationManager.GPS_PROVIDER)) {
			provider=LocationManager.GPS_PROVIDER;
		}else {
			//当没有可用的位置提供器时，弹出Toast提示用户
			Toast.makeText(this, "No Location provider to use", Toast.LENGTH_SHORT).show();
			return ;
		}
		Location location = locationManager.getLastKnownLocation(provider);
		if (location!=null) {
			navigate(location);
		}
		locationManager.requestLocationUpdates(provider, 5000, 10, locationListener);
	}
	
	private void navigate(Location location) {
		if (isFirstLocation) {
			LatLng lng =new LatLng(location.getLatitude(), location.getLongitude());
			MapStatusUpdate newLatLng = MapStatusUpdateFactory.newLatLng(lng);
			baiduMap.animateMapStatus(newLatLng);
			MapStatusUpdate zoomTo = MapStatusUpdateFactory.zoomTo(16f);
			baiduMap.animateMapStatus(zoomTo);
			isFirstLocation=false;
		}
		MyLocationData.Builder builder=new MyLocationData.Builder();
		builder.latitude(location.getLatitude());
		builder.longitude(location.getLongitude());
		MyLocationData locationData = builder.build();
		baiduMap.setMyLocationData(locationData);
	}
	LocationListener locationListener=new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChanged(Location location) {
			if (location!=null) {
				navigate(location);
			}
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		baiduMap.setMyLocationEnabled(false);
		mapView.onDestroy();
		locationManager.removeUpdates(locationListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}
}
