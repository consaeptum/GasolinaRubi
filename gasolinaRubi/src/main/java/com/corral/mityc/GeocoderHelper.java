package com.corral.mityc;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
    Lo usamos para obtener la población que detectamos

 Usage:
    new GeocoderHelper().fetchCityName(context, location);
    Then do somthing in onPostExecute() of example code
    (send broadcast, invoke listener method, set a textView text, whatever)
 */
public class GeocoderHelper
{
    //private static final AndroidHttpClient ANDROID_HTTP_CLIENT = AndroidHttpClient.newInstance(GeocoderHelper.class.getName());

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

    public void fetchLocation(final Context contex, ResultReceiver rr, final Estacion estacion)
    {
        // guardamos el Receiver para enviar el resultado en onPostExecute()
        mReceiver = rr;

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
                        addresses = geocoder.getFromLocationName(estacion.getDireccion(),5);
                        location = new Location("");
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
                    return fetchLocationUsingGoogleMap();
                }
            }

            // Geocoder failed :-(
            // Our B Plan : Google Map
            private Location fetchLocationUsingGoogleMap()
            {
                String googleMapUrl = "";
                try
                {
                    // la dirección en Mityc contiene espacios de más y comas que podrían ser
                    // prescindibles.  Podría depurarse para mejorar el reconocimiento de la
                    // dirección.

                    // no reconoce carretera+e-9+km.+14,9,+rubi entre otros...

// movemos en direccion la poblacion al principio
// poblacion,direccion
                    String direccion = estacion.getDireccion();
                    Pattern pattern = Pattern.compile("^(.*),(.*)$");
                    Matcher matcher = pattern.matcher(direccion);
                    if (matcher.find())
                    {
                        direccion = "barcelona," + matcher.group(2).trim() + "," + matcher.group(1).replace(","," ");
                    }

                    String direc = Parseo.sinAcentos(direccion.replace(" ", "+"));
                    googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?address=" + direc + "&sensor=false&language=es";
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

                    String lat = null;   // población
                    String lon = null;   // código postal

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
                        estacion.setLocation(location);
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
                    Log.i("GeocoderHelper", loc.toString());

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constantes.RESULT_DATA_KEY, estacion);
                    mReceiver.send(Constantes.SUCCESS_RESULT, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constantes.RESULT_DATA_KEY, estacion);
                    mReceiver.send(Constantes.FAILURE_RESULT, bundle);
                }
            };
        }.execute();
    }

}