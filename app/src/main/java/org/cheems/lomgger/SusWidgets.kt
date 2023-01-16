package org.cheems.lomgger

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
////    LomggerTheme { Greeting("Android") }
//}

@Composable
fun AmogusTextField (textState: MutableState<TextFieldValue>, sus: String) {
    TextField(
        value = textState.value,
        placeholder = { Text(sus) },
        onValueChange = { textState.value = it }
    )
}

@Composable
fun AmogusButton (onClick: () -> Unit) {
    Button(
        onClick = onClick
    ) {
        Text("Save")
    }
}