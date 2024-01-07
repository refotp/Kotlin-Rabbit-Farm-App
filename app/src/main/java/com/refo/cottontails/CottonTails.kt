package com.refo.cottontails

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class CottonTails : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseDatabase.getInstance().purgeOutstandingWrites()
    }
}