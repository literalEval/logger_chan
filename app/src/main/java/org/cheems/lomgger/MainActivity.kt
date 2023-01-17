package org.cheems.lomgger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cheems.lomgger.ui.theme.LoggerChanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val susServices = SusServices(LocalContext.current)
            val creds = susServices.getCreds()

            val userTextState = remember {
                mutableStateOf(
                    TextFieldValue(creds?.elementAt(0) ?: "")
                )
            }
            val passTextState = remember {
                mutableStateOf(
                    TextFieldValue(creds?.elementAt(1) ?: "")
                )
            }

            LoggerChanTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                         TopAppBar (
                             title = {
                                 Text("Logger Chan")
                             },
                             actions = {
                                 IconButton(
                                     onClick = {
                                         GlobalScope.launch {
                                             susServices.tryLogin()
                                         }
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
                    backgroundColor = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val context = LocalContext.current

                        Image(
                            modifier = Modifier.padding(vertical = 4.dp),
                            painter = painterResource(id = R.drawable.sus),
                            contentDescription = "amogus_icon"
                        )

                        AmogusTextField(userTextState, "User Name")
                        AmogusTextField(passTextState, "Password")

                        AmogusButton(
                            onClick = {
                                susServices.saveCreds(
                                    context,
                                    userTextState.value.text,
                                    passTextState.value.text,
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


