package com.corral.mityc;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.corral.mityc.estaciones.Estacion;
import com.corral.mityc.receptores.CityNameResultReceiverFromGeocoder;
import com.corral.mityc.receptores.EstacionesResultReceiverFromWSJsonGetEstacionesPorPoblacion;
import com.corral.mityc.receptores.LocationEstacionResultReceiverFromGeocoder;
import com.corral.mityc.receptores.LocationPoblacionCentroResultReceiver;
import com.corral.mityc.receptores.ScrapWebMitycReceiver;
import com.corral.mityc.servicios.GeocoderHelperConAsynctask;
import com.corral.mityc.servicios.LocationPoblacionCentroIntentService;
import com.corral.mityc.servicios.WSJsonGetEstacionesPorPoblacion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static android.support.v4.view.ViewPager.LayoutParams.MATCH_PARENT;
import static com.corral.mityc.MitycRubi.PlaceholderFragment.omrc;
import static com.corral.mityc.R.id.container;
import static com.google.ads.AdRequest.LOGTAG;

//import static com.corral.mityc.Parseo.cargaCodigosPoblacion;

//import com.google.android.gms.maps.MapFragment;

//import com.google.android.gms.maps.MapFragment;

//import static com.corral.mityc.MitycRubi.PlaceholderFragment.mapFragment;
//import static com.corral.mityc.MitycRubi.PlaceholderFragment.contexto;


public class MitycRubi extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        LocationListener {

    private LocationRequest mlocationRequest;
    private LocationCallback mlocationCallback;

    // El cliente que usaremos para detectar la posición gps.
    private FusedLocationProviderClient mFusedLocationClient;

    private static Context contexto;

    private static final Logger log = Logger.getLogger(Constantes.class.getName());
    private static final int RESULT_COD_POB = 1;
    public static final Integer PETICION_CONFIG_UBICACION = 1024;
    private static final int PERMISSION_REQUEST_LOCATION = 0;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    // COD_LOCALIDAD indica el código de la localidad que detectamos por geolocalización.
    // Como el usuario tiene la posibilidad de seleccionar otras localidades, en onCreate
    // COD_LOC_DRAWERLIST será igual a COD_LOCALIDAD pero cuando el usuario quiera ver
    // otra población COD_LOC_DRAWERLIST será el que indique la localidad actual mientras que
    // COD_LOCALIDAD seguirá indicando la localidad en la que estemos.
    public static String COD_LOCALIDAD = "";
    public static String COD_LOC_DRAWERLIST = "";
    public static String NOM_LOCALIDAD = "";
    public static String NOM_LOC_DRAWERLIST = "";
    public static Boolean cambioPoblacion = false;

    private String listaCodPoblaciones;
    private String listaNomPoblaciones;
    private static DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public OnMapReadyCallback mapCallBack = this;


    /**
     * La tabla de precios
     */
    private static TablaPrecios tp;

    private ScrapWebMitycReceiver mScrappingScrapWebMitycReceiver;
    private CityNameResultReceiverFromGeocoder mCityNameResultReceiverFromGeocoder;
    private LocationPoblacionCentroResultReceiver mLocationPoblacionCentroResultReceiver;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    private ActionBarDrawerToggle mDrawerToggle;
    private GoogleMap googleMap;

    // Si el usuario pulsa sobre una estación esta variable deja de ser null
    // y onMapReady sabe que el usuario quiere mostrar en el mapa la estación
    private static Estacion estacionVer = null;

    // el resultreceier para cuando el usuario haga clic en una estación.
    // Cuenado se tenga la posición geografica de la estación, iniciará el mapa.
    private static LocationEstacionResultReceiverFromGeocoder mLocationEstacionResultReceiverFromGeocoder;

    // la barra de progreso que indica que está cargando los datos.
    private ProgressDialog progressBar;
    private android.app.Fragment staticFragment;
    private static MapFragment mapFragment;

    /*******************************************************************************
     ************************** Métodos del interfaz LocationListener **************
     *******************************************************************************/

    @Override
    public void onLocationChanged(Location location) {
        localizacionConseguida(location);
        //actualizar();
        disableLocationUpdates();
    }


