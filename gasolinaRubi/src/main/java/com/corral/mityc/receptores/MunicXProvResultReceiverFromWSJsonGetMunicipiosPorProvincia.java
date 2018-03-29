package com.corral.mityc.receptores;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;
import com.corral.mityc.servicios.WSJsonGetEstacionesPorPoblacion;

/*
 * La acción a realizar cuando WSJsonGetMunicipiosPorProvincia
 * tiene los datos preparados por fetchCityName()
 */
@SuppressLint("ParcelCreator")
public class MunicXProvResultReceiverFromWSJsonGetMunicipiosPorProvincia extends ResultReceiver {

    private MitycRubi mMitycRubi;

    public MunicXProvResultReceiverFromWSJsonGetMunicipiosPorProvincia(Handler handler, MitycRubi mr) {
        super(handler);
        mMitycRubi = mr;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        String mCodigoMitycPoblacionResultado = null;

        if (resultCode == Constantes.SUCCESS_RESULT) {
            mCodigoMitycPoblacionResultado = resultData.getString(Constantes.RESULT_DATA_KEY);

            // acción a realizar cuando ya tenemos el código de la población de mityc
            if (mCodigoMitycPoblacionResultado != null) {

                //mMitycRubi.COD_LOCALIDAD = mCodigoMitycPoblacionResultado;
                mMitycRubi.COD_LOC_DRAWERLIST = mCodigoMitycPoblacionResultado;

                // utilizamos WSJsonGetEstacionesPorPoblacion para obtener la lista
                // de estaciones de Mityc.

                WSJsonGetEstacionesPorPoblacion mWSJE = new WSJsonGetEstacionesPorPoblacion();
                EstacionesResultReceiverFromWSJsonGetEstacionesPorPoblacion eRRfWSExP =
                        new EstacionesResultReceiverFromWSJsonGetEstacionesPorPoblacion(new Handler(), mMitycRubi);
                mWSJE.obtenEstaciones(eRRfWSExP, mMitycRubi, mCodigoMitycPoblacionResultado);
            }
        }
    }
}
