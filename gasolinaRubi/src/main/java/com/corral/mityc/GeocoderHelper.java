package com.corral.mityc;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;


/*
    Lo usamos para obtener la población que detectamos

 Usage:
    new GeocoderHelper().fetchCityName(context, location);
    Then do somthing in onPostExecute() of example code
    (send broadcast, invoke listener method, set a textView text, whatever)
 */
public class GeocoderHelper
{
    private static final AndroidHttpClient ANDROID_HTTP_CLIENT = AndroidHttpClient.newInstance(GeocoderHelper.class.getName());

    private boolean running = false;

    // inyectaremos el receiver al que enviar la respuesta cuando tengamos el resultado
    protected static ResultReceiver mReceiver;

    public void fetchCityName(final Context contex, ResultReceiver rr, final Location location)
    {
        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mReceiver = rr;

        if (running)
            return;

        new AsyncTask<Void, Void, String>()
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
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.size() > 0)
                        {
                            cityNameCP = addresses.get(0).getLocality() + " " + addresses.get(0).getPostalCode();
                        }
                    }
                    catch (Exception ignored)
                    {
                        // after a while, Geocoder start to trhow "Service not availalbe" exception. really weird since it was working before (same device, same Android version etc..
                    }
                }

                if (cityNameCP != null) // i.e., Geocoder succeed
                {
                    return cityNameCP;
                }
                else // i.e., Geocoder failed
                {
                    return fetchCityNameUsingGoogleMap();
                }
            }

            // Geocoder failed :-(
            // Our B Plan : Google Map
            private String fetchCityNameUsingGoogleMap()
            {
                String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + ","
                        + location.getLongitude() + "&sensor=false&language=es";

                try
                {
                    JSONObject googleMapResponse = new JSONObject(ANDROID_HTTP_CLIENT.execute(new HttpGet(googleMapUrl),
                            new BasicResponseHandler()));

                    String cn = null;   // población
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
                                        if ("sublocality".equals(types.getString(k)))
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
                                    if ((cn != null) && (cp != null))
                                    {
                                        return cn + " " +  cp;
                                    }
                                }
                            }
                        }
                    }
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
                    // Do something with cityName
                    Log.i("GeocoderHelper", cityName);

                    Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, cityName);
                    mReceiver.send(Constantes.SUCCESS_RESULT, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constantes.RESULT_DATA_KEY, cityName);
                    mReceiver.send(Constantes.FAILURE_RESULT, bundle);
                }
            };
        }.execute();
    }
}