package com.dj.tencentmap;

import android.os.Bundle;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.dj.library.LogUtils;
import com.dj.tencentmap.databinding.ActivityMainBinding;
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
                    LogUtils.e("当前大小：" + progress);
                    mapFragment.getMap().setMaxZoomLevel(progress);
                    mapFragment.getMap().setMinZoomLevel(progress);
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
                mapFragment.getMap().setMaxZoomLevel(20);
                mapFragment.getMap().setMinZoomLevel(3);
            }
        });

        mapFragment.getMap().setOnCameraChangeListener(new TencentMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                binding.seekBar.setProgress((int) cameraPosition.zoom,false);
            }

            @Override
            public void onCameraChangeFinished(CameraPosition cameraPosition) {
                LogUtils.e("当前缩放级别："+cameraPosition.zoom);
                binding.seekBar.setProgress((int) cameraPosition.zoom,false);
            }
        });
    }
}