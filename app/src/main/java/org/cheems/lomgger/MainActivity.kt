package org.cheems.lomgger

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.cheems.lomgger.ui.theme.LoggerChanTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val amogusViewModel = ViewModelProvider(this).get<AmogusViewModel>()

        setContent {

            amogusViewModel.init(LocalContext.current)
            val creds = amogusViewModel.getCreds()

            val userTextState = remember {
                mutableStateOf(
                    TextFieldValue(creds.elementAt(0) ?: "")
                )
            }
            val passTextState = remember {
                mutableStateOf(
                    TextFieldValue(creds.elementAt(1) ?: "")
                )
            }

            LoggerChanTheme {

                val scaffoldState = rememberScaffoldState()

                LaunchedEffect(Unit) {
                    amogusViewModel.isMessageShownFlow.collectLatest {
                        println("amogus")
//                        if (it) {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = it,
                                duration = SnackbarDuration.Short
                            )
//                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    scaffoldState = scaffoldState,
                    topBar = {
                         TopAppBar (
                             title = {
                                 Text("Logger Chan")
                             },
                             actions = {
                                 IconButton(
                                     onClick = {
                                         amogusViewModel.openRepo()
                                     }
                                 ) {
                                     Icon(
                                         Icons.Filled.Favorite,
                                         null
                                     )
                                 }
                             }
                         )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = {
                                IconButton(
                                    onClick = {
                                        GlobalScope.launch {
                                            amogusViewModel.tryLogin()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, "Login Button")
                                }
                                IconButton(
                                    onClick = {
                                        GlobalScope.launch {
                                            amogusViewModel.tryLogout()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.Close, "Logout Button")
                                }
                                IconButton(
                                    onClick = {
                                        amogusViewModel.tryConnectWifi()
                                    }
                                ) {
                                    Icon(Icons.Rounded.Build, "Wifi Button")
                                }
                            },
                            onClick = { /*TODO*/ }
                        )
                    },
                    backgroundColor = MaterialTheme.colors.background
                ) {
                    if (amogusViewModel.shouldShowSnackBar) {
                        Snackbar (
                            modifier = Modifier.background(color = Color.Red)
                                ) {
                            Text("boi")
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        Image(
                            modifier = Modifier.padding(vertical = 4.dp),
                            painter = painterResource(id = R.drawable.sus),
                            contentDescription = "amogus_icon"
                        )

                        AmogusTextField(userTextState, "User Name")
                        AmogusTextField(passTextState, "Password")

                        AmogusButton(
                            onClick = {
                                amogusViewModel.saveCreds(
                                    userTextState.value.text,
                                    passTextState.value.text,
                                )
                            }
                        )

                        AmogusButton(
                            onClick = {
                                amogusViewModel.tryConnectWifi()
                            }
                        )
                    }
                }
            }
        }
    }
}


