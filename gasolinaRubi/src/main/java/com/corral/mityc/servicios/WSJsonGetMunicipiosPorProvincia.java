package com.corral.mityc.servicios;

import android.os.AsyncTask;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by javier on 27/02/18.
 *
 * Con esta clase podremos buscar la población obtenida de cityNameResultReceiverFromGeocoder.
 *
 * Este clase se ocupará de leer las provincias del servicio web de Mityc y llamar al receiver
 * correspondiente cuando esté listo.
 */

public class WSJsonGetMunicipiosPorProvincia {

    private static boolean running = false;
    static private String urlWSProvincias =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/Listados/MunicipiosPorProvincia/";


    // inyectaremos el receiver al que enviar la respuesta cuando tengamos el resultado
    protected static ResultReceiver mResultReceiver;


    public static void obtenMunicipio(ResultReceiver rr, final String cpprov, String poblacion) {

        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mResultReceiver = rr;
-
        if (running) return;

        new AsyncTask<String, Void, String>() {

            protected String doInBackground(String... urls) {
                return readJSONFeed();
            }

            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject weatherObservationItems = new JSONObject(jsonObject.getString("weatherObservation"));

                    System.out.println(weatherObservationItems.getString("clouds")
                            + " - "
                            + weatherObservationItems.getString("stationName"));
                } catch (Exception e) {
                    Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
                }
            }


            /**
             * [{
             *   "IDMunicipio":"Contenido de la cadena",
             *           "IDProvincia":"Contenido de la cadena",
             *           "IDCCAA":"Contenido de la cadena",
             *           "Municipio":"Contenido de la cadena",
             *           "Provincia":"Contenido de la cadena",
             *           "CCAA":"Contenido de la cadena"
             * }]
            */
            String readJSONFeed() {

                String response = "";
                try {
                    URL url = new URL(urlWSProvincias.concat(cpprov));
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