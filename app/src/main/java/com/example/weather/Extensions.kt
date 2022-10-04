package com.example.weather

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.isPermissionGranted(par: String):Boolean {
    return ContextCompat.checkSelfPermission(activity as AppCompatActivity,
    par)== PackageManager.PERMISSION_GRANTED
}