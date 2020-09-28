package com.dj.tencentmap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.dj.tencentmap.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textView.setText("hhhh");
    }
}