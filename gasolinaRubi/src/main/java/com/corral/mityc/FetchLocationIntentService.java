package com.corral.mityc;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FetchLocationIntentService extends IntentService {

    protected static ResultReceiver mReceiver;

    public FetchLocationIntentService() {
        super("FetchLocationIntentService");
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startAction(Context context, MitycRubi.CoordenadasResultReceiver mResultReceiver, String mPoblacion) {

        mReceiver = mResultReceiver;
        Intent intent = new Intent(context, FetchLocationIntentService.class);
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
