package com.dj.tencentmap.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapModel extends ViewModel {
    private MutableLiveData<Integer> mapZoomLevel = new MutableLiveData<>();

    public MutableLiveData<Integer> getMapZoomLevel() {
        return mapZoomLevel;
    }

    public void setMapZoomLevel(Integer mapZoomLevel) {
        this.mapZoomLevel.setValue(mapZoomLevel);
    }
}
