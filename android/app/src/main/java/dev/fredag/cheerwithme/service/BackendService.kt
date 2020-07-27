package dev.fredag.cheerwithme.service

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.fasterxml.jackson.databind.ObjectMapper

class BackendService constructor(context: Context) {
    private val backendUrl = "http://192.168.0.113:8080/"
    companion object {
        var token: String = ""
        @Volatile
        private var INSTANCE: BackendService? = null
        var context: Context? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: BackendService(context).also {
                    INSTANCE = it
                    this.context = context
                }
            }
    }

    fun <R> get(path: String, klass: Class<R>, callback: (Result<R>) -> Unit) {
        val objectMapper = AppIOSingleton.getInstance(context!!).objectMapper
        val url = backendUrl + path
        val request = object : StringRequest(
            Method.GET,
            url,
            successListener(path, objectMapper, klass, callback),
            errorListener(callback)
        ) {
            override fun getHeaders(): MutableMap<String, String> = getAppHeaders()
            //@Throws(AuthFailureError::class)
            //override fun getHeaders(): Map<String, String> = getAppHeaders()
        }
        AppIOSingleton.getInstance(context!!).addToRequestQueue(request)
    }

    fun <R> post(path: String, body: Any, klass: Class<R>, callback: (Result<R>) -> Unit) {
        val objectMapper = AppIOSingleton.getInstance(context!!).objectMapper
        val url = backendUrl + path
        val request = object : StringRequest(
            Method.POST,
            url,
            successListener(path, objectMapper, klass, callback),
            errorListener(callback)
        ) {
            //@Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> = getAppHeaders()
            override fun getBody(): ByteArray = objectMapper.writeValueAsBytes(body)
        }
        AppIOSingleton.getInstance(context!!).addToRequestQueue(request)
    }

    private fun getAppHeaders(): MutableMap<String, String> = mutableMapOf(
        "Authorization" to "Bearer $token",
        "Content-Type" to "application/json"
    )

    private fun <R> errorListener(callback: (Result<R>) -> Unit): Response.ErrorListener {
        return Response.ErrorListener { error ->
            Log.d("BackendService", "Failed with error $error")
            callback(Result.failure(error))
        }
    }

    private fun <R> successListener(
        path: String,
        objectMapper: ObjectMapper,
        klass: Class<R>,
        callback: (Result<R>) -> Unit
    ): Response.Listener<String> {
        return Response.Listener { response ->
            Log.d("BackendService", "path: $path response: $response")
            val b: R = objectMapper.readValue(response, klass)
            callback(Result.success(b))
        }
    }
}