    /*******************************************************************************
     ************************** Métodos del interfaz OnMapReadyCallback ************
     *******************************************************************************/
    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        if ((mLastLocation != null) && (!COD_LOC_DRAWERLIST.isEmpty())) {

            prepararDrawerList();
            // iniciamos servicio de coordenadas de otra población para no usar este hilo
            // al consultar las coordenadas via http.
            mLocationPoblacionCentroResultReceiver = new LocationPoblacionCentroResultReceiver(new Handler(), this);
            LocationPoblacionCentroIntentService.startAction(this, mLocationPoblacionCentroResultReceiver, null, NOM_LOC_DRAWERLIST);
        }

    }

    public android.app.Fragment getStaticFragment() {
        return staticFragment;
    }

    /*******************************************************************************
     ************************** Métodos del interfaz OnConnectionFailedListener ****
     *******************************************************************************/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    /*******************************************************************************
     ************************** Métodos del interfaz ConnectionCallbacks ***********
     *******************************************************************************/
    /*
     * Cuando se haya completado la conexión mGoogleApiClient.connect(); se
     * ejecutará automáticamente este método.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (permisosGps())
            enableLocationUpdates();
    }


    @Override
    public void onConnectionSuspended(int i) {
    }


    /*******************************************************************************
     ************************** Métodos de la clase AppCompatActivity **************
     *******************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mityc_tab);

        // contexto y mLocationEstacionResultReceiverFromGeocoder se inicializan aquí porque en otras
        // partes del código al ser static no lo permite.
        contexto = this;
        mLocationEstacionResultReceiverFromGeocoder = new LocationEstacionResultReceiverFromGeocoder(new Handler(), this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tp = new TablaPrecios(this);

        listaCodPoblaciones = getListaCodPoblaciones()[0];

        ImageView iconDrawer = (ImageView) findViewById(R.id.imageIconDrawer);
        View.OnClickListener mToggleDrawerButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(omrc);

                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.openDrawer(Gravity.LEFT);
                // evita que se cierre el DrawerLayout al moverse por el mapa.
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                prepararDrawerList();
            }
        };

        ImageView iconDrawerClose = (ImageView) findViewById(R.id.imageIconSwipeLeft);
        iconDrawerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        iconDrawer.setOnClickListener(mToggleDrawerButton);

        /* especificamos que hacer cuando encuentra una localización */
        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // manejamos la primera que salga
                    localizacionConseguida(location);
                    break;
                }
            };
        };

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // evita que se cargue los Tab cada vez que se cambie de Tab.
        mViewPager.setOffscreenPageLimit(4);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //mScrappingScrapWebMitycReceiver = new ScrapWebMitycReceiver(this);

        staticFragment = getFragmentManager().findFragmentById(R.id.map);

        // guardamos el mapFragment para poder acceder a él desde clickEnEstacion()
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        conectarApiGoogle();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Verificamos ques sea la respuesta que solicitamos
        if (requestCode == RESULT_COD_POB) {
            // Verificamos que la respuesta sea correcta
            if (resultCode == RESULT_OK) {

                // recibimos un string con codigo#nombrePoblación de NuevaPoblacion setOnChildClickListener
                String[] res = data.getStringExtra(NuevaPoblacion.RESULTADO).split("#");
                String codpob = res[0];
                String nompob = res[1];
                String codigos = getListaCodPoblaciones()[0];
                String nombres = getListaCodPoblaciones()[1];

                if (!codigos.isEmpty()) codigos = codigos.concat("#");
                codigos = codigos.concat(codpob);

                if (!nombres.isEmpty()) nombres = nombres.concat("#");
                nombres = nombres.concat(nompob);

                setListaCodPoblaciones(new String[] {codigos, nombres});
                COD_LOC_DRAWERLIST = codpob;
                NOM_LOC_DRAWERLIST = nompob;

                // cambiamos población a la última recién introducida
                cambioPoblacion(codigos.split("#").length);
            }
        }
        if (requestCode == PETICION_CONFIG_UBICACION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i(LOGTAG, "El usuario no ha realizado los cambios de configuración necesarios");
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {

        try {
            unregisterReceiver(mScrappingScrapWebMitycReceiver);
        } catch (Exception e) {
            //Log.e("onPause", e.getMessage());
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // recargamos los códigos de población.
/*        if (Constantes.codigosPoblacion == null)
            cargaCodigosPoblacion(this);
*/

        // conectamos el cliente de geolocalización.
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }

        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(
                Constantes.BROADCAST_ACTION);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mScrappingScrapWebMitycReceiver,
                mStatusIntentFilter);

    }


    @Override
    protected void onPause() {

        try {
            unregisterReceiver(mScrappingScrapWebMitycReceiver);
        } catch (Exception e) {
            //Log.e("onPause", e.getMessage());
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mityc_tab, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_actualizar) {
        //}
        return super.onOptionsItemSelected(item);
    }


    /*******************************************************************************
     ************************** Métodos propios de MitycRubi ***********************
     *******************************************************************************/

    /*
        ----------
        |Activity|
        ----------
      conectarApiGoole()
              |
              |
              V
       ---------------------
       |connectionCallbacks|
       ---------------------
            onConnected()
                  |
                  |
                  V                       ---------------
        enableLocationUpdates() ----->    |PendingResult|
                                          ---------------
                                             onResult()
                                                 |
                                                 V                  ------------------
                                       startLocationUpdates() --->  |LocationListener|
                                                                    ------------------
                                                                    onLocationChanged()
                                                                            |
                                                                            |
                                                                            V
                                                                    localizacionConseguida()
                                                                            |
                                                                            |
                                                                            V
                                                                     getLastLocation()
                                                                     getMapAsync()
                                                                     FetchAddresIntentService()

     */

    private void conectarApiGoogle() {
        // conectamos mGoogleApliClient al final de onCreate para obligar a que
        // antes de nada se carguen los datos que hubiese en la caché y poder
        // mostrar los datos anteriores mientras se actualiza de fondo los datos
        // actuales.
        // Create an instance of GoogleAPIClient.  Para usar geolocalización.

        /* en este caso leeremos los datos de posicionamiento que hayan sido
           llamados por cualquier otra aplicación (puede ser impreciso)
        */

        // iniciar barra de progreso de carga
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Cargando datos del Ministerio de Industria ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        } // else
        // conectamos el cliente de geolocalización.  Cuando conecte llamará
        // automáticamente a onConnect()
        mGoogleApiClient.connect();
    }

    /***** GETTERS Y SETTERS ****************/
    /**
     * devuelve LastLocation
     * @return LastLocation
     */
    public Location getLastLocation() {
        return mLastLocation;
    }

    /**
     * devuelve tp
     * @return tp
     */
    public TablaPrecios getTp() {
        return tp;
    }

    public void setTp(TablaPrecios t) {
        tp = t;
    }

    /**
     * devuelve ViewPager
     * @return ViewPager
     */
    public ViewPager getViewPager() {
        return mViewPager;
    }

    /**
     * devuelve estacionVer
     * @return estacionVer
     */
    public Estacion getEstacionVer() {
        return estacionVer;
    }

    public static void setEstacionVer(Estacion e) {
        estacionVer = e;
    }

    /**
     * devuelve googleMap
     * @return googleMap
     */
    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    /**
     * devuelve progressBar
     * @return progressBar
     */
    public ProgressDialog getProgressBar() {
        return progressBar;
    }

    /********* FIN GETTERS Y SETTERS ************/


    /********************************************
     * Métodos para tratar permisos
     */

    /*
     * Si hay permiso para acceder a Gps, devuelve verdadero, si no falso.
     */
    public boolean permisosGps() throws SecurityException {
        Boolean perm = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
        // Si no tiene permisos, lo solicitamos y se recibirá el resultado de la solicitud en
        // onRequestPermissionsResult().
        if (!perm) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        }
        return perm;
    }

    /*
     * Recibimos el resultado de la petición de permisos realizada en el anterior método.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Si el usuario concede permisos, comenzamos de nuevo
                    // con enableLocationUpdates().
                    enableLocationUpdates();

                } else {

                    // El usuario denegó los permisos
                    Log.i(LOGTAG, "### MitycRubi:onRequestPermissionsResult-> el usuario" +
                            "denegó los permisos.");
                }
                return;
            }

        }
    }

    /*
     * FIN Métodos para tratar permisos
     ********************************************/

    /*
        Cuando ya tenemos una localización sea por LastLocation o
        LocationRequest llamamos a este método para iniciar FetchAddressResult.
        Usamos getLastLocation porque si se consiguió obtener LocationRequest
        LastLocation nos dará esa última localización, si no, la anterior.
     */
    private void localizacionConseguida(Location location) {

        if (!permisosGps()) return;

        //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLastLocation = location;

        // test Terrassa
        //mLastLocation.setLatitude(41.561111);
        //mLastLocation.setLongitude(2.008056);

        // test SantCugat
        //mLastLocation.setLatitude(41.473538);
        //mLastLocation.setLongitude(2.085244);

        // test Barcelona
        //mLastLocation.setLatitude(41.3825);
        //mLastLocation.setLongitude(2.176944);

        // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
        // se llame a onMapReady()

/*
        mapFragment = (MapFragment) this.getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(omrc);
*/

        // iniciamos servicio de geolocalizacion.
        mCityNameResultReceiverFromGeocoder = new CityNameResultReceiverFromGeocoder(new Handler(), this);
        new GeocoderHelperConAsynctask().fetchCityNameFromLocation(this, mCityNameResultReceiverFromGeocoder, mLastLocation);

        disableLocationUpdates();
    }


    /*
        activamos la localización de posición real.
     */
    private void enableLocationUpdates() {

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(1000);
        mlocationRequest.setFastestInterval(500);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //LocationSettingsRequest.Builder locSettingsRequest = new LocationSettingsRequest.Builder()
        //                .addLocationRequest(mlocationRequest);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.i(LOGTAG, "Configuración correcta");
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MitycRubi.this,
                                PETICION_CONFIG_UBICACION);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }


    /*
     * Cuando ya tenemos la posición real usamos este método para dejar de
     *Consultar la posición y ahorrar batería.
     */
    private void disableLocationUpdates() {

        mFusedLocationClient.removeLocationUpdates(mlocationCallback);

    }


    private void startLocationUpdates() {

        try {
            if (permisosGps())
                mFusedLocationClient.requestLocationUpdates(mlocationRequest, mlocationCallback, null);
        } catch (SecurityException se) {
            Log.i(LOGTAG, "### MitycRub.startLocationUpdates() securityException");
        }
    }

    private void actualizar() {
        mostrarTituloBuscando(null);

        // usamos ApiGoogle solo para coordenadas
        if ((!cambioPoblacion) && (mGoogleApiClient != null) && (!mGoogleApiClient.isConnected())) {
            conectarApiGoogle();
        } else {

            try {
                if (permisosGps()) {

                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    // iniciamos servicio de geolocalizacion.
                    if (!cambioPoblacion) {
                        mCityNameResultReceiverFromGeocoder = new CityNameResultReceiverFromGeocoder(new Handler(), this);
                        new GeocoderHelperConAsynctask().fetchCityNameFromLocation(this, mCityNameResultReceiverFromGeocoder, mLastLocation);
                    } else {
                        // aquí debemos tener NOM_LOCALIDAD, COD_LOC_DRAWER_LIST, COD_PRO_DRAWER_LIST
                        // Si llegamos aquí cuando el usuario ha

                        WSJsonGetEstacionesPorPoblacion mWSJE = new WSJsonGetEstacionesPorPoblacion();
                        EstacionesResultReceiverFromWSJsonGetEstacionesPorPoblacion eRRfWSExP =
                                new EstacionesResultReceiverFromWSJsonGetEstacionesPorPoblacion(new Handler(), this);
                        mWSJE.obtenEstaciones(eRRfWSExP, this, COD_LOC_DRAWERLIST);
                    }
                }
            } catch (SecurityException se) {
                Log.i(LOGTAG,"### MitycRubi.actualizar SecurityException");
            }
        }
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setVisibility(View.VISIBLE);
    }


    /*******************************************************************************
     ************************** Métodos para manejar el menu DrawerList ************
     *******************************************************************************/

    /*
     * devuelve array de strings.
     * [listaCodPoblaciones, listaNomPoblaciones]
     */
    public String[] getListaCodPoblaciones() {
        SharedPreferences ultimaLocalidad = getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0);
        listaCodPoblaciones = ultimaLocalidad.getString(Constantes.SHARED_LISTA_CODIGOS_POBLACIONES, "");
        listaNomPoblaciones = ultimaLocalidad.getString(Constantes.SHARED_LISTA_NOMS_POBLACIONES, "");

        if (listaCodPoblaciones.startsWith("#"))
            listaCodPoblaciones = listaCodPoblaciones.substring(1, listaCodPoblaciones.length());
        if (listaCodPoblaciones.endsWith("#"))
            listaCodPoblaciones = listaCodPoblaciones.substring(0, listaCodPoblaciones.length() - 1);
        listaCodPoblaciones = listaCodPoblaciones.replace("##", "#");

        if (listaNomPoblaciones.startsWith("#"))
            listaNomPoblaciones = listaNomPoblaciones.substring(1, listaNomPoblaciones.length());
        if (listaNomPoblaciones.endsWith("#"))
            listaNomPoblaciones = listaNomPoblaciones.substring(0, listaNomPoblaciones.length() - 1);
        listaNomPoblaciones = listaNomPoblaciones.replace("##", "#");
        String[] retorno = { listaCodPoblaciones, listaNomPoblaciones };

        return retorno;
    }

    /*
     * devuelve la posición en el menú del nombre de la población dada
     */
    public int posicionListaCodPoblaciones(String poblacion) {
        if (poblacion.equals(NOM_LOCALIDAD)) return 0;
        String lista = getListaCodPoblaciones()[1];
        String[] arrPobs = lista.split("#");
        for (int i = 0; i < arrPobs.length; i++) {
            if (poblacion.equals(arrPobs[i])) {
                return i + 1;
            }
        }
        return -1;
    }


    public void deleteListaCodPoblaciones(String poblacion) {
        String[] l = getListaCodPoblaciones();
        String listaCod = l[0];
        String listaNom = l[1];

        int i = 0;
        String[] arrCod = listaCod.split("#");
        String[] arrNom = listaNom.split("#");
        for (i = 0; i < arrNom.length; i++) {
            if (poblacion.equals(arrNom[i])) {
                break;
            }
        }
        if ((i < arrNom.length) && (!arrNom[i].equals(poblacion))) return;

        listaCod = listaCod.replaceAll(
                        "(^" + arrCod[i] + "$)|(^" + arrCod[i]
                        + "#)|(#" + arrCod[i] + "$)|(#" + arrCod[i]
                        + "#)"
                        , "#");
        listaNom = listaNom.replaceAll(
                "(^" + arrNom[i] + "$)|(^" + arrNom[i]
                        + "#)|(#" + arrNom[i] + "$)|(#" + arrNom[i]
                        + "#)"
                , "#");

        if (listaCod.startsWith("#"))
            listaCod = listaCod.substring(1, listaCod.length());
        if (listaCod.endsWith("#"))
            listaCod = listaCod.substring(0, listaCod.length() - 1);
        listaCod = listaCod.replace("##", "#");

        if (listaNom.startsWith("#"))
            listaNom = listaNom.substring(1, listaNom.length());
        if (listaNom.endsWith("#"))
            listaNom = listaNom.substring(0, listaNom.length() - 1);
        listaNom = listaNom.replace("##", "#");
        setListaCodPoblaciones(new String[] { listaCod, listaNom });
    }

    public void setListaCodPoblaciones(String[] lista) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0).edit();
        editor.putString(Constantes.SHARED_LISTA_CODIGOS_POBLACIONES, lista[0]);
        editor.putString(Constantes.SHARED_LISTA_NOMS_POBLACIONES, lista[1]);
        editor.commit();
    }

    public void preparaListaMenuDrawer() {

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] lista = getListaCodPoblaciones();
        String[] listaCod = lista[0].split("#");
        String[] listaNom = lista[1].split("#");

        String listaPoblaciones = null;
        if (!listaCod[0].isEmpty()) {
            listaPoblaciones = NOM_LOCALIDAD + "#" + TextUtils.join("#", listaNom).concat("#<otras poblaciones>");
        } else {
            listaPoblaciones = NOM_LOCALIDAD.concat("#<otras poblaciones>");
        }
        listaNom = listaPoblaciones.split("#");
        mDrawerList.setAdapter(new DrawerListAdapter(getApplicationContext(), Arrays.asList(listaNom)));
    }


    public void prepararDrawerList() {

        // preparamos el drawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar mDrawerToolbar = (Toolbar) findViewById(R.id.toolbar);

        preparaListaMenuDrawer();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mDrawerToolbar,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

    }


    /*
        Mientras se está descargando los datos de una población se muestra el título
        en color rojo.
        Si el título t es null, sólo cambia de color el título actual.
     */
    public void mostrarTituloBuscando(String t) {
        com.corral.mityc.AutoResizeTextView titulo = (com.corral.mityc.AutoResizeTextView) findViewById(R.id.titulo);
        if (t != null) titulo.setText(t);
        titulo.setTextColor(Color.parseColor("#FFFF0000"));
    }

    /*
        Cuando se han descargado los datos de una población se muestra el título en
        granate.
        Si el título t es null, sólo cambia de color el título actual.
     */
    public void mostrarTituloEncontrado(String t) {
        com.corral.mityc.AutoResizeTextView titulo = (com.corral.mityc.AutoResizeTextView) findViewById(R.id.titulo);
        if (t != null) titulo.setHint(t);
        titulo.setTextColor(Color.parseColor("#843636"));
    }

    /*******************************************************************************
     ************************** Clase para borrar elementos del menu DrawerList ****
     *******************************************************************************/
    public class DrawerListAdapter extends ArrayAdapter<String> {

        private Context context;
        private List<String> labels;
        private float mLastX;
        private float mLastY;



        public DrawerListAdapter(Context context, List<String> labels) {
            super(context, R.layout.drawer_list_item, labels);
            this.context = context;
            this.labels = labels;
        }

        class ViewHolder {
            public TextView textView1;
            public ImageView icon_1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            View rowView = convertView;

            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.drawer_list_item, null, true);
                holder = new ViewHolder();
                holder.textView1 = (TextView) rowView.findViewById(R.id.text1);
                holder.textView1.setText(labels.get(position));
                if (holder.textView1.getText().toString().equalsIgnoreCase(NOM_LOC_DRAWERLIST)) {
                    holder.textView1.setBackgroundColor(Color.MAGENTA);
                }
                holder.icon_1 = (ImageView) rowView.findViewById(R.id.locationnow);
                if (position == 0) {
                    holder.icon_1.setVisibility(View.VISIBLE);
                } else {
                    holder.icon_1.setVisibility(View.GONE);
                }
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            rowView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Float currentX = event.getX();
                    Float currentY = event.getY();

                    ViewHolder vh = (ViewHolder) v.getTag();
                    TextView tv = ((ViewHolder) v.getTag()).textView1;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mLastX = currentX;
                            mLastY = currentY;
                            tv.setBackgroundColor(Color.CYAN);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            tv.setBackgroundColor(Color.CYAN);
                            if ( currentX > mLastX + 10 ) { //v.getWidth() / 6) {
                                v.setEnabled(false);
                                v.setOnClickListener(null);
                                TranslateAnimation translateAnimation1 = new TranslateAnimation(
                                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 10f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
                                translateAnimation1.setDuration(1500);
                                v.startAnimation(translateAnimation1);
                                deleteListaCodPoblaciones(tv.getText().toString());
                                tv.setBackgroundColor(Color.BLACK);
                                translateAnimation1.setAnimationListener(new Animation.AnimationListener(){
                                    public void onAnimationStart(Animation a){}
                                    public void onAnimationRepeat(Animation a){}
                                    public void onAnimationEnd(Animation a){
                                        prepararDrawerList();
                                    }

                                });
                            }
                            if (( currentY > mLastY + 5 ) || ( currentY < mLastY - 5)) {
                                tv.setBackgroundColor(Color.BLACK);
                            }
                            break;
                        case MotionEvent.ACTION_UP:

                            tv.setBackgroundColor(Color.BLACK);
                            if (((currentX <= mLastX + 20) && (currentX >= mLastX - 20)) &&
                                    ((currentY <= mLastY + 10) && (currentY >= mLastY - 10))) {
                                onItemListClick(posicionListaCodPoblaciones(tv.getText().toString()));
                            }

                            break;
                    }
                    return true;
                }
            });
            return rowView;
        }
    }

    public void onItemListClick(int position) {
        if ( position == -1 ) { //(mDrawerList.getCount() - 1)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            nuevaPoblacion();
        } else {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            cambioPoblacion(position);
        }
    }

    private void nuevaPoblacion() {
        cambioPoblacion = true;
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(this, NuevaPoblacion.class);
        startActivityForResult(intent, RESULT_COD_POB);
    }

    private void cambioPoblacion(int position) {

        cambioPoblacion = true;
        // iniciar barra de progreso de carga
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Cargando datos del Ministerio de Industria ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        if (position > 0) {
            COD_LOC_DRAWERLIST = getListaCodPoblaciones()[0].split("#")[position - 1];
            NOM_LOC_DRAWERLIST = getListaCodPoblaciones()[1].split("#")[position - 1];
        } else {
            COD_LOC_DRAWERLIST = COD_LOCALIDAD;
            NOM_LOC_DRAWERLIST = NOM_LOCALIDAD;
            cambioPoblacion = false;
        }
        mostrarTituloBuscando(NOM_LOC_DRAWERLIST);

        // mientras se actualiza ViewPages, mostramos lo que tenemos en caché de esta población.
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        tp.recuperaCache(COD_LOC_DRAWERLIST, this);
        mViewPager.getAdapter().notifyDataSetChanged();
        actualizar();
    }


    /*******************************************************************************
     ************************** A placeholder fragment containing a simple view ****
     *******************************************************************************/
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        static final String ARG_SECTION_NUMBER = "section_number";
//        private static Boolean concurriendo = false;

        // Como mapCallBack es non-static y dentro de este bloque sí es estático
        // creamos esta variable intermedia statica para poder manejar el map
        // cuando el usuario haga clic en una estación. Lo iniciamos en newInstance().
        public static OnMapReadyCallback omrc;
        private static Fragment mFragment;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, OnMapReadyCallback o) {
            omrc = o;

            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);

            fragment.setArguments(args);

            // evitamos que al cambiar la orientación se pierda el fragment y haya null exception.
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.fragment, container, false);

