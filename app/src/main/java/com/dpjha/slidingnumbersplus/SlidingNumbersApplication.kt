package com.dpjha.slidingnumbersplus

import android.app.Application
import com.dpjha.slidingnumbersplus.services.PlayGamesService

class SlidingNumbersApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlayGamesService.start(this)
    }
}
