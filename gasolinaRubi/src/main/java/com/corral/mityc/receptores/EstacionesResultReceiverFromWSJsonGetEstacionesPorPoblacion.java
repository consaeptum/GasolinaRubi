package com.corral.mityc.receptores;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;

/*
 * La acción a realizar cuando WSJsonGetMunicipiosPorProvincia
 * tiene los datos preparados por fetchCityName()
 */
@SuppressLint("ParcelCreator")
public class EstacionesResultReceiverFromWSJsonGetEstacionesPorPoblacion extends ResultReceiver {

    private MitycRubi mMitycRubi;

    public EstacionesResultReceiverFromWSJsonGetEstacionesPorPoblacion(Handler handler, MitycRubi mr) {
        super(handler);
        mMitycRubi = mr;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        String mJSonEstacionesResult = null;

        if (resultCode == Constantes.SUCCESS_RESULT) {
            mJSonEstacionesResult = resultData.getString(Constantes.RESULT_DATA_KEY);

            // acción a realizar cuando ya tenemos Las estaciones
            if (mJSonEstacionesResult != null) {

                //if (!mMitycRubi.getTp().realizarPeticionJSON(mJSonEstacionesResult)) tp.recuperaCache(cod_loc, this);
                //mMitycRubi.setTp((TablaPrecios) intent.getSerializableExtra(Constantes.EXTENDED_DATA));

                mMitycRubi.getTp().realizarPeticionJSON(mJSonEstacionesResult);

                // detenemos la barra de progreso porque ya tenemos lo que buscabamos.
                mMitycRubi.getProgressBar().dismiss();

                try {
                    mMitycRubi.cambioPoblacion = false;
                    mMitycRubi.getViewPager().getAdapter().notifyDataSetChanged();
                } catch (Exception e) {
                    // se produce al pulsar back button y volver a la aplicación.
                    e.printStackTrace();
                }
                mMitycRubi.mostrarTituloEncontrado(null);
                mMitycRubi.cambioPoblacion = false;
            }
        }
    }
}
