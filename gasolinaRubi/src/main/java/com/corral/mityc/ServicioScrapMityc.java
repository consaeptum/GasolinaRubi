package com.corral.mityc;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ServicioScrapMityc extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    private Intent mitycIntent;
    private TablaPrecios tp = new TablaPrecios(this);

    public ServicioScrapMityc() {
        super("ServicioScrapMityc");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionScrap(Context context, String cod_localidad) {

        Intent intent = new Intent(context, ServicioScrapMityc.class);
        intent.setAction(Constantes.ACTION_SCRAP);
        intent.putExtra(Constantes.PARAMETRO_COD_LOCALIDAD, cod_localidad);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(Intent mitycRubiIntent) {
        if (mitycRubiIntent != null) {
            final String action = mitycRubiIntent.getAction();
            if (Constantes.ACTION_SCRAP.equals(action)) {
                mitycIntent = mitycRubiIntent;
                handleActionScrap();
            }
        }
    }

    /**
     * Handle action Scrap in the provided background thread with the provided
     * parameters.
     */
    private void handleActionScrap() {
        if (mitycIntent != null) {

            String cod_loc = mitycIntent.getStringExtra(Constantes.PARAMETRO_COD_LOCALIDAD);
            if (!tp.realizarPeticionHttp(cod_loc)) tp.recuperaCache(cod_loc, this);

            /*
             * Creates a new Intent containing a Uri object
             * BROADCAST_ACTION is a custom Intent action
             */
            Intent localIntent =
                    new Intent(Constantes.BROADCAST_ACTION)
            // Puts the status into the Intent
            .putExtra(Constantes.EXTENDED_DATA, tp);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        }
    }
}
