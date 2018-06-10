package com.corral.mityc.servicios;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.Parseo;
import com.corral.mityc.excepciones.RegistroNoExistente;
import com.corral.mityc.util.MLog;
import com.corral.mityc.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import static com.corral.mityc.MitycRubi.NOM_LOC_DRAWERLIST;

/**
 * Created by javier on 27/02/18.
 *
 * Con esta clase podremos buscar la población obtenida de cityNameResultReceiverFromGeocoder.
 *
 * Este clase se ocupará de leer las provincias del servicio web de Mityc y llamar al receiver
 * correspondiente cuando esté listo.
 */

public class WSJsonGetMunicipiosPorProvincia {

    //private static final String TAG = WSJsonGetMunicipiosPorProvincia.class.getSimpleName();

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
                    res = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrlMuniciposPorProvincia(cpprov));
                } catch (IOException e) {
                    res = null;
                }
                return res;
            }

            protected void onPostExecute(String result) {
                HashMap<String, String> codMitycPobs;
                String codigoMityc = null;

                if (result != null) {
                    codMitycPobs = cargaMunicipios(result);
                    codigoMityc = buscaPoblacion(codMitycPobs, poblacion);
                    NOM_LOC_DRAWERLIST = poblacion;
                }

                if ((result != null) && (codigoMityc != null)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, codigoMityc);
                    mResultReceiver.send(Constantes.SUCCESS_RESULT, bundle);

                    MLog.v(Constantes.TAG, "WSJsonGetMunicipiosPorProvincia::onPostExecute() " +
                                    " Se encontró en Mityc la población " + poblacion);
                } else {

                    MLog.v(Constantes.TAG, "WSJsonGetMunicipiosPorProvincia::onPostExecute() " +
                                    " No se encontró en Mityc la población " + poblacion);

                            Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, "");
                    mResultReceiver.send(
                            codigoMityc==null? Constantes.FAILURE_NOTFOUND:Constantes.FAILURE_RESULT,
                            bundle);
                }
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

                    // si encontramos el caracter '/' (que indica la misma población en idioma local)
                    // creamos la misma población en dos líneas pero con la población en cada idioma
                    // por separado.
                    String v[] = hm.get(codigoMityc).split("/");
                    for (int i = 0; i < v.length; i++) {
                        String pobMityc = Parseo.sinAcentos(v[i]);
                        pobMityc = Parseo.quitarPreposiciones(pobMityc);
                        hm.put(codigoMityc, pobMityc.trim());
                    }

                    /*
                    if (codigoMityc.contains("/")) {
                        String pobMityc = Parseo.sinAcentos(hm.get(codigoMityc.split("/")[0]));
                        pobMityc = Parseo.quitarPreposiciones(pobMityc);
                        hm.put(codigoMityc, pobMityc.trim());
                    }
                    String pobMityc = Parseo.sinAcentos(hm.get(codigoMityc.split("/")[1]));
                    pobMityc = Parseo.quitarPreposiciones(pobMityc);
                    hm.put(codigoMityc, pobMityc.trim());
                    */
                }

                // tenemos la lista de mityc (codigo, poblacion) en mayusculas, sin acentos ni
                // preposiciones o articulos.  También pobABuscar.
                String codigo;
                try {
                    codigo = Parseo.buscarCodigoPoblacionMityc(hm, pobABuscar.trim());
                } catch (RegistroNoExistente rne) {
                    codigo = null;
                }
                return codigo;
            }
        };
        mTask.execute();
    }
}