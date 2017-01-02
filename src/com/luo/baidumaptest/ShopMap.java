package com.luo.baidumaptest;

import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.luo.baidumaptest2.R;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class ShopMap extends AppCompatActivity {
	private final static String TAG = "MainActivity";
	private MapView mMapView;
//	private LocationClient mLocationClient;
//	private BDLocationListener myListener = new MyLocationListener();
	private BitmapDescriptor mCurrentMarker;
//	private boolean isFirstLocate = true;
	private BaiduMap baiduMap;
//	private int count;
	private MyLocationConfiguration config;
	private GeoCoder mGeoCoder;
	
	private LocationManager locationManager;
	private String provider;
	private boolean isFirstLoc = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getSupportActionBar().hide();
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		mMapView = (MapView) findViewById(R.id.mMapView);
		baiduMap = mMapView.getMap();
		
		mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.zhizhen);
		config = new MyLocationConfiguration(
        		com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, true, mCurrentMarker);  
        baiduMap.setMyLocationConfigeration(config);
        
//		baiduMap.setMyLocationEnabled(true);
//		mLocationClient = new LocationClient(getApplicationContext());
//		mLocationClient.registerLocationListener(myListener);
//		initLocation();
//		mLocationClient.start();
		initMyLocation();
		
	}
	// -----------------------
	private void navigateToAddress() {
		OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {  
		    public void onGetGeoCodeResult(GeoCodeResult result) {  
		    	if (mMapView == null || baiduMap == null) {
					return;
				}
		        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
//		        	Toast.makeText(ShopMap.this, "地址解析错误", Toast.LENGTH_SHORT).show();
//		        	mGeoCoder.geocode(new GeoCodeOption().city("中国广东深圳").address("福田区"));
		        	sendAddressRequest();
		        	return;
		        } else { 
		        
    		        //获取地理编码结果  
    		        LatLng ll = result.getLocation();
    		        if (ll != null) {
    		        	navigateTo(ll.latitude, ll.longitude);
    		        	Log.v(TAG, "onGetGeoCodeResult() & ll.latitude = " + ll.latitude + "; ll.longitude = " + ll.longitude);
    				} else {
    					Log.v(TAG, "onGetGeoCodeResult() & ll is null");
//    					mGeoCoder.geocode(new GeoCodeOption().city("中国广东深圳").address("福田区"));
    					sendAddressRequest();
    				}
		        }
		    }  
		 
		    @Override  
		    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {  
		        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
		            //没有找到检索结果  
//		        	Toast.makeText(MainActivity.this, "onGetReverseGeoCodeResult is null", Toast.LENGTH_SHORT).show();
		        }  
		        //获取反向地理编码结果  
		    }  
		};
//		if (count == 0) {
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(listener);
//		}
//		mGeoCoder.geocode(new GeoCodeOption().city("中国广东深圳").address("福田区"));
		sendAddressRequest();
	}
	
	private void sendAddressRequest () {
		mGeoCoder.geocode(new GeoCodeOption().city("中国广东深圳").address("福田区保税区市花路16号"));
	}

	private void initMyLocation () {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 获取所有位置提供器
		List<String> providerList = locationManager.getProviders(true);
		if (providerList.contains(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		} else {
			return;
		}
		
		Location location = locationManager.getLastKnownLocation(provider);
		if (location != null) {
			navigateTo(location);
			Log.v(TAG, "initMyLocation()");
		}
		
		locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);
	}
	
	private LocationListener locationListener = new LocationListener() {
		
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
			navigateTo(location);
			Log.v(TAG, "onLocationChanged()");
		}
	};
	
	private void navigateTo (Location location) {
		if (isFirstLoc) {
			LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
			MapStatus mapStatus = new MapStatus.Builder().target(ll).zoom(15f).build();
			MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
			baiduMap.setMapStatus(mMapStatusUpdate);
			isFirstLoc = false;
			if (locationManager != null) {
				locationManager.removeUpdates(locationListener);
			}
			navigateToAddress();
		} else {
			return;
		}
	}
	

	private void navigateTo (double latitude, double longitude) {
//		if (isFirstLocate) {
			baiduMap.setMyLocationEnabled(true);
			LatLng ll = new LatLng(latitude, longitude);
			MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
			baiduMap.animateMapStatus(update);
			update = MapStatusUpdateFactory.zoomTo(15f);
			baiduMap.animateMapStatus(update);
//			if (count > 0) {
//				isFirstLocate = false;
//			}
//		}
		MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
		locationBuilder.accuracy(100);
		locationBuilder.latitude(latitude);
		locationBuilder.longitude(longitude);
		MyLocationData locationData = locationBuilder.build();
		baiduMap.setMyLocationData(locationData);
//		count ++;
	}
	// ----------------------------
	
	
