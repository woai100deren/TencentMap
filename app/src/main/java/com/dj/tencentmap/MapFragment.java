package com.dj.tencentmap;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dj.library.LogUtils;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment;
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions;
import com.tencent.tencentmap.mapsdk.maps.UiSettings;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;

public class MapFragment extends SupportMapFragment implements LocationSource, TencentLocationListener {

    protected LocationSource.OnLocationChangedListener locationChangedListener;

    protected TencentLocationManager locationManager;
    protected TencentLocationRequest locationRequest;

    protected UiSettings mapUiSettings;

    @Override
    public View onCreateView(LayoutInflater layoutinflater, ViewGroup viewgroup, Bundle bundle) {
        View view = super.onCreateView(layoutinflater, viewgroup, bundle);
        initLocation();
        initUiSetting();
        return view;
    }

    private void initUiSetting() {
        mapUiSettings=getMap().getUiSettings();
        mapUiSettings.setScaleViewEnabled(true);
        mapUiSettings.setScaleViewPosition(TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_CENTER);
        //设置是否显示定位按钮
        mapUiSettings.setMyLocationButtonEnabled(true);
        //设置当前是否显示比例尺
        mapUiSettings.setScaleViewEnabled(true);
        //设置比例尺的显示位置
        mapUiSettings.setScaleViewPosition(TencentMapOptions.SCALEVIEW_POSITION_BOTTOM_LEFT);
        //设置是否开启地图缩放手势
        mapUiSettings.setZoomGesturesEnabled(true);


        //3D建筑物是否显示(默认true)
        getMap().setBuilding3dEffectEnable(true);
        LogUtils.e("最小缩放："+getMap().getMinZoomLevel()+",最大缩放："+getMap().getMaxZoomLevel());
    }

    /**
     * 定位的一些初始化设置
     */
    private void initLocation() {
        //用于访问腾讯定位服务的类, 周期性向客户端提供位置更新
        locationManager = TencentLocationManager.getInstance(getContext());
        //设置坐标系
        locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        //创建定位请求
        locationRequest = TencentLocationRequest.create();
        //设置定位周期（位置监听器回调周期）为3s
        locationRequest.setInterval(3000);
        //地图上设置定位数据源
        getMap().setLocationSource(this);
        //设置当前位置可见
        getMap().setMyLocationEnabled(true);
    }

    /**
     * 设置定位图标样式
     */
    public void setLocStyle(MyLocationStyle locationStyle) {
        getMap().setMyLocationStyle(locationStyle);
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (i == TencentLocation.ERROR_OK && locationChangedListener != null) {
            Location location = new Location(tencentLocation.getProvider());
            //设置经纬度以及精度
            location.setLatitude(tencentLocation.getLatitude());
            location.setLongitude(tencentLocation.getLongitude());
            location.setAccuracy(tencentLocation.getAccuracy());
            locationChangedListener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;
        locationManager.requestLocationUpdates(locationRequest, this, Looper.myLooper());
    }

    @Override
    public void deactivate() {
        locationManager.removeUpdates(this);
        locationManager = null;
        locationRequest = null;
        locationChangedListener = null;
    }

}
