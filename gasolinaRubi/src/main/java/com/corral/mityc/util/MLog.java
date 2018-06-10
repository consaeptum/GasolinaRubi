package com.corral.mityc.util;

import com.corral.mityc.Constantes;

public class MLog {

        public static void i(String tag, String msg) {

            if (Constantes.DEBUG) {
                android.util.Log.i(tag, msg);
            }
        }

        public static void e(String tag, String msg) {
            if (Constantes.DEBUG) {
                android.util.Log.e(tag, msg);

            }
        }

        public static void e(String tag, String msg, Exception e) {
            if (Constantes.DEBUG) {
                android.util.Log.e(tag, msg, e);

            }
        }

        public static void e(String tag, Exception e) {
            if (Constantes.DEBUG) {
                android.util.Log.e(tag, "", e);

            }
        }

        public static void v(String tag, String msg) {
            if (Constantes.DEBUG) {
                android.util.Log.v(tag, msg);
            }
        }

        public static void d(String tag, String msg) {
            if (Constantes.DEBUG) {
                android.util.Log.d(tag, msg);
            }
        }
}
