package com.airmouse3d

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point wiring up the Hilt dependency graph for the whole app. */
@HiltAndroidApp
class AirMouseApplication : Application()
