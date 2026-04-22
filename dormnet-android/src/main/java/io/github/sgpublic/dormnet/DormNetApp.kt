package io.github.sgpublic.dormnet

import android.app.Application
import io.github.sgpublic.dormnet.core.registerContext

class DormNetApp : Application() {
    override fun onCreate() {
        super.onCreate()

        registerContext(this)
    }
}
