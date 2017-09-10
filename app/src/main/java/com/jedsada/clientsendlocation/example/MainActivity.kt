package com.jedsada.clientsendlocation.example

import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LocationManager.LocationListener {

    private val dbRefDashboard: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("dashboard")
    }

    override fun onStart() {
        super.onStart()
        Dexter.withActivity(this)
                .withPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(permissionsListener)
                .withErrorListener(errorListener)
                .check()
    }

    override fun onStop() {
        super.onStop()
        LocationManager.removeLocationUpdate(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private val permissionsListener: MultiplePermissionsListener = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            when (hasDeniedPermission(report)) {
                false -> LocationManager.requestHighPowerLocationUpdate(this@MainActivity, this@MainActivity)
                else -> Snackbar.make(container, report.toString(), Snackbar.LENGTH_SHORT).show()
            }
        }

        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
            token?.continuePermissionRequest()
        }
    }

    private val errorListener = { _: DexterError ->
        // nothings
    }

    private fun hasDeniedPermission(report: MultiplePermissionsReport?): Boolean =
            report?.deniedPermissionResponses != null && !report.deniedPermissionResponses.isEmpty()

    private fun getDeviceId(): String? = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    override fun onLocationChanged(location: Location?) {
        location?.run {
            tvLocation.text = "${location.latitude} \t ${location.longitude}"
            val model = LocationModel(getDeviceId(), location)
            val deviceRoot = dbRefDashboard.child(String.format("deviceId:%s", model.deviceId))
            deviceRoot.setValue(model) { databaseError, _ ->
                when (databaseError != null) {
                    true -> tvLocation.text = databaseError.message
                }
            }
        }
    }

    override fun onLocationUnavailable() {
        // TODO : show error get location
    }
}