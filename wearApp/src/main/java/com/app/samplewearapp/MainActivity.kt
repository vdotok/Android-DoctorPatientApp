package com.app.samplewearapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.widget.WearableRecyclerView
import com.app.samplewearapp.util.CustomViewType
import com.vdotok.connect.manager.WearSdkManager
import com.app.samplewearapp.util.CustomRecyclerAdapter
import com.vdotok.connect.manager.WearSdkManagerCallback
import java.util.ArrayList

class MainActivity :  FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider, WearSdkManagerCallback {

    private lateinit var manager: WearSdkManager

    private lateinit var mViewTypeData: ArrayList<CustomViewType.ViewTypeData>
    
    private var mWearableRecyclerView: WearableRecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mCustomRecyclerAdapter: CustomRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AmbientModeSupport.attach(this)

        initAdapterViews()

        checkAndRequestPermissions()

        initWearSdkManager()
    }

    private fun initWearSdkManager() {
        manager = WearSdkManager.getInstance(this)
    }

    private fun initAdapterViews() {
        mWearableRecyclerView = findViewById(R.id.recycler_view)
        mWearableRecyclerView?.isEdgeItemsCenteringEnabled = true


        mWearableRecyclerView?.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        mWearableRecyclerView?.layoutManager = mLayoutManager

        mViewTypeData = ArrayList()

        mViewTypeData.add(CustomViewType.TypeDefault())
        mViewTypeData.add(CustomViewType.TypeOptions())

        mCustomRecyclerAdapter = CustomRecyclerAdapter(mViewTypeData)
        mWearableRecyclerView?.adapter = mCustomRecyclerAdapter
    }


    private fun checkAndRequestPermissions() {
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION), 12345)
            }
            else
                requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 12345)
        }
    }


    fun onOptionButtonClicked(view: View) {
        when (view.id) {
            R.id.btn_send_list -> manager.sendSensorsListToPhone()
            R.id.btn_send_msg -> manager.sendMessageToPhone("")
            else -> {}
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return MyAmbientCallback()
    }

    private inner class MyAmbientCallback : AmbientModeSupport.AmbientCallback()



    override fun onMessageReceivedFromPhone(data: String) {
        runOnUiThread {
            Toast.makeText(this, data, Toast.LENGTH_LONG).show()
        }
    }
}