//            synchronized (concurriendo) {
                if ((tp != null)) { // && (!concurriendo)) {
//                    concurriendo = true;
                    rellenaTabla(rootView);
                }
//                concurriendo = false;
//            }
            return rootView;
        }

        private void rellenaTabla(View rv) {

            int mNum = getArguments() != null ? getArguments().getInt(ARG_SECTION_NUMBER) : 1;

            TableLayout tl = (TableLayout) rv.findViewById(R.id.table_layout);

            ArrayList<Estacion> le;
            String producto = "";
            switch (mNum) {
                case 1:
                    producto = Constantes.LProductos[0][1];
                    break;
                case 2:
                    producto = Constantes.LProductos[1][1];
                    break;
                case 3:
                    producto = Constantes.LProductos[2][1];
                    break;
                case 4:
                    producto = Constantes.LProductos[3][1];
                    break;
            }

            //
            // SI ES LA PRIMERA EJECUCION NO CARGAR LA TABLA. USAR EL CACHE.
            // Si COD_LOC_DRAWERLIST no tiene todavía el código no hace nada
            // y esperamos a la siguiente pasada cuando haya código.
            if (!COD_LOC_DRAWERLIST.isEmpty()) {
//                synchronized (tp) {
                    le = tp.getEstaciones(COD_LOC_DRAWERLIST, producto);
//                }

                TableRow.LayoutParams trpstr = new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 3f);
                TableRow.LayoutParams trpnum = new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1f);

                int count = 0;
                if (le != null) {

                    for (Estacion estacion : le) {
                        TableRow tr = new TableRow(getContext());
                        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                        if (count % 2 != 0) tr.setBackgroundColor(Color.parseColor("#DECBCB"));
                        count++;
                        TextView tvNom = new TextView(getContext());
                        tvNom.setText(estacion.getNombre());
                        tvNom.setLayoutParams(trpstr);
                        tvNom.setTextColor(Color.BLACK);
                        if (tp.usandoCache()) tvNom.setTextColor(Color.parseColor("#843636"));
                        tr.addView(tvNom);

                        TextView tvDir = new TextView(getContext());
                        tvDir.setText(estacion.getDireccion());
                        tvDir.setLayoutParams(trpstr);
                        tvDir.setTextColor(Color.BLACK);
                        if (tp.usandoCache()) tvDir.setTextColor(Color.parseColor("#843636"));
                        tr.addView(tvDir);

                        TextView tvPvpAnt = new TextView(getContext());

                        tvPvpAnt.setLayoutParams(trpnum);
                        tvPvpAnt.setTextColor(Color.GRAY);
                        tr.addView(tvPvpAnt);


                        TextView tvPvpAct = new TextView(getContext());
                        tvPvpAct.setText(estacion.getProducto(producto).getPrecioFormat());
                        tvPvpAct.setLayoutParams(trpnum);
                        tvPvpAct.setTextColor(Color.BLACK);
                        if (tp.usandoCache()) tvPvpAct.setTextColor(Color.parseColor("#843636"));
                        tr.addView(tvPvpAct);

                        tl.addView(tr, new TableLayout.LayoutParams(
                                MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));

                        // añadirmos la acción a realizar cuando el usuario haga click
                        // en la estación de servicio que quiere ver.
                        final Estacion e = estacion;
                        final int cnt = count;
                        tr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                v.setBackgroundColor(Color.parseColor("#FFF1B5"));
                                AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.0f, 1.0f);
                                alphaAnimation1.setDuration(1500);
                                v.startAnimation(alphaAnimation1);

                                alphaAnimation1.setAnimationListener(new Animation.AnimationListener(){
                                    public void onAnimationStart(Animation a){}
                                    public void onAnimationRepeat(Animation a){}
                                    public void onAnimationEnd(Animation a){
                                        v.setBackgroundColor((cnt % 2 != 0)? Color.WHITE: Color.parseColor("#DECBCB"));
                                        clickEnEstacion(e, getActivity());
                                    }

                                });

                            }
                        });

                    }
                }
            }
        }
