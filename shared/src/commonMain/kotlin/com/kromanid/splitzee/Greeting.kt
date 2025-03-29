package com.kromanid.splitzee

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

class Greeting {
    private val platform = getPlatform()
    private val rocketComponent = RocketComponent()

    @NativeCoroutines
    fun greet(): Flow<String> = flow {
        emit("Hello, ${platform.name}.")
        delay(1.seconds)
        emit("Your platforms number is $num!")
        delay(1.seconds)
        emit(daysPhase())
        delay(1.seconds)
        emit(rocketComponent.launchPhase())
    }
}