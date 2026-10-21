package com.kromanid.splitzee.di

import org.koin.core.module.Module

/** Provides the platform `SqlDriver` and the `SplitzeeDatabase` built on it. */
expect val platformModule: Module
