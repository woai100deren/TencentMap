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
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterManager;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.List;

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
        //指南针控件是否展示
        mapUiSettings.setCompassEnabled(true);


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
        //设置定位周期（位置监听器回调周期）为3s,当定位周期大于0时, 不论是否有得到新的定位结果, 位置监听器都会按定位周期定时被回调; 当定位周期等于0时, 仅当有新的定位结果时, 位置监听器才会被回调(即, 回调时机存在不确定性). 如果需要周期性回调, 建议将 定位周期 设置为 5000-10000ms
//        locationRequest.setInterval(3000);
        locationRequest.setInterval(0);
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

    /**
     * 地图点聚合示例
     */
    public void aggregate(){
        // 实例化点聚合管理者
        ClusterManager mClusterManager = new ClusterManager<MarkerClusterItem>(getContext(), getMap());

        // 默认聚合策略,调用时不必添加,如果需要其他聚合策略可以按以下代码修改
        NonHierarchicalDistanceBasedAlgorithm<MarkerClusterItem> ndba = new NonHierarchicalDistanceBasedAlgorithm<>(getContext());
        // 设置点聚合生效距离,以dp为单位
        ndba.setMaxDistanceAtZoom(35);
        // 设置策略
        mClusterManager.setAlgorithm(ndba);

        // 设置聚合渲染器,默认使用的是DefaultClusterRenderer,可以不调用下列代码
        DefaultClusterRenderer<MarkerClusterItem> renderer = new DefaultClusterRenderer<>(getContext(), getMap(), mClusterManager);
        // 设置最小聚合数量,默认为4,这里设置为2,即有2个以上不包括2个marker才会聚合
        renderer.setMinClusterSize(2);
        // 定义聚合的分段,当超过5个不足10个的时候,显示5+,其他分段同理
        renderer.setBuckets(new int[]{5, 10, 20, 50});
        mClusterManager.setRenderer(renderer);


        //添加聚合数据
        List<MarkerClusterItem> items = new ArrayList<MarkerClusterItem>();
        items.add(new MarkerClusterItem(39.984059,116.307621));
        items.add(new MarkerClusterItem(39.981954,116.304703));
        items.add(new MarkerClusterItem(39.984355,116.312256));
        items.add(new MarkerClusterItem(39.980442,116.315346));
        items.add(new MarkerClusterItem(39.981527,116.308994));
        items.add(new MarkerClusterItem(39.979751,116.310539));
        items.add(new MarkerClusterItem(39.977252,116.305776));
        items.add(new MarkerClusterItem(39.984026,116.316419));
        items.add(new MarkerClusterItem(39.976956,116.314874));
        items.add(new MarkerClusterItem(39.978501,116.311827));
        items.add(new MarkerClusterItem(39.980277,116.312814));
        items.add(new MarkerClusterItem(39.980236,116.369022));
        items.add(new MarkerClusterItem(39.978838,116.368486));
        items.add(new MarkerClusterItem(39.977161,116.367488));
        items.add(new MarkerClusterItem(39.915398,116.396713));
        items.add(new MarkerClusterItem(39.937645,116.455421));
        items.add(new MarkerClusterItem(39.896304,116.321182));
        items.add(new MarkerClusterItem(31.254487,121.452827));
        items.add(new MarkerClusterItem(31.225133,121.485443));
        items.add(new MarkerClusterItem(31.216912,121.442528));
        items.add(new MarkerClusterItem(31.251552,121.500893));
        items.add(new MarkerClusterItem(31.249204,121.455917));
        items.add(new MarkerClusterItem(22.546885,114.042892));
        items.add(new MarkerClusterItem(22.538086,113.999805));
        items.add(new MarkerClusterItem(22.534756,114.082031));
        mClusterManager.addItems(items);
        getMap().setOnCameraChangeListener(mClusterManager);
    }
}
