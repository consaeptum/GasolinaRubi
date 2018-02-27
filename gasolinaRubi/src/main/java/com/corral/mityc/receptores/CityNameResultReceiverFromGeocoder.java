package com.corral.mityc.receptores;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;
import com.corral.mityc.R;
import com.corral.mityc.excepciones.RegistroNoExistente;
import com.corral.mityc.servicios.ScrapWebMitycIntentService;
import com.corral.mityc.servicios.WSJsonGetMunicipiosPorProvincia;
import com.google.android.gms.maps.MapFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.corral.mityc.MitycRubi.COD_LOCALIDAD;
import static com.corral.mityc.MitycRubi.COD_LOC_DRAWERLIST;
import static com.corral.mityc.Parseo.buscarCodigoAPoblacion;
import static com.corral.mityc.Parseo.buscarCodigoPoblacion;

/*
 * La acción a realizar cuando GeocoderHelperConAsynctask
 * tiene los datos preparados por fetchCityNameFromLocation()
 */
@SuppressLint("ParcelCreator")
public class CityNameResultReceiverFromGeocoder extends ResultReceiver {

    // inyectamos mitycRubi porque desde aquí se necesita acceder a diferentes propiedades de MitycRubi
    MitycRubi mitycRubi;

    public CityNameResultReceiverFromGeocoder(Handler handler, MitycRubi mr) {
        super(handler);
        mitycRubi = mr;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        String mDireccionResultado = null;

        // si falló la red al salir de suspend y Location devolvió null, lo intentamos
        // una vez más.
        if (resultCode == Constantes.FAILURE_RESULT) {
            if (!COD_LOC_DRAWERLIST.isEmpty()) {
                ScrapWebMitycIntentService.startActionScrap(mitycRubi.getApplicationContext(), COD_LOC_DRAWERLIST);
            } else {
                Toast.makeText(mitycRubi.getApplicationContext(), "Problema conectando al servidor MITYC", Toast.LENGTH_SHORT).show();
            }
        } else {

            // Obtenemos el nombre de la población y código postal y provincia.
            mDireccionResultado = resultData.getString(Constantes.RESULT_DATA_KEY);

            if (mDireccionResultado != null) {

                Pattern p = Pattern.compile("^(.*)\\s([0-9]{5})$"); // "poblacion códigoPostal"
                //Pattern p = Pattern.compile("^(.*)([0-9]{5}) (.*), (.*),(.*)$");
                Matcher m = p.matcher(mDireccionResultado);

                if (m.matches()) {
                    String cp = m.group(2);
                    String poblacion = m.group(1);
                    String cpprov = cp.substring(0, 2);
                    mitycRubi.getLastLocation().setProvider(poblacion); // trick
                    if (cpprov.startsWith("0")) cpprov = cpprov.substring(1, 2);

                    try {
                        String codpob = buscarCodigoPoblacion(cpprov, poblacion);

                        // Si COD_LOC_DRAWERLIST vacio -> primera vez que se utiliza la app
                        // OR COD_LOC_DRAWERLIST=COD_LOCALIDAD AND !=codpob -> habíamos seleccionado
                        // una población que ya descargamos o estamos en una población distinta a
                        // la anterior.
                        if ((COD_LOC_DRAWERLIST.isEmpty())
                                || ((COD_LOC_DRAWERLIST.equals(COD_LOCALIDAD))
                                && (!COD_LOCALIDAD.equals(codpob)))) {
                            COD_LOC_DRAWERLIST = codpob;
                        }

                        mitycRubi.mostrarTituloBuscando(buscarCodigoAPoblacion(COD_LOC_DRAWERLIST));
                        mitycRubi.getTp().recuperaCache(COD_LOC_DRAWERLIST, mitycRubi.getApplicationContext());

                        // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
                        // se llame a onMapReady()
                        MapFragment mapFragment = (MapFragment) mitycRubi.getFragmentManager().findFragmentById(R.id.map);
                        mapFragment.getMapAsync(mitycRubi.mapCallBack);

                        // ponemos en COD_LOCALIDAD la población recién detectada
                        // COD_LOC_DRAWERLIST mantendrá la que se hubiera seleccionado en la lista
                        COD_LOCALIDAD = codpob;

                        Toolbar t = (Toolbar) mitycRubi.findViewById(R.id.toolbar);
                        mitycRubi.setSupportActionBar(t);
                        mitycRubi.getViewPager().getAdapter().notifyDataSetChanged();

                        SharedPreferences.Editor editor = mitycRubi.getApplicationContext().getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0).edit();
                        editor.putString(Constantes.SHARED_PREFS_ULTIMA_LOCALIDAD, COD_LOC_DRAWERLIST);
                        editor.commit();

// Esta parte deberá quitarse y sustituirla por una llamada a WSJsonGetEstacionesPorPoblacionConAsynctask
// ya que en este punto llamaremos a WSJsonGetMunicipiosPorProvincia y le pasaremos el
// código de provincia y la población.  El receptor correspondiente llamará a WSJsonGetEstacionesPorPoblación

                        // aunque grabamos en SharedPreferences la Localidad actual, no descargamos
                        // los datos de esta sino de COD_LOC_DRAWERLIST (la que el usuario hubiera
                        // seleccionado)
//                        ScrapWebMitycIntentService.startActionScrap(mitycRubi.getApplicationContext(), COD_LOC_DRAWERLIST);
WSJsonGetMunicipiosPorProvincia.obtenMunicipios()
                    } catch (RegistroNoExistente rne) {
                        //log.log(Level.ALL, rne.getMessage());
                    }
                }
            }
        }
    }
}
