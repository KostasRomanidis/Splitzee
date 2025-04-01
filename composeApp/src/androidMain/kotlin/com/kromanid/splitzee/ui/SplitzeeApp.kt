package com.kromanid.splitzee.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun SplitzeeApp(
    text: String,
    onClick: () -> Unit
) {
    MaterialTheme {
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
                    onClick = { onClick.invoke() }
                ) {
                    Text("Add an expense")
                }
            }
        ) { paddings ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(paddingValues = paddings),
                    text = text
                )
            }
        }
    }
}