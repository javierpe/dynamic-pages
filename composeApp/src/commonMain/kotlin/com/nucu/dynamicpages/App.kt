package com.nucu.dynamicpages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nucu.dynamicpages.data.mappers.HomeContactResponseMapper
import com.nucu.dynamicpages.di.HomeDynamicPagesModule
import com.nucu.dynamicpages.test.model.ContactResponse
import com.nucu.dynamicpages.test.model.PhoneResponse
import com.nucu.dynamicpages.test.rule.MainModule
import com.nucu.dynamicpages.test.rule.NameRule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.ksp.generated.module

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinApplication(
            application = {
                modules(
                    MainModule().module,
                    HomeDynamicPagesModule().module
                )
            }
        ) {
            AppContent(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val rule = koinInject<NameRule>()
    val mapper = koinInject<HomeContactResponseMapper>()

    var showContent by remember { mutableStateOf(false) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showContent = !showContent }) {
            Text("Click me!")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { mutableStateOf("Loading...") }
            LaunchedEffect(Unit) {
                greeting.value = mapper.mapToPhoneUi(
                    contactResponse = ContactResponse(
                        name = "Francisco",
                        phone = PhoneResponse(
                            country = "Mexico"
                        )
                    )
                ).toString()
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Compose: ${greeting.value}")
            }
        }
    }
}
