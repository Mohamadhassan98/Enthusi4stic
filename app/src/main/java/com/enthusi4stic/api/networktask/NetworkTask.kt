package com.enthusi4stic.api.networktask

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import com.beust.klaxon.Klaxon
import com.enthusi4stic.api.progressdialog.CircularProgressBarDialog
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import org.intellij.lang.annotations.Language
import java.lang.Exception

fun Any.toJson(): String = Klaxon().toJsonString(this)

enum class MediaType(private val mimeType: String) {
    JSON("application/json"),
    PlainText("text/plain"),
    FormMultipart("multipart/form-data");
    fun encode(encoder: String = "utf-8") = "${this.mimeType}; charset=$encoder".toMediaType()
}

val String.jsonRequestBody get() = this.toRequestBody(MediaType.JSON.encode())

val Map<String, String>.formBody get() =
    FormBody.Builder().also {
        this.forEach { (key, value) ->
            it.add(key, value)
        }
    }.build()

fun formBody(vararg p0: Pair<String, String>) =
    FormBody.Builder().also {
        p0.forEach { it2 ->
            it.add(it2.first, it2.second)
        }
    }.build()

@Language("kotlin")
private const val multiPartBodyExample = """
    val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", "value")
            .addFormDataPart(
                "avatar", "avatar.jpeg",
                imageFile.toRequestBody(MediaType.FormMultiPart.encode()))
            .build()
"""

open class NetworkTask(beautiful horizontal progress
    val url: String,
    val method: Method = Method.GET,
    protected val body: RequestBody? = null,
    protected val ctx: Context? = null,
    val waitingMessage: String? = null
) {
    enum class Method {
        GET, POST, PUT, PATCH, DELETE, HEAD
    }

    init {
        when (method) {
            Method.GET, Method.HEAD -> {
                if (body != null) {
                    throw IllegalArgumentException("method ${method.name} cannot have body.")
                }
            }
            Method.PATCH, Method.PUT, Method.POST -> {
                if (body == null) {
                    throw IllegalArgumentException("method ${method.name} must have a body.")
                }
            }
        }
    }

    private var onCallBack: (Response?) -> Unit = {}

    fun setOnCallBack(callback: (Response?) -> Unit): NetworkTask {
        onCallBack = callback
        return this
    }

    fun send() {
        Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    inner class Task: AsyncTask<RequestBody, Unit, Response>() {

        var progressDialog: CircularProgressBarDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            ctx?.let {
                progressDialog = CircularProgressBarDialog(it, waitingMessage ?: "Please wait...")
                progressDialog?.show()
            }
        }

        override fun doInBackground(vararg params: RequestBody?) = try {
            val request = Request.Builder().url(url).let {
                when (method) {
                    Method.GET -> it.get()
                    Method.POST -> it.post(body!!)
                    Method.DELETE -> it.delete(body ?: EMPTY_REQUEST)
                    Method.PUT -> it.put(body!!)
                    Method.PATCH -> it.patch(body!!)
                    Method.HEAD -> it.head()
                }
            }.build()
            OkHttpClient().newCall(request).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        override fun onPostExecute(result: Response?) {
            super.onPostExecute(result)
            progressDialog?.dismiss()
            onCallBack(result)
        }
    }
}