package com.corral.mityc.servicios;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;
import com.corral.mityc.util.NetworkUtils;

import java.io.IOException;

/**
 * Created by javier on 27/08/17.
 *
 * Este clase se ocupará de leer las provincias del servicio web de Mityc y llamar al receiver
 * correspondiente cuando esté listo.
 */

public class WSJsonGetEstacionesPorPoblacion {

    private static final String TAG = WSJsonGetEstacionesPorPoblacion.class.getSimpleName();

    private static AsyncTask<String, Void, String> mTask;

    private boolean running = false;
    static private String urlWSEstaciones =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/FiltroMunicipio/";

    // inyectaremos el receiver al que enviar la respuesta cuando tengamos el resultado
    protected static ResultReceiver mResultReceiver;

    private MitycRubi mMitycRubi;

    // El código de la población según Mityc
    static String codigoPobMityc;


    @SuppressLint("StaticFieldLeak")
    public void obtenEstaciones(ResultReceiver rr, MitycRubi mr, final String codigoPob) {

        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mResultReceiver = rr;
        mMitycRubi = mr;
        codigoPobMityc = codigoPob;

        if (running) return;

        mMitycRubi.getProgressBar().setMessage("Obteniendo estaciones por población ...");
        mTask = new AsyncTask<String, Void, String>() {

            protected String doInBackground(String... urls) {
                String res;
                try {
                    res = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrlEstacionesPorPoblacion(codigoPob));
                } catch (IOException e) {
                    res = null;
                }
                return res;

            }

            // result será la estructura JSon recibida del WS.
            protected void onPostExecute(String result) {
                if (result != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, result);
                    mResultReceiver.send(Constantes.SUCCESS_RESULT, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, result);
                    mResultReceiver.send(Constantes.FAILURE_RESULT, bundle);
                }
            }

        }.execute();
    }
}