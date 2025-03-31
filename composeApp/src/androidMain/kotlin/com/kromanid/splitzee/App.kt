package com.kromanid.splitzee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App(mainViewModel: MainViewModel = viewModel()) {
    MaterialTheme {
        val greetings by mainViewModel.greetings.collectAsStateWithLifecycle()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(all = 20.dp),
            bottomBar = {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    onClick = {}
                ) {
                    Text("Add an expense")
                }
            }
        ) { paddings ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(modifier = Modifier.padding(paddingValues = paddings), text = greetings)
            }
        }
    }
}