package com.example.producttracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : AppCompatActivity() {
    val INSERT_MODE = "INSERT_MODE"
    val DELETE_MODE = "DELETE_MODE"
    var mode: String = ""
    var scannedResult: String = ""
    private val client = OkHttpClient()
    var api_response : String = ""
    val url_hreoku_post = "https://shrouded-citadel-96744.herokuapp.com/api/contacts"
    val url_hreoku_delete_barcode = "https://shrouded-citadel-96744.herokuapp.com/api/contacts/delete/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan.setOnClickListener{
            run {
                mode = INSERT_MODE
                IntentIntegrator(this@MainActivity).initiateScan();
            }
        }
        btnDelete.setOnClickListener{
            run{
                mode = DELETE_MODE
                IntentIntegrator(this@MainActivity).initiateScan();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        var result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if(result != null){

            if(result.contents != null){
                scannedResult = result.contents

                if(mode == INSERT_MODE){
                    create_contact(url_hreoku_post, scannedResult)
                }else if (mode == DELETE_MODE){
                    delete_contact_by_barcode(url_hreoku_delete_barcode, scannedResult)
                }
                IntentIntegrator(this@MainActivity).initiateScan()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState?.let {
            scannedResult = it.getString("scannedResult").toString()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("scannedResult", scannedResult)
        super.onSaveInstanceState(outState)
    }

    fun post_request_api(url: String, message: String) {
        val requestBody = message.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .method("POST", requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                api_response = response.body?.string().toString()
            }
        })

    }

    fun create_contact(url:String, name: String){
        val contact = Contact(name, " ", 1)
        val gson = Gson()
        val request = gson.toJson(contact)
        post_request_api(url, request)
    }

    fun delete_contact_by_barcode(url:String, barcode: String){
        var result = "$url$barcode"
        delete_barcode_request_api(result)
    }

    private fun delete_barcode_request_api(result: String) {
        val request = Request.Builder()
            .url(result)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                api_response = response.body?.string().toString()
            }
        })
    }

}
