package dev.fredag.cheerwithme

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.iid.FirebaseInstanceId
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: FragmentActivity() {
    private var currentNavigationId = -1
    val fragmentManager = supportFragmentManager

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        if (currentNavigationId == item.itemId) {
            return@OnNavigationItemSelectedListener false
        }
        currentNavigationId = item.itemId

        lateinit var switchToFragment: Fragment
        var retValue = false
        when (item.itemId) {
            R.id.navigation_cheer -> {
                switchToFragment = CheerViewFragment()
                retValue = true
            }
            R.id.navigation_map -> {
                switchToFragment = MapViewFragment()
                retValue = true
            }
            R.id.navigation_calendar -> {
                switchToFragment = CheerViewFragment()
                retValue = true
            }
            R.id.navigation_friends -> {
                Log.d("App", "Switching to friends fragment")
                switchToFragment = FriendsFragment()
                retValue = true
            }
            R.id.navigation_profile -> {
                switchToFragment = FriendsFragment2()
                retValue = true
            }
        }
        invalidateOptionsMenu();

        val fragmentTransaction = fragmentManager.beginTransaction()
            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        fragmentTransaction.replace(R.id.fragment_container, switchToFragment)
        fragmentTransaction.commit()

        retValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app)

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, CheerViewFragment())
        fragmentTransaction.commit()

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)


        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            Log.d("MAIN_STUFF", "STUFFFF" + it.result?.token)
        }

        startKoin {
            // Android context
            androidContext(this@App)
            // modules
            modules(notificationModule)
        }
    }
}


