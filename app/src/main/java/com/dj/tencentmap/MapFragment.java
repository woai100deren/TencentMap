package com.dj.tencentmap;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.dj.library.LogUtils;
import com.dj.tencentmap.model.MapModel;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions;
import com.tencent.tencentmap.mapsdk.maps.UiSettings;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;
import com.tencent.tencentmap.mapsdk.vector.utils.animation.MarkerTranslateAnimator;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterManager;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends SupportMapFragment implements LocationSource, TencentLocationListener , TencentMap.OnCameraChangeListener {
    private FragmentActivity activity;
    protected LocationSource.OnLocationChangedListener locationChangedListener;

    protected TencentLocationManager locationManager;
    protected TencentLocationRequest locationRequest;

    protected UiSettings mapUiSettings;
    private boolean is2d = true;

    @Override
    public View onCreateView(LayoutInflater layoutinflater, ViewGroup viewgroup, Bundle bundle) {
        View view = super.onCreateView(layoutinflater, viewgroup, bundle);
        initLocation();
        initUiSetting();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
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
        getMap().setOnCameraChangeListener(this);

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
        //连续定位
//        locationManager.requestLocationUpdates(locationRequest, this, Looper.myLooper());
        //单次定位
        locationManager.requestSingleFreshLocation(null, this, Looper.getMainLooper());
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

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LogUtils.e("zoom变化："+cameraPosition);
        if(activity!=null) {
            ViewModelProviders.of(activity).get(MapModel.class).setMapZoomLevel((int) (cameraPosition.zoom * 1000));
        }
    }

    @Override
    public void onCameraChangeFinished(CameraPosition cameraPosition) {
        LogUtils.e("zoom变化："+cameraPosition);
        if(activity!=null) {
            ViewModelProviders.of(activity).get(MapModel.class).setMapZoomLevel((int) (cameraPosition.zoom * 1000));
        }
    }

    public void moveCamera(float zoomLevel){
        //地图视野调整，参考：https://lbs.qq.com/mobile/androidMapSDK/developerGuide/setCamera
        CameraUpdate cameraSigma = CameraUpdateFactory.zoomTo(zoomLevel);
        getMap().moveCamera(cameraSigma); //移动地图
    }

    public void addMarker(){
        final LatLng position = new LatLng(30.476195,114.416428);
        final Marker mMarker = getMap().addMarker(new MarkerOptions(position));
        getMap().setOnMarkerClickListener(new TencentMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getId().equals(mMarker.getId())) {
                    LogUtils.e("点击了Marker:"+mMarker.getId());
                    CameraUpdate cameraSigma = CameraUpdateFactory.newLatLngZoom(position,//中心点坐标，地图目标经纬度
                            18);//目标缩放级别
                    getMap().animateCamera(cameraSigma); //移动地图,在 500ms 内以匀速将地图状态设置为 cameraUpdate
                }
                return false;
            }
        });
        //设置Marker支持点击
        mMarker.setClickable(true);
    }

    public void carMove(){
        //第一步：解析路线
        String mLine = "39.98409,116.30804,39.98409,116.3081,39.98409,116.3081,39.98397,116.30809,39.9823,116.30809,39.9811,116.30817,39.9811,116.30817,39.97918,116.308266,39.97918,116.308266,39.9791,116.30827,39.9791,116.30827,39.979008,116.3083,39.978756,116.3084,39.978386,116.3086,39.977867,116.30884,39.977547,116.308914,39.976845,116.308914,39.975826,116.308945,39.975826,116.308945,39.975666,116.30901,39.975716,116.310486,39.975716,116.310486,39.975754,116.31129,39.975754,116.31129,39.975784,116.31241,39.975822,116.31327,39.97581,116.31352,39.97588,116.31591,39.97588,116.31591,39.97591,116.31735,39.97591,116.31735,39.97593,116.31815,39.975967,116.31879,39.975986,116.32034,39.976055,116.32211,39.976086,116.323395,39.976105,116.32514,39.976173,116.32631,39.976254,116.32811,39.976265,116.3288,39.976345,116.33123,39.976357,116.33198,39.976418,116.33346,39.976418,116.33346,39.97653,116.333755,39.97653,116.333755,39.978157,116.333664,39.978157,116.333664,39.978195,116.33509,39.978195,116.33509,39.978226,116.33625,39.978226,116.33625,39.97823,116.33656,39.97823,116.33656,39.978256,116.33791,39.978256,116.33791,39.978016,116.33789,39.977047,116.33791,39.977047,116.33791,39.97706,116.33768,39.97706,116.33768,39.976967,116.33706,39.976967,116.33697";
        String[] linePointsStr = mLine.split(",");
        LatLng[] mCarLatLngArray = new LatLng[linePointsStr.length / 2];
        for (int i = 0; i < mCarLatLngArray.length; i++) {
            double latitude = Double.parseDouble(linePointsStr[i * 2]);
            double longitude = Double.parseDouble(linePointsStr[i * 2 + 1]);
            mCarLatLngArray[i] = new LatLng(latitude, longitude);
        }
        //第二步：添加小车路线
        getMap().addPolyline(new PolylineOptions().add(mCarLatLngArray));
        //第三步：添加小车
        LatLng carLatLng = mCarLatLngArray[0];
        Marker mCarMarker = getMap().addMarker(
                new MarkerOptions(carLatLng)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.taxi))
                        .flat(true)
                        .clockwise(false));
        //第四步：创建移动动画
        MarkerTranslateAnimator mAnimator = new MarkerTranslateAnimator(mCarMarker, 5 * 1000, mCarLatLngArray, true);
        //第五步：调整最佳视界
        getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(
                LatLngBounds.builder().include(Arrays.asList(mCarLatLngArray)).build(), 50));
        //第六步：开启动画移动
        mAnimator.startAnimation();
    }

    /**
     * 切换倒伏角度(skew) : 以相机为顶点与地图平面的垂线和地图中心点之间的夹角，理解为2D和3D切换
     */
    public void changeSkew(){
        if(is2d){
            is2d = false;
            CameraUpdate cameraSigma =
                    CameraUpdateFactory.newCameraPosition(new CameraPosition(
                            getMap().getCameraPosition().target, //中心点坐标，地图目标经纬度
                            getMap().getCameraPosition().zoom,  //目标缩放级别
                            45f, //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                            getMap().getCameraPosition().bearing)); //目标旋转角 0~360° (正北方为0)
            getMap().moveCamera(cameraSigma); //移动地图
        }else{
            is2d = true;
            CameraUpdate cameraSigma =
                    CameraUpdateFactory.newCameraPosition(new CameraPosition(
                            getMap().getCameraPosition().target, //中心点坐标，地图目标经纬度
                            getMap().getCameraPosition().zoom,  //目标缩放级别
                            0, //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                            getMap().getCameraPosition().bearing)); //目标旋转角 0~360° (正北方为0)
            getMap().moveCamera(cameraSigma); //移动地图
        }
    }
}
