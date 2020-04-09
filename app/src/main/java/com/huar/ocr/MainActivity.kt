package com.huar.ocr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.msd.ocr.idcard.LibraryInitOCR
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    companion object{
        const val SCAN_ID_CARD_REQUEST = 1
    }

    private var imgFull = ""
    private var imgAvatar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            var bundle: Bundle? = null
            LibraryInitOCR.initOCR(this)
            bundle = Bundle()
            bundle.putBoolean("saveImage", true)
            bundle.putInt("requestCode", SCAN_ID_CARD_REQUEST)
            bundle.putInt("type", 0)
            bundle.putBoolean("showSelect", false)
            LibraryInitOCR.startScan(this@MainActivity, bundle)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SCAN_ID_CARD_REQUEST -> {
                    val result = data!!.getStringExtra("OCRResult")
                    try {
                        val jo = JSONObject(result!!)
                        if (jo.opt("type") == "1") {
                            val sb = StringBuffer()
                            mSuccessText2.text = ""
                            sb.append(String.format("姓名 = %s\n", jo.opt("name")))
                            sb.append(String.format("性别 = %s\n", jo.opt("sex")))
                            sb.append(String.format("民族 = %s\n", jo.opt("folk")))
                            sb.append(String.format("日期 = %s\n", jo.opt("birt")))
                            sb.append(String.format("号码 = %s\n", jo.opt("num")))
                            sb.append(String.format("住址 = %s\n", jo.opt("addr")))
                            imgAvatar = jo.opt("headPath")!!.toString()
                            imgFull = jo.opt("imgPath")!!.toString()
                            Glide.with(this).load(imgAvatar).into(im_avatar)
                            Glide.with(this).load(imgFull).into(im_full)
                            mSuccessText1.text = sb.toString()
                        } else {
                            val sb = StringBuffer()
                            sb.append(String.format("签发机关 = %s\n", jo.opt("issue")))
                            sb.append(String.format("有效期限 = %s\n", jo.opt("valid")))
                            mSuccessText2.text = sb.toString()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}
