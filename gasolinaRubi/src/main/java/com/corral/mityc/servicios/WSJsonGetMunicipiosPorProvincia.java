package com.corral.mityc.servicios;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.corral.mityc.Constantes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

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


    public static void obtenMunicipio(ResultReceiver rr, final String cpprov, final String poblacion) {

        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mResultReceiver = rr;

        if (running) return;

        new AsyncTask<String, Void, String>() {

            protected String doInBackground(String... urls) {
                return readJSONFeed();
            }

            protected void onPostExecute(String result) {
                HashMap<String, String> codMitycPobs = cargaMunicipios(result);
                String codigoMityc = buscaPoblacion(codMitycPobs, poblacion);

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


            /*
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
                    return null;
                } catch (ProtocolException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
                return response;
            }

            /*
             * Carga la lista de Municipios para el código de provincia dado y lo devuelve
             * en un HashMap<codigo_poblacion_de_mityc, nombre_población>
             */
            HashMap<String, String> cargaMunicipios(String result) {

                HashMap<String, String> hm = new HashMap<String, String>();

                try {
                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject value=jsonArray.getJSONObject(i);
                        hm.put(value.getString("IDMunicipio"), value.getString("Municipio"));
                        Log.e("json", i+"="+value);
                    }

                } catch (Exception e) {
                    return null;
                }
                return hm;
            }

            /*
             * Buscamos la población dada entre la lista del HashMap<codigo, poblacion>
             */
            String buscaPoblacion(HashMap<String, String> hm, String poblacion) {

            }
        }.execute();
    }
}