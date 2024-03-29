package dev.fredag.cheerwithme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fredag.cheerwithme.data.UserRepository
import dev.fredag.cheerwithme.data.UserState
import dev.fredag.cheerwithme.ui.CheerWithMeTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_login, container, false) as ViewGroup
// TODO
//        fragmentView.setContent(Recomposer.current()) {
//            Login(this)
//        }

        return fragmentView
    }
}


private fun createGoogleLoginIntent(context: Context): Intent {
    val oauthServerClientId = context.getString(R.string.oauth_server_client_id)
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(oauthServerClientId)
        .requestProfile()
        .requestServerAuthCode(oauthServerClientId)
        .requestEmail()
        .build()
    val mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    val signInIntent = mGoogleSignInClient.signInIntent
    return signInIntent
}

@Composable
fun Login(
    loginViewModel: LoginViewModel,
    loginSuccess: () -> Unit,
    loginFailed: () -> Unit,
) = CheerWithMeTheme {

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            loginViewModel.handleLoginResult(loginSuccess, loginFailed)
        )
    val context = LocalContext.current


    Surface {
        Column(modifier = Modifier.padding(20.dp, 30.dp)) {
            Text(
                text = "Welcome",
                fontSize = 8.em,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_cheer_with_me),
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentDescription = ""
                )
                Text(
                    "Cheer With Me is a social app that requires you to have an account in order to connect with friends and share happenings.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp, 40.dp)
                )
                Button(
                    onClick = {
                        Log.d("Login", "ButtonClicked!")
                        launcher.launch(createGoogleLoginIntent(context))
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        disabledBackgroundColor = Color.Gray
                    ),
                    //padding = InnerPadding (8.dp, 0.dp, 8.dp, 0.dp), //TODO
                    enabled = !loginViewModel.loading.value
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_btn_google_light_normal),
                        modifier = Modifier.size(50.dp),
                        contentDescription = ""
                    )
                    Text(
                        "Sign in with Google",
                        color = Color.DarkGray,
                        //fontFamily = fontFamily(listOf(font(R.font.roboto_medium))), // TODO
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 0.dp)
                    )
                }
                if (loginViewModel.loading.value) {
                    CircularProgressIndicator(
                        Modifier.padding(0.dp, 24.dp)
                    )
                }
            }
        }
    }
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    val loading = mutableStateOf(false)

    fun handleLoginResult(loginSuccess: () -> Unit, loginFailed: () -> Unit): (ActivityResult) -> Unit {
        return inner@{
            Log.d("Login", "Got login result: ${it.resultCode}")
            Log.d("Login", "Got login result: $it")
            val bundle = it.data?.extras
            if (bundle != null) {
                for (key in bundle.keySet()) {
                    Log.d("Login", key + " : " + if (bundle[key] != null) bundle[key] else "NULL")
                }

            }
            if (it.resultCode == Activity.RESULT_OK) {
                val account = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    .getResult(ApiException::class.java)
                if (account == null) {
                    Log.d("Login", "Failed to sign in")
                    return@inner
                }
                Log.d("Login", "Granted scopes ${account.grantedScopes}")
                Log.d("Login", "Requested scopes ${account.requestedScopes}")
                Log.d(
                    "Login",
                    "${account.displayName} ${account.email} '${account.grantedScopes}' ${account.id} ${account.idToken} ${account.serverAuthCode}"
                )
                handleLoginSuccess(account, loginSuccess)
            } else {
                loginFailed()
                loading.value = false
            }

        }
    }


    private fun handleLoginSuccess(
        account: GoogleSignInAccount,
        loginSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            userRepository.loginWithGoogle(account.serverAuthCode!!, account.idToken!!)
            Log.d("Login", "Login with google complete")

        }.invokeOnCompletion {
            loading.value = false
            if (it !== null) {
                TODO("Error handle")
            }
            UserState.loggedIn.postValue(true)
            //val navController = Navigation.findNavController(fragment.requireView())
            // TODO navigate to home, login done
            loginSuccess()
            // navController.navigate(navController.graph.startDestination) TODO
        }


    }

}
