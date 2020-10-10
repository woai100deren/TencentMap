package com.dj.tencentmap;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dj.library.LogUtils;
import com.dj.tencentmap.databinding.ActivityMainBinding;
import com.dj.tencentmap.model.MapModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MapFragment mapFragment;
    private boolean isSeekBarChange = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        addMapFragment();


        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isSeekBarChange) {
                    LogUtils.e("当前大小：" + progress / 1000.0f);
                    mapFragment.moveCamera(progress/ 1000.0f); //移动地图
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

        MapModel mapModel = ViewModelProviders.of(this).get(MapModel.class);//获取ViewModel,让ViewModel与此activity绑定
        mapModel.getMapZoomLevel().observe(this, new Observer<Integer>() { //注册观察者
            @Override
            public void onChanged(Integer s) {
                LogUtils.e("activity中接收zoom变化："+s);
                binding.seekBar.setProgress(s,false);
            }
        });

        binding.jhBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.aggregate();
            }
        });

        binding.addMarkerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.addMarker();
            }
        });

        binding.carMoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapFragment.carMove();
            }
        });
    }

    /**
     * 添加地图fragment
     */
    private void addMapFragment(){
        mapFragment = new MapFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.mapFragment, mapFragment).commit();
    }
}