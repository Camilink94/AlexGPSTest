package com.camilink.alexgps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import android.location.LocationManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.telephony.TelephonyManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

  companion object {
    const val MY_PERMISSIONS_REQUEST_FINE_LOCATION_AND_PHONE = 1
    const val TAG = "Acc"
  }

  var url: String = ""
  var httpProt: String = ""
  var port: Int = 0
  lateinit var mContext: MainActivity

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    mContext = this@MainActivity

    val httpArr = arrayOf("http://", "https://")
    val httAdapter = ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, httpArr)
    httAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
    urlSp.adapter = httAdapter

  }

  fun onSendInfoClk(v: View?) {
    onSendInfoClk()
  }

  @AfterPermissionGranted(MY_PERMISSIONS_REQUEST_FINE_LOCATION_AND_PHONE)
  fun onSendInfoClk() {
    val perms = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION)
    Log.d(TAG, "Granted: " + (ContextCompat.checkSelfPermission(mContext, perms[0]) == PackageManager.PERMISSION_GRANTED))
    if (EasyPermissions.hasPermissions(this, *perms)) {
      Log.d(TAG, "PermGranted... Sending")
      sendInfo()
    } else {
      Log.d(TAG, "PermDenied... Asking")
      EasyPermissions.requestPermissions(this, "Por favor añada los permisos",
          MY_PERMISSIONS_REQUEST_FINE_LOCATION_AND_PHONE, *perms)
    }
  }

  @SuppressLint("StaticFieldLeak")
  fun sendInfo() {
    url = urlTiet.text.toString()
    if (url != "") {
      httpProt = urlSp.selectedItem.toString()
      port = portTiet.text.toString().toInt()

      object : AsyncTask<Void, Int, Void?>() {

        var thereIsInternet: Boolean = false
        var postParams = ""
        var resCode = 0
        var result = ""

        override fun onPreExecute() {
          super.onPreExecute()

          loadingTv.text = getText(R.string.util_checkInternet)
          dataLl.visibility = View.GONE
          loadingLl.visibility = View.VISIBLE
        }

        override fun onProgressUpdate(vararg values: Int?) {
          super.onProgressUpdate(*values)
          if (values[0] == 1) {
            loadingTv.text = "Enviando información..."
          } else if (values[0] == 2) {
            postParamsEt.setText(postParams)
          }
        }

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg p0: Void): Void? {
          thereIsInternet = AppUtils.checkInternet(mContext)
          if (thereIsInternet) {
            publishProgress(1)

            var imei = ""
            var phoneNumber = ""
            val mTelephony = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (mTelephony.deviceId != null) {
              imei = mTelephony.deviceId
              phoneNumber = mTelephony.line1Number ?: ""
            }

            var keyword = "001"

            val timestamp1 = SimpleDateFormat("yyMMddhhmmss").format(Date())
            val timestamp2 = ""

            val gpsActive = "F"//"L" if not active
            val gpsActive2 = if (gpsActive == "F") "A" else "V"

            val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider = LocationManager.NETWORK_PROVIDER
            val lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)

            val latVal = Math.abs(lastKnownLocation?.latitude ?: 0.toDouble())
            val latOri = if (latVal >= 0) "N" else "S"
            val lonVal = Math.abs(lastKnownLocation?.longitude ?: 0.toDouble())
            val lonOri = if (lonVal >= 0) "E" else "W"
            val speed = lastKnownLocation?.speed ?: 0.toFloat()
            val direction = "0"
            val altitude = lastKnownLocation?.altitude ?: 0.toDouble()

            val accState = ""
            val doorState = ""
            val oilState1 = ""
            val oilState2 = ""
            val tempState = ""

            postParams = "imei:" + imei +
                "," + keyword +
                "," + timestamp1 +
                "," + phoneNumber +
                "," + gpsActive +
                "," + timestamp2 +
                "," + gpsActive2 +
                "," + latVal +
                "," + latOri +
                "," + lonVal +
                "," + lonOri +
                "," + speed +
                "," + direction +
                "," + altitude +
                "," + accState +
                "," + doorState +
                "," + oilState1 +
                "," + oilState2 +
                "," + tempState + ";"
            Log.d(TAG, postParams)

            publishProgress(2)

            val s = DatagramSocket()
            val local = InetAddress.getByName(
                //httpProt +
                    url)
            val msg_length = postParams.length
            val message = postParams.toByteArray()
            val p = DatagramPacket(message, msg_length, local, port)
            s.send(p)

          }
          return null
        }

        override fun onPostExecute(resu: Void?) {
          super.onPostExecute(resu)

          resCodeEt.setText("" + resCode)
          responseEt.setText(result)

          dataLl.visibility = View.VISIBLE
          loadingLl.visibility = View.GONE
        }
      }.execute()

    } else {
      urlTiet.error = "Ingrese una URL"
    }
  }
}
