package com.example.weather.Fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import com.android.volley.Request
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.Adapter.Model
import com.example.weather.Adapter.ViewPageAdapter
import com.example.weather.MainViewModel
import com.example.weather.R
import com.example.weather.databinding.FragmentMainBinding
import com.example.weather.isPermissionGranted
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import com.example.weather.DiologGps

const val API_KEY = "62c24fc707174f1ab37185958220509"

class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tabList = listOf(
        "Hours",
        "Days"
    )
    private val model: MainViewModel by activityViewModels()
    private lateinit var launcher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
//        reqData("Kazan")  //  getLocation()
    }
    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = ViewPageAdapter(activity as FragmentActivity, fList)
        binding.vpage.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.vpage) { tab, posi ->
            tab.text = tabList[posi]
        }.attach()
        binding.cloudButton.setOnClickListener {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
            checkLocation()
        }
        binding.searchButton.setOnClickListener{
            DiologGps.searchCity(requireContext(),object :DiologGps.Listener{
                override fun onClick(name: String?) {
                    name?.let { it1 -> reqData(it1) }
                }
            })
        }
    }


    private fun isLocation(): Boolean {
        val locmanager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    private fun checkLocation(){
        if(isLocation()){
            getLocation()
        } else {
            DiologGps.locationSettingsDialog(requireContext(), object : DiologGps.Listener{
                override fun onClick(name:String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun getLocation() {

        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                reqData("${it.result.latitude},${it.result.longitude}")
            }
    }

    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxminTemp = "${it.minTemp}°c / ${it.maxTemp}°c"
            val curTemp = "${it.currentTime}°c"
            date.text = it.time
            CityCard.text = it.city
            currentTemp.text = curTemp
            condision.text = it.condition
            minMaxTemp.text = maxminTemp
            Picasso.get().load("http:" + it.imageUrl).into(imageCard)

        }
    }

    fun permissionListener() {
        launcher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun reqData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=3&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val req = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseData(result)
            },
            { error ->
                Log.d("Logs", "Error:$error")
            }
        )
        queue.add(req)
    }

    private fun parseData(result: String) {
        val obj = JSONObject(result)
        val list = parseDays(obj)
        parseCurrentData(obj, list[0])

    }

    private fun parseDays(obj: JSONObject): ArrayList<Model> {
        val list = ArrayList<Model>()
        val daysArray = obj.getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name = obj.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = Model(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c")
                    .toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c")
                    .toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }


    private fun parseCurrentData(obj: JSONObject, gradusItem: Model) {
        val item = Model(
            obj.getJSONObject("location").getString("name"),
            obj.getJSONObject("current").getString("last_updated"),
            obj.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            obj.getJSONObject("current").getString("temp_c")
                .toFloat().toInt().toString(),
            gradusItem.minTemp,
            gradusItem.maxTemp,
            obj.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            gradusItem.hours,
        )
        model.liveDataCurrent.value = item


    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}