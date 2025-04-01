package com.kromanid.splitzee

import android.app.Application
import com.kromanid.splitzee.di.androidModules
import com.kromanid.splitzee.di.initKoin
import org.koin.android.ext.koin.androidContext


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@App)
            modules(androidModules)
        }
    }


}