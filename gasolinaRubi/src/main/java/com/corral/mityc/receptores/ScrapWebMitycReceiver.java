package com.corral.mityc.receptores;

/**
 * Cuando el servicio ScrapWebMItycIntentService ha terminado el scraping de la web de Mityc,
 * llama a este Receiver.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;
import com.corral.mityc.TablaPrecios;

/*******************************************************************************
 ************************** BroadCastReceiver que se activa cuando *************
 ************************** ServicioScraptMityc.HandleEvent envía un aviso. ****
 *******************************************************************************/

public class ScrapWebMitycReceiver extends BroadcastReceiver {

    private MitycRubi mitycRubi;

    public ScrapWebMitycReceiver(MitycRubi mr) {
        super();
        mitycRubi = mr;
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {

        mitycRubi.setTp((TablaPrecios) intent.getSerializableExtra(Constantes.EXTENDED_DATA));

        // detenemos la barra de progreso porque ya tenemos lo que buscabamos.
        mitycRubi.getProgressBar().dismiss();

        try {
            mitycRubi.getViewPager().getAdapter().notifyDataSetChanged();
        } catch (Exception e) {
            // se produce al pulsar back button y volver a la aplicación.
            e.printStackTrace();
        }
        mitycRubi.mostrarTituloEncontrado(null);
    }
}

