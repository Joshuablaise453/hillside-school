package com.electrohub.schoolmanagementapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.auth.FirebaseAuth

class LogoutService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        
        // Log out the user when the app is swiped away from recent tasks
        FirebaseAuth.getInstance().signOut()
        
        // Stop the service so it doesn't linger
        stopSelf()
    }
}