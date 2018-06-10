package com.corral.mityc.receptores;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.widget.Toolbar;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;
import com.corral.mityc.Parseo;
import com.corral.mityc.R;
import com.corral.mityc.servicios.WSJsonGetMunicipiosPorProvincia;
import com.corral.mityc.util.MLog;
import com.google.android.gms.maps.MapFragment;

import static com.corral.mityc.MitycRubi.COD_LOC_DRAWERLIST;
import static com.corral.mityc.MitycRubi.NOM_LOCALIDAD;
import static com.corral.mityc.MitycRubi.NOM_LOC_DRAWERLIST;
import static com.corral.mityc.MitycRubi.SUBNOM_LOC_DRAWERLIST;


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
            mitycRubi.infoFalloConexión(false);
            //Toast.makeText(mitycRubi.getApplicationContext(), "Problema conectando al servidor MITYC", Toast.LENGTH_SHORT).show();
        } else {

            String cpprov = "";

            // Obtenemos el nombre de la población y código postal y provincia.
            mDireccionResultado = resultData.getString(Constantes.RESULT_DATA_KEY);

            if (mDireccionResultado != null) {

                mitycRubi.getProgressBar().setMessage("Conseguida población por coordenadas geográficas ...");

                String[] toks = mDireccionResultado.split("#");
                String poblacion = toks[0];
                String barrio = toks[1].contains("null")? null: toks[1];
                String cp = toks[2];
                cpprov = cp.substring(0, 2);
                mitycRubi.PROV_DRAWERLIST = Parseo.buscarProvinciaCodigo(cpprov);

                MLog.v(Constantes.TAG, "CityNameResultReceiverFromGeocoder::onReceiveResult() "
                        + " PROV_DRAWERLIST " + mitycRubi.PROV_DRAWERLIST + "#" +
                        " poblacion " + poblacion + "#" +
                        " barrio " + barrio + "#" + cp );

                NOM_LOCALIDAD = poblacion;
                NOM_LOC_DRAWERLIST = poblacion;
                SUBNOM_LOC_DRAWERLIST = barrio;

                mitycRubi.mostrarTituloBuscando(NOM_LOCALIDAD, SUBNOM_LOC_DRAWERLIST);
                mitycRubi.getTp().recuperaCache(COD_LOC_DRAWERLIST, mitycRubi.getApplicationContext());

                // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
                // se llame a onMapReady()
                MapFragment mapFragment = (MapFragment) mitycRubi.getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(mitycRubi.mapCallBack);

                // ponemos en COD_LOCALIDAD la población recién detectada
                // COD_LOC_DRAWERLIST mantendrá la que se hubiera seleccionado en la lista
                //mitycRubi.COD_LOCALIDAD = codpob;

                Toolbar t = (Toolbar) mitycRubi.findViewById(R.id.toolbar);
                mitycRubi.setSupportActionBar(t);
                mitycRubi.getViewPager().getAdapter().notifyDataSetChanged();

                SharedPreferences.Editor editor = mitycRubi.getApplicationContext().getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0).edit();
                editor.putString(Constantes.SHARED_PREFS_ULTIMA_LOCALIDAD, COD_LOC_DRAWERLIST);
                editor.commit();

                MunicXProvResultReceiverFromWSJsonGetMunicipiosPorProvincia mxp =
                        new MunicXProvResultReceiverFromWSJsonGetMunicipiosPorProvincia(new Handler(), mitycRubi);

                WSJsonGetMunicipiosPorProvincia.obtenMunicipio(mxp, cpprov, poblacion);

            }
        }
    }
}
