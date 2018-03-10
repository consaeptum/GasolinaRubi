package com.corral.mityc.servicios;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

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
    public void obtenEstaciones(ResultReceiver rr, MitycRubi mr, String codigoPob) {

        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mResultReceiver = rr;
        mMitycRubi = mr;
        codigoPobMityc = codigoPob;

        if (running) return;

        mTask = new AsyncTask<String, Void, String>() {

            protected String doInBackground(String... urls) {
                return readJSONFeed();
            }

            // resulta será la estructura JSon recibida del WS.
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

            String readJSONFeed() {

                String response = "";
                try {
                    URL url = new URL(urlWSEstaciones.concat(codigoPobMityc));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");

                    // read the response
                    System.out.println("Response Code: " + conn.getResponseCode());

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    response = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

                    System.out.println(response);

                } catch (java.net.MalformedURLException e){
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }.execute();
    }
}