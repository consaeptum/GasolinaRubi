package com.corral.mityc.servicios;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.excepciones.FalloConexion;
import com.corral.mityc.receptores.LocationPoblacionCentroResultReceiver;
import com.corral.mityc.Parseo;
import com.corral.mityc.excepciones.RegistroNoExistente;

/**
 * En el mapa, mostramos el puntero a la población en general en el caso de que no estemos apuntando
 * a una Estación en concreto.
 * Este IntentService se encarga de buscar las coordenadas de la población indicada y devolviendo
 * el resultado a un ResultReceiver que se debe indicar al inicial el servicio.
 *
 */
public class LocationPoblacionCentroIntentService extends IntentService {

    protected static ResultReceiver mReceiver;

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
    public static void startAction(Context context, LocationPoblacionCentroResultReceiver mResultReceiver, String mPoblacion) {

        mReceiver = mResultReceiver;
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


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                Location loc;
                String poblacion = intent.getStringExtra(Constantes.LOCATION_DATA_EXTRA);

                loc = Parseo.getLocation(poblacion);

                deliverResultToReceiver(Constantes.SUCCESS_RESULT, loc);
            } catch (RegistroNoExistente registroNoExistente) {
                deliverResultToReceiver(Constantes.FAILURE_RESULT, null);
            } catch (FalloConexion falloConexion) {
                deliverResultToReceiver(Constantes.FAILURE_RESULT, null);
            }
        }
    }
}
