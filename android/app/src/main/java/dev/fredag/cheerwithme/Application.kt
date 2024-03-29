package dev.fredag.cheerwithme

import android.app.Application
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.MapboxSearchSdk
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        MapboxSearchSdk.initialize(
            application = this,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        )
    }
}