//	private void initLocation(){
//        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
//        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
//        int span=1000;
//        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
//        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
//        option.setOpenGps(true);//可选，默认false,设置是否使用gps
//        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
//        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死  
//        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
//        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
//        mLocationClient.setLocOption(option);
//    }
	
//	private void navigateTo(BDLocation location){
//		if (isFirstLocate) {
//			LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
//			MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
//			baiduMap.animateMapStatus(update);
//			update = MapStatusUpdateFactory.zoomTo(14f);
//			baiduMap.animateMapStatus(update);
////			isFirstLocate = false;
//		}
//		
//		MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
//		locationBuilder.accuracy(location.getRadius());
//		locationBuilder.latitude(location.getLatitude());
//		locationBuilder.longitude(location.getLongitude());
//		MyLocationData locationData = locationBuilder.build();
//		baiduMap.setMyLocationData(locationData);
//	}
	
//	public class MyLocationListener implements BDLocationListener {
//		 
//        @Override
//        public void onReceiveLocation(final BDLocation location) {
//        	
//        	if (count > 1) {
//				return;
//			}
//        	
//        	
//    		OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {  
//    		    public void onGetGeoCodeResult(GeoCodeResult result) {  
////    		    	if (count == 0) {
////    	        		navigateTo(location);
////    				}
//    		        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
//    		            //没有检索到结果  
////    		        	Toast.makeText(MainActivity.this, "onGetGeoCodeResult is null", Toast.LENGTH_SHORT).show();
//    		        	navigateTo(location);
//    		        } else { 
//    		        
//	    		        //获取地理编码结果  
//	    		        LatLng ll = result.getLocation();
//	    		        if (ll != null) {
//	    		        	navigateTo(ll.latitude, ll.longitude);
//	    		        	Log.v(TAG, "onGetGeoCodeResult() & ll.latitude = " + ll.latitude + "; ll.longitude = " + ll.longitude);
//	    				} else {
//	    					Log.v(TAG, "onGetGeoCodeResult() & ll is null");
//	    					mGeoCoder.geocode(new GeoCodeOption() 
//	    		    			    .city("中国广东深圳")
//	    		    			    .address("福田区"));
//	    				}
//    		        }
//    		    }  
//    		 
//    		    @Override  
//    		    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {  
//    		        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
//    		            //没有找到检索结果  
////    		        	Toast.makeText(MainActivity.this, "onGetReverseGeoCodeResult is null", Toast.LENGTH_SHORT).show();
//    		        }  
//    		        //获取反向地理编码结果  
//    		    }  
//    		};
//    		if (count == 0) {
//    			mGeoCoder = GeoCoder.newInstance();
//    			mGeoCoder.setOnGetGeoCodeResultListener(listener);
//			}
//    		mGeoCoder.geocode(new GeoCodeOption() 
//    			    .city("中国广东深圳")
//    			    .address("福田区"));
//        }
//	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGeoCoder.destroy();
		baiduMap.setMyLocationEnabled(false);
//		mLocationClient.stop();
		mMapView.onDestroy();
	}

}
