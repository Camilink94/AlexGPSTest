package com.camilink.alexgps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import android.location.LocationManager
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

  companion object {
    const val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1
    const val TAG = "Acc"
  }

  var url: String = ""
  lateinit var mContext: MainActivity

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    mContext = this@MainActivity

    val httpArr = arrayOf("http://", "https://")
    val httAdapter = ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, httpArr)
    httAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    urlSp.adapter = httAdapter

  }

  fun onSendInfoClk(v: View?) {
    onSendInfoClk()
  }

  @AfterPermissionGranted(MY_PERMISSIONS_REQUEST_FINE_LOCATION)
  fun onSendInfoClk() {
    val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    if (EasyPermissions.hasPermissions(this, *perms)) {
      Log.d(TAG,"PermGranted... Sending")
      sendInfo()
    } else {
      Log.d(TAG,"PermDenied... Asking")
      EasyPermissions.requestPermissions(this, "Debe dar permiso para ubicación",
          MY_PERMISSIONS_REQUEST_FINE_LOCATION, *perms)
    }
  }

  @SuppressLint("StaticFieldLeak")
  fun sendInfo() {
    url = urlTiet.text.toString()
    if (!url.equals("")) {

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
            val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationProvider = LocationManager.NETWORK_PROVIDER
            val lastKnownLocation = locationManager.getLastKnownLocation(locationProvider)
            postParams = "lat=" + lastKnownLocation.latitude + "&long=" + lastKnownLocation.longitude
            publishProgress(2)

            try {
              val urlToRequest = URL(url)
              val urlConnection = urlToRequest.openConnection() as HttpURLConnection
              urlConnection.setDoOutput(true)
              urlConnection.setRequestMethod("POST")
              //urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
              //urlConnection.setRequestProperty("Content-Type", "application/form-data");
              //urlConnection.setRequestProperty("Content-Type", "application/json");
              urlConnection.setConnectTimeout(30000)
              urlConnection.setReadTimeout(30000)
              urlConnection.setFixedLengthStreamingMode(
                  postParams.toByteArray().size)
              val out = PrintWriter(urlConnection.getOutputStream())
              out.print(postParams)
              out.close()
              resCode = urlConnection.responseCode
              val inp = BufferedReader(
                  InputStreamReader(urlConnection.getInputStream()))
              var inputLine: String
              val response = StringBuilder()
              inputLine = inp.readLine()
              while (inputLine != null) {
                response.append(inputLine)
                inputLine = inp.readLine()
              }
              inp.close()
              //print result
              result = response.toString()
            } catch (e: IOException) {
              e.printStackTrace()
            }

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
