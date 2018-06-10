package com.corral.mityc.servicios;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;


/*
    Lo usamos para obtener la población según las coordenadas con fetchCityNameFromLocation() o
    para obtener las coordenadas de una Estación con fetchLocationFromEstacion().
    Devuelve el resultado a un ResultReceiver que se le debe indicar.

 Usage:
    new GeocoderHelperConAsynctask().fetchCityNameFromLocation(context, location);
    Then do somthing in onPostExecute() of example code
    (send broadcast, invoke listener method, set a textView text, whatever)
 */
public class GeocoderHelperConAsynctask
{
    //private static final AndroidHttpClient ANDROID_HTTP_CLIENT = AndroidHttpClient.newInstance(GeocoderHelperConAsynctask.class.getName());
    private static AsyncTask<Void, Void, String> mTask;

    private static final String TAG = "MitycRubi__";

    private boolean running = false;

    // inyectaremos el receiver al que enviar la respuesta cuando tengamos el resultado
    protected static ResultReceiver mReceiver;

    @SuppressLint("StaticFieldLeak")
    public void fetchCityNameFromLocation(final Context contex, ResultReceiver rr, final Location location)
    {
        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mReceiver = rr;

        ((MitycRubi) contex).getProgressBar().setMessage("Obteniendo población por coordenadas geográficas ...");

        if (running || location == null)
            return;

        mTask = new AsyncTask<Void, Void, String>()
        {
            protected void onPreExecute()
            {
                running = true;
            };

            @Override
            protected String doInBackground(Void... params)
            {
                String cityNameCP = null;

                if (Geocoder.isPresent())
                {
                    try
                    {
                        Geocoder geocoder = new Geocoder(contex, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
                        if (addresses.size() > 0)
                        {
                            String cn = null; // población
                            String cs = null; // barrio
                            String cp = null; // código postal
                            for (Address a: addresses) {
                                if (cn == null) {
                                    cn = a.getLocality();
                                    cp = a.getPostalCode();
                                }
                                if (cs == null) {
                                    String n = a.getSubLocality();
                                    if (n != null) cs = a.getFeatureName();
                                }
                                if (cn != null && cs != null && cp != null) break;
                            }
                            cityNameCP = cn + "#" + cs + "#" + cp;
                        }
                    }
                    catch (Exception ignored)
                    {
                        // si algo falla intentaremos usar fetchCityNameUsingGoogleMap
                        // Así que no es necesario hacer nada con esta excepción.
                    }
                }

                if (cityNameCP != null) // i.e., Geocoder succeed
                {
                    Log.v(TAG, "GeocoderHelper::GeocoderHelperConAsynctask() " );
                    return cityNameCP;
                }
                else // i.e., Geocoder failed
                {
                    Log.v(TAG, "GeocoderHelper::fetchCityNameUsingGoogleMap " );
                    return fetchCityNameUsingGoogleMap();
                }
            }

            // Geocoder failed :-(
            // Our B Plan : Google Map
            private String fetchCityNameUsingGoogleMap()
            {
                String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + location.getLatitude() + ","
                        + location.getLongitude() + "&sensor=false&language=es";

                try
                {
                    URL url = new URL(googleMapUrl);
                    URLConnection urlCon = url.openConnection();
                    urlCon.connect();
                    InputStream datos = urlCon.getInputStream();

                    StringBuilder sb = new StringBuilder();
                    String line;

                    BufferedReader br = new BufferedReader(new InputStreamReader(datos));
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    JSONObject googleMapResponse = new JSONObject(sb.toString());

                    String cn = null;   // población
                    String cs = null;   // barrio
                    String cp = null;   // código postal

                    // many nested loops.. not great -> use expression instead
                    // loop among all results
                    JSONArray results = (JSONArray) googleMapResponse.get("results");
                    for (int i = 0; i < results.length(); i++)
                    {
                        // loop among all addresses within this result
                        JSONObject result = results.getJSONObject(i);
                        if (result.has("address_components"))
                        {
                            JSONArray addressComponents = result.getJSONArray("address_components");
                            // loop among all address component to find a 'locality' or 'sublocality'
                            for (int j = 0; j < addressComponents.length(); j++)
                            {
                                JSONObject addressComponent = addressComponents.getJSONObject(j);
                                if (result.has("types"))
                                {
                                    JSONArray types = addressComponent.getJSONArray("types");

                                    // search for locality and sublocality
                                    for (int k = 0; k < types.length(); k++)
                                    {
                                        // *********** población **********************************
                                        if ("locality".equals(types.getString(k)) && cn == null)
                                        {
                                            if (addressComponent.has("long_name"))
                                            {
                                                cn = addressComponent.getString("long_name");
                                            }
                                            else if (addressComponent.has("short_name"))
                                            {
                                                cn = addressComponent.getString("short_name");
                                            }
                                        }
                                        if ("route".equals(types.getString(k)) && cs == null)
                                        {
                                            if (addressComponent.has("long_name"))
                                            {
                                                cs = addressComponent.getString("long_name");
                                            }
                                            else if (addressComponent.has("short_name"))
                                            {
                                                cs = addressComponent.getString("short_name");
                                            }
                                        }

                                        // *********** código postal ******************************
                                        if ("postal_code".equals(types.getString(k)) && cp == null)
                                        {
                                            if (addressComponent.has("short_name"))
                                            {
                                                cp = addressComponent.getString("short_name");
                                            }
                                            else if (addressComponent.has("long_name"))
                                            {
                                                cp = addressComponent.getString("long_name");
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                    if (cn != null && cp != null)
                        return cn + "#" + cs + "#" +  cp;
                    else
                        return null;
                }
                catch (Exception ignored)
                {
                    ignored.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(String cityName)
            {
                running = false;
                if (cityName != null)
                {
                    // retornamos ciudad#barrio#codigoPostal
                    Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, cityName);
                    mReceiver.send(Constantes.SUCCESS_RESULT, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, cityName);
                    mReceiver.send(Constantes.FAILURE_RESULT, bundle);
                }
            }
        }.execute();
    }
}