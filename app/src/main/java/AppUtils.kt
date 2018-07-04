import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.camilink.alexgps.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL

class AppUtils {

  companion object {

    val TAG = "LEL"

    fun checkInternet(c: Context): Boolean {
      val conMgr = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val netInfo = conMgr.getActiveNetworkInfo()
      if (netInfo != null && netInfo.isConnected()) {
        //Log.i("checkInternet", "netInfo connected");
        try {
          val urlc = (URL(c.getString(R.string.util_serverConnTestUrl)).openConnection()) as HttpURLConnection
          urlc.setRequestProperty("User-Agent", "Test")
          urlc.setRequestProperty("Connection", "close")
          urlc.setConnectTimeout(3000) //choose your own timeframe
          urlc.setReadTimeout(4000) //choose your own timeframe
          urlc.connect()
          //Log.i("checkInternet", "Response is " + (urlc.getResponseCode()) + "");
          return (urlc.getResponseCode() === 200)
        } catch (e: IOException) {
          //Log.i("checkInternet", "no Internet");
          return (false) //connectivity exists, but no internet.
        }
      } else {
        //Log.i("checkInternet", "netInfo NOT connected");
        return false
      }
    }

    fun makeUrlConnection(urls: String, postParams: String): String {
      var result = ""
      Log.d(TAG, "makeUrlConnection: URL:" + urls)
      Log.d(TAG, "makeUrlConnection: postParams: " + postParams)
      try {
        val urlToRequest = URL(urls)
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
        Log.d(TAG, "makeUrlConnection: resCode: " + urlConnection.getResponseCode())
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
      Log.d(TAG, "makeUrlConnection: result: " + result)
      return result
    }
  }
}