///###
        private void clickEnEstacion(Estacion estacion, FragmentActivity cntx) {

            // iniciamos servicio de geolocalizacion.
            //new GeocoderHelperConAsynctask().fetchLocationFromEstacion(getContext(), mLocationEstacionResultReceiverFromGeocoder, estacion);

            // obtenemos cordenadas de la estación previamente preparada con coordenadasResultReciever.
            setEstacionVer(estacion);

            // !! quizás esta parte debería llamarse desde LocationEstacionResultReceiverFromGeocoder
            // !! porque mientras no tenemos la localización, no debería modificarse el mapa.

            // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
            // se llame a onMapReady()

            MitycRubi.mapFragment.getMapAsync(omrc);

            // abrimos el Drawer
            mDrawerLayout = (DrawerLayout) cntx.findViewById(R.id.drawer_layout);
            mDrawerLayout.openDrawer(Gravity.LEFT);
            // evita que se cierre el DrawerLayout al moverse por el mapa.
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);

        }

    }


    /*******************************************************************************
     ************************** A FragmentPageAdapter that returns a fragment   ****
     ************************** corresponding to one of the sections/tabs/pages ****
     *******************************************************************************/
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, mapCallBack );
        }

        public int getItemPosition(Object item) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "GASOLEO A";
                case 1:
                    return "GASOLEO A NUEVO";
                case 2:
                    return "SIN PLOMO 95";
                case 3:
                    return "SIN PLOMO 98";
            }
            return null;
        }
    }
}
