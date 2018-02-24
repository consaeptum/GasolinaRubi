package com.corral.mityc.receptores;

/**
 * Cuando LocationPoblacionCentroIntentService devuelve las coordenadas de la población solcitada,
 * esta clase se encarga de recibir el valor y hacer lo correspondiente (poner en el mapa el puntero)
 */

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;

import com.corral.mityc.Constantes;
import com.corral.mityc.MitycRubi;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.corral.mityc.MitycRubi.COD_LOCALIDAD;
import static com.corral.mityc.MitycRubi.COD_LOC_DRAWERLIST;

/*********************************************************************************
 ************************** La acción a realizar cuando LocationPoblacionCentroIntentService
 ************************** tiene los datos preparados. (map listo) **************
 *********************************************************************************/
@SuppressLint("ParcelCreator")
public class LocationPoblacionCentroResultReceiver extends ResultReceiver {

    private MitycRubi mitycRubi;

    public LocationPoblacionCentroResultReceiver(Handler handler, MitycRubi mr) {
        super(handler);
        mitycRubi = mr;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        Location mAnotherLocation = mitycRubi.getLastLocation();
        CameraUpdate cameraUpdate;

        if (mitycRubi.getEstacionVer() == null) {

            // si necesitamos el mapa de una localidad distinta a la que estamos actualmente ...
            if (((!COD_LOC_DRAWERLIST.equals(COD_LOCALIDAD)) && (resultCode == Constantes.SUCCESS_RESULT))) {
                mAnotherLocation = (Location) resultData.getParcelable("location");
            }

            // si no conseguimos una localización precisa retornamos sin hacer nada.
            if (mAnotherLocation == null)
                return;

            // Si habíamos guardado de Provider el nombre de la población obtenido con GeoCodeHelper
            if (!mAnotherLocation.getProvider().equals("fused")) {
                mitycRubi.mostrarTituloBuscando(mAnotherLocation.getProvider());
                mitycRubi.preparaListaMenuDrawer();
            }
            mitycRubi.getGoogleMap().addMarker(new MarkerOptions()
                    .position(new LatLng(mAnotherLocation.getLatitude(), mAnotherLocation.getLongitude()))
                    .title(mitycRubi.getLastLocation().getProvider())); //buscarCodigoAPoblacion(COD_LOC_DRAWERLIST)));
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mAnotherLocation.getLatitude(), mAnotherLocation.getLongitude()), 12);
        } else {
            mitycRubi.getGoogleMap().addMarker(new MarkerOptions()
                    .position(new LatLng(mitycRubi.getEstacionVer().getLocation().getLatitude(), mitycRubi.getEstacionVer().getLocation().getLongitude()))
                    .title(mitycRubi.getEstacionVer().getNombre())).showInfoWindow();
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mitycRubi.getEstacionVer().getLocation().getLatitude(), mitycRubi.getEstacionVer().getLocation().getLongitude()), 16);
            mitycRubi.setEstacionVer(null);  // vuelve a ser null hasta que el usuario vuelva a hacer click en una estación.
        }

        // Gets to GoogleMap from the MapView and does initialization stuff
        mitycRubi.getGoogleMap().getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(mitycRubi.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mitycRubi.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mitycRubi.getGoogleMap().setMyLocationEnabled(false);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(mitycRubi.getApplicationContext());
            // Updates the location and zoom of the MapView
            mitycRubi.getGoogleMap().moveCamera(cameraUpdate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
