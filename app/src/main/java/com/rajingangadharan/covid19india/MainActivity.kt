package com.rajingangadharan.covid19india

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.razerdp.widget.animatedpieview.AnimatedPieView
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig
import com.razerdp.widget.animatedpieview.data.SimplePieInfo
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var confirm: String? = null
    private var active: String? = null
    private var recover: String? = null
    private var dead: String? = null
    private var update: String? = null
    private var deltaCon: String? = null
    private var deltaRec: String? = null
    private var deltaDead: String? = null
    private var txtConfirm: MaterialTextView? = null
    private var txtActive: MaterialTextView? = null
    private var txtRecover: MaterialTextView? = null
    private var txtDead: MaterialTextView? = null
    private var txtUpdate: MaterialTextView? = null
    private var txtDeltaCon: MaterialTextView? = null
    private var txtDeltaRec: MaterialTextView? = null
    private var txtDeltaDead: MaterialTextView? = null
    private var txtProgress: MaterialTextView? = null
    private var pieView: AnimatedPieView? = null
    private var loadProgress: ProgressBar? = null
    var requestQueue: RequestQueue? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.refresh) {
            if (isConnected()) {
                preFetch()
                fetchTotal()
            } else {
                Toast.makeText(applicationContext, "Please Connect To Network", Toast.LENGTH_LONG).show()
            }
            return true
        } else if (item.itemId == R.id.about) {
            val intent = Intent(this@MainActivity, AboutActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_text)

        requestQueue = Volley.newRequestQueue(this)
        txtConfirm = findViewById(R.id.txtConfirm)
        txtActive = findViewById(R.id.txtActive)
        txtRecover = findViewById(R.id.txtRecovered)
        txtDead = findViewById(R.id.txtDead)
        pieView = findViewById(R.id.pieView)
        loadProgress = findViewById(R.id.loadProgress)
        txtUpdate = findViewById(R.id.txtUpdate)
        txtDeltaCon = findViewById(R.id.txtDeltaCon)
        txtDeltaRec = findViewById(R.id.txtDeltaRec)
        txtDeltaDead = findViewById(R.id.txtDeltaDead)
        txtProgress = findViewById(R.id.txtProgress)
        val btnState = findViewById<MaterialButton>(R.id.btnState)
        val btnDay = findViewById<MaterialButton>(R.id.btnDay)

        if (isConnected()) {
            preFetch()
            fetchTotal()
        } else {
            Toast.makeText(applicationContext, "Please Connect To Network", Toast.LENGTH_LONG).show()
        }

        btnState.setOnClickListener {
            val intent = Intent(this@MainActivity, StateWise::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnDay.setOnClickListener {
            val intent = Intent(this@MainActivity, DayWise::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        if(capabilities != null) {
            when {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                } else {
                    TODO("VERSION.SDK_INT < LOLLIPOP")
                } -> {
                    return true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                } else {
                    TODO("VERSION.SDK_INT < LOLLIPOP")
                } -> {
                    return true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } else {
                    TODO("VERSION.SDK_INT < LOLLIPOP")
                } -> {
                    return true
                }
            }
        }
        return false
    }

    private fun preFetch() {
        confirm = ""
        active = ""
        recover = ""
        dead = ""
        update = ""
        deltaCon = ""
        deltaRec = ""
        deltaDead = ""
        requestQueue = Volley.newRequestQueue(this)
        loadProgress?.visibility = View.VISIBLE
        txtProgress?.visibility = View.VISIBLE
    }

    private fun fetchTotal() {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, "https://api.covid19india.org/data.json", null, Response.Listener { response: JSONObject ->
            try {
                val stateWise = response.getJSONArray("statewise")
                for (i in 0 until stateWise.length()) {
                    val jo = stateWise.getJSONObject(i)
                    if (jo["state"] == "Total") {
                        confirm = jo.getString("confirmed")
                        active = jo.getString("active")
                        recover = jo.getString("recovered")
                        dead = jo.getString("deaths")
                        val formatIn = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        val formatOut = SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.getDefault())
                        val date = formatIn.parse(jo.getString("lastupdatedtime"))!!
                        update = "Last Updated: " + formatOut.format(date)
                        deltaCon = jo.getString("deltaconfirmed")
                        deltaRec = jo.getString("deltarecovered")
                        deltaDead = jo.getString("deltadeaths")
                        break
                    }
                }
                val decimalFormat = DecimalFormat("##,##,###")

                val animCon = confirm?.toInt()?.let { ValueAnimator.ofInt(0, it) }
                animCon?.duration = 1000
                animCon?.addUpdateListener { animation: ValueAnimator ->
                    val ani = decimalFormat.format(animation.animatedValue.toString().toInt().toLong())
                    txtConfirm?.text = ani
                }
                animCon?.start()

                val animAct = active?.toInt()?.let { ValueAnimator.ofInt(0, it) }
                animAct?.duration = 1000
                animAct?.addUpdateListener { animation: ValueAnimator ->
                    val ani = decimalFormat.format(animation.animatedValue.toString().toInt().toLong())
                    txtActive?.text = ani
                }
                animAct?.start()

                val animRec = recover?.toInt()?.let { ValueAnimator.ofInt(0, it) }
                animRec?.duration = 1000
                animRec?.addUpdateListener { animation: ValueAnimator ->
                    val ani = decimalFormat.format(animation.animatedValue.toString().toInt().toLong())
                    txtRecover?.text = ani
                }
                animRec?.start()

                val animDead = dead?.toInt()?.let { ValueAnimator.ofInt(0, it) }
                animDead?.duration = 1000
                animDead?.addUpdateListener { animation: ValueAnimator ->
                    val ani = decimalFormat.format(animation.animatedValue.toString().toInt().toLong())
                    txtDead?.text = ani
                }
                animDead?.start()

                txtUpdate?.text = update

                val animDeltaCon = deltaCon?.toInt()?.let { ValueAnimator.ofInt(0, it) }
                animDeltaCon?.duration = 1000
                animDeltaCon?.addUpdateListener { animation: ValueAnimator ->
                    val ani = "\u2191" + decimalFormat.format(animation.animatedValue.toString().toInt().toLong())
                    txtDeltaCon?.text = ani
                }
                animDeltaCon?.start()

                val animDeltaRec = deltaRec?.toInt()?.let { ValueAnimator.ofInt(0, it) }
                animDeltaRec?.duration = 1000
                animDeltaRec?.addUpdateListener { animation: ValueAnimator ->
                    val ani = "\u2191" + decimalFormat.format(animation.animatedValue.toString().toInt().toLong())
                    txtDeltaRec?.text = ani
                }
                animDeltaRec?.start()

                val animDeltaDead = deltaDead?.toInt()?.let { ValueAnimator.ofInt(0, it) }
                animDeltaDead?.duration = 1000
                animDeltaDead?.addUpdateListener { animation: ValueAnimator ->
                    val ani = "\u2191" + decimalFormat.format(animation.animatedValue.toString().toInt().toLong())
                    txtDeltaDead?.text = ani
                }
                animDeltaDead?.start()

                val config = AnimatedPieViewConfig()
                config.startAngle(-90f)
                config.splitAngle(1f)
                active?.toFloat()?.toDouble()?.let { SimplePieInfo(it, ContextCompat.getColor(applicationContext, R.color.colorRed), "Active") }?.let { config.addData(it) }
                recover?.toFloat()?.toDouble()?.let { SimplePieInfo(it, ContextCompat.getColor(applicationContext, R.color.colorGreen), "Recovered") }?.let { config.addData(it) }
                dead?.toFloat()?.toDouble()?.let { SimplePieInfo(it, ContextCompat.getColor(applicationContext, R.color.colorYellow), "Deceased") }?.let { config.addData(it) }
                config.drawText(true)
                config.textSize(20f)
                config.canTouch(false)
                config.strokeMode(false)
                config.duration(1000)
                pieView?.applyConfig(config)
                pieView?.start()
                loadProgress?.visibility = View.INVISIBLE
                txtProgress?.visibility = View.INVISIBLE
            } catch (e: JSONException) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            } catch (e: ParseException) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
        }, Response.ErrorListener { error: VolleyError -> Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show() })
        requestQueue?.add(jsonObjectRequest)
        requestQueue?.cache?.clear()
    }
}