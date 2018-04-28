package com.corral.mityc

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class SplashActivity : Activity() {


        override fun onCreate(icicle : Bundle?) {
            super.onCreate(icicle)

            val intent = Intent(this, MitycRubi::class.java)
            startActivity (intent)
            finish()
        }
}