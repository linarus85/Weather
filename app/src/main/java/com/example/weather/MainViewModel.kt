package com.example.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.Adapter.Model

class MainViewModel: ViewModel() {
    val liveDataCurrent = MutableLiveData<Model>()
    val liveDataList = MutableLiveData<List<Model>>()
}