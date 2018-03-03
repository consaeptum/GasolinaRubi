package com.corral.mityc.receptores;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.servicios.WSJsonGetEstacionesPorPoblacion;

/*
 * La acción a realizar cuando WSJsonGetMunicipiosPorProvincia
 * tiene los datos preparados por fetchCityName()
 */
@SuppressLint("ParcelCreator")
public class MunicXProvResultReceiverFromWSJsonGetMunicipiosPorProvincia extends ResultReceiver {

    public MunicXProvResultReceiverFromWSJsonGetMunicipiosPorProvincia(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        String mCodigoMitycPoblacionResultado = null;

        if (resultCode == Constantes.SUCCESS_RESULT) {
            mCodigoMitycPoblacionResultado = resultData.getString(Constantes.RESULT_DATA_KEY);

            // acción a realizar cuando ya tenemos el código de la población de mityc
            if (mCodigoMitycPoblacionResultado != null) {

                // utilizamos WSJsonGetEstacionesPorPoblacion para obtener la lista
                // de estaciones de Mityc.

                WSJsonGetEstacionesPorPoblacion mWSJE = new WSJsonGetEstacionesPorPoblacion();
                mWSJE.obtenEstaciones();
            }
        }
    }
}
