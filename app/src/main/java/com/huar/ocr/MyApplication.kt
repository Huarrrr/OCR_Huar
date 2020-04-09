package com.huar.ocr

import android.app.Application
import com.msd.ocr.idcard.LibraryInitOCR


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        myApplication = this
        LibraryInitOCR.initOCR(context)
    }

    companion object {
        private var myApplication: MyApplication? = null
        val context: Application?
            get() = myApplication
    }
}