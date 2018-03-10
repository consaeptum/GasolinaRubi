package com.corral.mityc.servicios;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.corral.mityc.Constantes;
import com.corral.mityc.Parseo;
import com.corral.mityc.excepciones.RegistroNoExistente;
import com.corral.mityc.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by javier on 27/02/18.
 *
 * Con esta clase podremos buscar la población obtenida de cityNameResultReceiverFromGeocoder.
 *
 * Este clase se ocupará de leer las provincias del servicio web de Mityc y llamar al receiver
 * correspondiente cuando esté listo.
 */

public class WSJsonGetMunicipiosPorProvincia {

    private static final String TAG = WSJsonGetMunicipiosPorProvincia.class.getSimpleName();

    private static AsyncTask<String, Void, String> mTask;

    private static boolean running = false;
    static private String urlWSProvincias =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/Listados/MunicipiosPorProvincia/";


    // inyectaremos el receiver al que enviar la respuesta cuando tengamos el resultado
    protected static ResultReceiver mResultReceiver;


    @SuppressLint("StaticFieldLeak")
    public static void obtenMunicipio(ResultReceiver rr, final String cpprov, final String poblacion) {

        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mResultReceiver = rr;

        if (running) return;

        mTask = new  AsyncTask<String, Void, String>() {

            protected String doInBackground(String... urls) {
                String res;
                try {
                    res = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrlMuniciposPorProvincia(poblacion));
                } catch (IOException e) {
                    res = null;
                    Log.v(TAG, "### : " + "AsyncTask.doInBackGround() getResponseFromHttpUrl error");
                }
                return res;
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

////exep
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

                String pobABuscar = Parseo.sinAcentos(poblacion);
                pobABuscar = Parseo.quitarPreposiciones(pobABuscar);

                // transformamos a mayusculas y quitamos acentos y artículos a cada población de mityc.
                for (String codigoMityc: hm.keySet()) {
                    String pobMityc = Parseo.sinAcentos(hm.get(codigoMityc));
                    pobMityc = Parseo.quitarPreposiciones(pobMityc);
                    hm.put(codigoMityc, pobMityc);
                }

                // tenemos la lista de mityc (codigo, poblacion) en mayusculas, sin acentos ni
                // preposiciones o articulos.  También pobABuscar.
                String codigo;
                try {
                    codigo = Parseo.buscarCodigoPoblacionMityc(hm, pobABuscar);
                } catch (RegistroNoExistente rne) {
                    codigo = null;
                }
                return codigo;
            }
        };
        mTask.execute();
    }
}