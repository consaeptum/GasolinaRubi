package com.corral.mityc.receptores;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;
import com.corral.mityc.R;
import com.corral.mityc.estaciones.Estacion;
import com.google.android.gms.maps.MapFragment;

import static com.corral.mityc.MitycRubi.PlaceholderFragment.omrc;

/**
 * La acción a realizar cuando GeocoderHelperConAsynctask
 * tiene los datos preparados con fetchLocationFromEstacion....
 */
@SuppressLint("ParcelCreator")
public class LocationEstacionResultReceiverFromGeocoder extends ResultReceiver {

    MitycRubi mitycRubi;

    public LocationEstacionResultReceiverFromGeocoder(Handler handler, MitycRubi mr) {
        super(handler);
        mitycRubi = mr;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultCode == Constantes.SUCCESS_RESULT) {

            // obtenemos cordenadas de la estación previamente preparada con coordenadasResultReciever.
            mitycRubi.setEstacionVer((Estacion) resultData.getSerializable(Constantes.RESULT_DATA_KEY));

            // !! quizás esta parte debería llamarse desde LocationEstacionResultReceiverFromGeocoder
            // !! porque mientras no tenemos la localización, no debería modificarse el mapa.

            // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
            // se llame a onMapReady()
            MapFragment mapFragment = (MapFragment) mitycRubi.getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(omrc);

        }
    }
}
