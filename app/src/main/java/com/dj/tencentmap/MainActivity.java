package com.dj.tencentmap;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.dj.library.LogUtils;
import com.dj.tencentmap.databinding.ActivityMainBinding;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MapFragment mapFragment;
    private boolean isSeekBarChange = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isSeekBarChange) {
                    LogUtils.e("当前大小：" + progress / 1000.0f);
                    //地图视野调整，参考：https://lbs.qq.com/mobile/androidMapSDK/developerGuide/setCamera
                    CameraUpdate cameraSigma = CameraUpdateFactory.zoomTo(progress/ 1000.0f);
                    mapFragment.getMap().moveCamera(cameraSigma); //移动地图
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                LogUtils.e("触摸手势已经开始");
                isSeekBarChange = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LogUtils.e("触摸手势已经结束");
                isSeekBarChange = false;
            }
        });

        mapFragment.getMap().setOnCameraChangeListener(new TencentMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                binding.seekBar.setProgress((int) (cameraPosition.zoom * 1000),false);
            }

            @Override
            public void onCameraChangeFinished(CameraPosition cameraPosition) {
                LogUtils.e("当前缩放级别："+cameraPosition.zoom);
                binding.seekBar.setProgress((int) (cameraPosition.zoom * 1000),false);
            }
        });

        binding.jhBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.aggregate();
            }
        });
    }
}