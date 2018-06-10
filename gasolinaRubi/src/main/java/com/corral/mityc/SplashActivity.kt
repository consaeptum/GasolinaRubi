package com.corral.mityc

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class SplashActivity : Activity() {


        override fun onCreate(icicle : Bundle?) {
            super.onCreate(icicle)

            /*
             * Damos valor a Constantes.DEBUG
             * Nos será útil para la clase MLog, donde mostramos log, sólo si
             * estamos depurando código.
             */
            val compileMode = getString(R.string.COMPILE_MODE)
            Constantes.DEBUG = !compileMode.equals("release", ignoreCase = true)

            val intent = Intent(this, MitycRubi::class.java)
            startActivity (intent)
            finish()
        }
}