package dev.fredag.cheerwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.Composable
import androidx.compose.Recomposer
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.material.MaterialTheme


class FriendsFragment2 : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend, container, false)
        (view as ViewGroup).setContent(Recomposer.current()) {
            Hello("Jetpack Compose")
        }

        return view
    }
}

@Composable
fun Hello(name: String) {
    Column {
        Text("Hello")
        Text("World")
    }
}