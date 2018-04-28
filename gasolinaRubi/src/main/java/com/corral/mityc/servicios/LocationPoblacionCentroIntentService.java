package com.corral.mityc.servicios;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.receptores.LocationPoblacionCentroResultReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

/**
 * En el mapa, mostramos el puntero a la población en general en el caso de que no estemos apuntando
 * a una Estación en concreto.
 * Este IntentService se encarga de buscar las coordenadas de la población indicada y devolviendo
 * el resultado a un ResultReceiver que se debe indicar al inicial el servicio.
 *
 */
public class LocationPoblacionCentroIntentService extends IntentService {

    protected static ResultReceiver mReceiver;
    private boolean running = false;
    private static Context contex;
    private static String mMunicipio;
    private static String mProvincia;

    public LocationPoblacionCentroIntentService() {
        super("LocationPoblacionCentroIntentService");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startAction(Context context, LocationPoblacionCentroResultReceiver mResultReceiver, String provincia, String mPoblacion) {

        mReceiver = mResultReceiver;
        contex = context;
        mMunicipio = mPoblacion;
        mProvincia = provincia;

        Intent intent = new Intent(context, LocationPoblacionCentroIntentService.class);
        intent.putExtra(Constantes.RECEIVER, mResultReceiver);
        intent.putExtra(Constantes.LOCATION_DATA_EXTRA, mPoblacion);
        context.startService(intent);
    }

    private void deliverResultToReceiver(int resultCode, Location location) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("location", location);
        mReceiver.send(resultCode, bundle);
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            if (running)
                return;

            new AsyncTask<Void, Void, Location>()
            {
                Location location = null;

                protected void onPreExecute()
                {
                    running = true;
                };

                @Override
                protected Location doInBackground(Void... params)
                {
                    if (Geocoder.isPresent())
                    {
                        try
                        {
                            Geocoder geocoder = new Geocoder(contex, Locale.getDefault());
                            List<Address> addresses;
                            String direccion = "";
                            if (mProvincia == null)
                                direccion = mMunicipio;
                            else
                                direccion = mProvincia + "," + mMunicipio;
                            addresses = geocoder.getFromLocationName( direccion, 5);
                            location = new Location(mMunicipio);
                            location.setLatitude(addresses.get(0).getLatitude());
                            location.setLongitude(addresses.get(0).getLongitude());
                        }
                        catch (Exception ignored)
                        {
                            // after a while, Geocoder start to trhow "Service not availalbe" exception. really weird since it was working before (same device, same Android version etc..
                        }
                    }

                    if (location != null) // i.e., Geocoder succeed
                    {
                        return location;
                    }
                    else // i.e., Geocoder failed
                    {
                        return fetchLocationUsingGoogleMapHttp();
                    }
                }

                // Geocoder failed :-(
                // Our B Plan : Google Map
                private Location fetchLocationUsingGoogleMapHttp()
                {
                    String googleMapUrl = "";
                    try
                    {
                        // la dirección en Mityc contiene espacios de más y comas que podrían ser
                        // prescindibles.  Podría depurarse para mejorar el reconocimiento de la
                        // dirección.

                        // no reconoce carretera+e-9+km.+14,9,+rubi entre otros...
                        String direccion = "";
                        if (mProvincia == null)
                            direccion = mMunicipio;
                        else
                            direccion = mProvincia + "," + mMunicipio;
                        googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?address=" + direccion + "&sensor=false&language=es";
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

                        String lat = null;
                        String lon = null;

                        // many nested loops.. not great -> use expression instead
                        // loop among all results
                        JSONArray results = (JSONArray) googleMapResponse.get("results");
                        for (int i = 0; i < results.length(); i++)
                        {
                            // loop among all addresses within this result
                            JSONObject result = results.getJSONObject(i);
                            if (result.has("geometry"))
                            {
                                JSONObject geometry = result.getJSONObject("geometry");

                                if (geometry.has("location")) {
                                    JSONObject location = geometry.getJSONObject("location");
                                    lat = location.getString("lat");
                                    lon = location.getString("lng");
                                }
                            }

                        }
                        if ((lat != null) && (lon != null)) {
                            location = new Location("");
                            location.setLatitude(Double.parseDouble(lat));
                            location.setLongitude(Double.parseDouble(lon));
                            return location;
                        }

                    }
                    catch (Exception ignored)
                    {
                        ignored.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(Location loc)
                {
                    running = false;
                    if (loc != null)
                    {
                        // Do something with cityName
                        deliverResultToReceiver(Constantes.SUCCESS_RESULT, loc);
                    } else {
                        deliverResultToReceiver(Constantes.FAILURE_RESULT, null);
                    }
                };
            }.execute();

        }
    }
}
