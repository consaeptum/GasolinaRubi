package com.corral.mityc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.location.LocationManager;
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
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.corral.mityc.servicios.GeocoderHelperConAsynctask;
import com.corral.mityc.servicios.LocationPoblacionCentroIntentService;
import com.corral.mityc.servicios.WSJsonGetEstacionesPorPoblacion;
import com.corral.mityc.util.AutoResizeTextView;
import com.corral.mityc.util.MLog;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
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
    private static final int PETICION_PERMISO_MULTIPLE = 0;

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
    public static String SUBNOM_LOC_DRAWERLIST = "";
    public static String PROV_DRAWERLIST = "";
    public static Boolean cambioPoblacion = false;

    private String listaCodPoblaciones;
    private String listaNomPoblaciones;
    private String listaProvPoblaciones;
    public static DrawerLayout mDrawerLayout;

    // controlamos el bloqueo al borrar una población con esta variable
    private static Boolean bloqueo = false;

    private ListView mDrawerList;
    public OnMapReadyCallback mapCallBack = this;


    /**
     * La tabla de precios
     */
    private static TablaPrecios tp;

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

        MLog.v(Constantes.TAG, "onLocationChanged() ...");

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
        //if ((mLastLocation != null) && (!COD_LOC_DRAWERLIST.isEmpty())) {
        if (!COD_LOC_DRAWERLIST.isEmpty()) {

            prepararDrawerList();
            // iniciamos servicio de coordenadas de otra población para no usar este hilo
            // al consultar las coordenadas via http.
            //if (getEstacionVer() == null) {
                mLocationPoblacionCentroResultReceiver = new LocationPoblacionCentroResultReceiver(new Handler(), this);


// ATENCIONA PONER LA PROVINCIA PARA EVITAR ERRORES
                LocationPoblacionCentroIntentService.startAction(this,
                        mLocationPoblacionCentroResultReceiver,
                        (PROV_DRAWERLIST != null && !PROV_DRAWERLIST.isEmpty())? PROV_DRAWERLIST: null,
                        NOM_LOC_DRAWERLIST);
            //} else {
                //setEstacionVer(null);
            //}
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
        infoFalloConexión(true);
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

        // Cuando creamos una nueva población en la clase NuevaPoblacion, después de enviar
        // el Result a la activity MitycRubi, se llama a finish y MitycActivity llama a onRestart.
        // En onRestart se llama a ConectarApiGoogle, pero como en este caso queremos mantener
        // la población seleccionada y no ver la localización donde estamos, evitamos llamar a
        // enableLocationUpdates si cambioPoblación es verdadero.
        if (locationEnabled()) {
            if (permisosGps() && !cambioPoblacion) {
                enableLocationUpdates();


                MLog.v(Constantes.TAG, " onConnected() :::  llamada a enableLocationUpdates()  ...");
            }

        } else {
            infoFalloConexión(true);
        }
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
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // contexto y mLocationEstacionResultReceiverFromGeocoder se inicializan aquí porque en otras
        // partes del código al ser static no lo permite.
        contexto = this;
        mLocationEstacionResultReceiverFromGeocoder = new LocationEstacionResultReceiverFromGeocoder(new Handler(), this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tp = new TablaPrecios(this);
        listaCodPoblaciones = getListaCodPoblaciones()[0];

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                if (getEstacionVer() == null) {
                    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(omrc);
                    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                    prepararDrawerList();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (getEstacionVer() != null) {
                    setEstacionVer(null);
                    prepararDrawerList();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        ImageView iconDrawerClose = (ImageView) findViewById(R.id.imageIconSwipeLeft);
        iconDrawerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // evita que se cargue los Tab cada vez que se cambie de Tab.
        mViewPager.setOffscreenPageLimit(4);

        TabLayout tabLayout;
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mSectionsPagerAdapter.notifyDataSetChanged();

        staticFragment = getFragmentManager().findFragmentById(R.id.map);

        // guardamos el mapFragment para poder acceder a él desde clickEnEstacion()
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        // especificamos que hacer cuando encuentra una localización
        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // manejamos la primera que salga

                    MLog.v(Constantes.TAG, " LocationCallback :: (MitycRubi.onCreate()): onLocationResult ...");
                    localizacionConseguida(location);
                    break;
                }
            }
        };

        SharedPreferences sp = getSharedPreferences(getString(R.string.preferencias_mityc), MODE_PRIVATE);
        String codpob = sp.getString(getString(R.string.preferencia_ultima_cod_pob), "");
        if ((!codpob.isEmpty()) && (tp != null)) {
            if (tp.recuperaCache(codpob, this) != null) {
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }

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
                String nomprov = PROV_DRAWERLIST;   // En NuevaPoblación se modifica PROV_DRAWERLIST
                String codigos = getListaCodPoblaciones()[0];
                String nombres = getListaCodPoblaciones()[1];
                String provincias = getListaCodPoblaciones()[2];

                /*
                 * Evitamos añadir una población ya añadida previamente.
                 */
                if (!nombres.contains(nompob)) {
                    if (!codigos.isEmpty()) codigos = codigos.concat("#");
                    codigos = codigos.concat(codpob);

                    if (!nombres.isEmpty()) nombres = nombres.concat("#");
                    nombres = nombres.concat(nompob);

                    if (!provincias.isEmpty()) provincias = provincias.concat("#");
                    provincias = provincias.concat(nomprov);

                    setListaCodPoblaciones(new String[]{codigos, nombres, provincias});
                }

                COD_LOC_DRAWERLIST = codpob;
                NOM_LOC_DRAWERLIST = nompob;

                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(omrc);

                // cambiamos población a la última recién introducida
                cambioPoblacion(codigos.split("#").length);

            } else {

                // Al volver de NuevaPoblacion, el Toolbar se oculta y aquí forzamos que vuelva
                // a aparecer.
                Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
                tb.setVisibility(View.VISIBLE);
            }
        }

        if (requestCode == PETICION_CONFIG_UBICACION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    startLocationUpdates();
                    break;
                default:
                    infoFalloConexión(false);
                    break;

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
         * llamamos a conectarApiGoogle desde aquí para evitar hacerlo en onCreate, de modo que
         * en onCreate mostramos la última localidad cargada sin que se vea afectado el tiempo
         * de mostrar los componentes visuales de la activity.
         */
        //conectarApiGoogle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * conectamos el cliente de geolocalización.
         * Controlamos que mGoogleApiClient no sea null, ya que si es la primera ejecución
         * se mostraría la ayuda y llamaríamos a conectarApiGoogle más tarde.
         */
        if ((!cambioPoblacion) && (mGoogleApiClient != null) && (!mGoogleApiClient.isConnected())) {
            mGoogleApiClient.connect();
        }

/*
        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(
                Constantes.BROADCAST_ACTION);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mScrappingScrapWebMitycReceiver,
                mStatusIntentFilter);
*/
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
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

        if (item.getItemId() == android.R.id.home) {

            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(omrc);

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerLayout.openDrawer(Gravity.LEFT);
            // evita que se cierre el DrawerLayout al moverse por el mapa.
            //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            prepararDrawerList();

        }
        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_actualizar) {
        //}
        return super.onOptionsItemSelected(item);
    }


    /*
     * ShowViewCase para GasolinaRubí:
     *
     * Aquí mostramos ayuda al usuario sobre cómo utilizar la aplicación.
     */


    public void mostrarAyuda() {

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        View tv = getToolbarNavigationIcon(tb);
        final View v = (View) findViewById(R.id.left_drawer);
        mViewPager.setVisibility(View.INVISIBLE);

        SharedPreferences sp = getSharedPreferences(getString(R.string.preferencias_mityc), MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean(getString(R.string.preferencia_primera_vez), false);
        spe.commit();

        final ShowcaseView scv = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(tv))
                .setContentTitle("Menú desplegable: Mapa y lista de poblaciones preferidas")
                .setContentText("Aquí puede desplegar el mapa, visualizar otras poblaciones y guardarlas para visualizarlas más tarde")
                .blockAllTouches()
                .withMaterialShowcase()
                .setStyle(R.style.CustomShowcaseTheme)
                .build();
        scv.setButtonText("Entendido");


        scv.overrideButtonClick(new View.OnClickListener() {
            int count1 = 0;

            @Override
            public void onClick(View v) {
                count1++;
                switch (count1) {
                    case 1:
                        String[] poblaciones = {"0#1#2#3#4", "Terrassa#Sant Cugat del Vallès#Barcelona#Rubí#Sabadell", "0#1#2#3#4"};
                        setListaCodPoblaciones(poblaciones);
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                        prepararDrawerList();
                        scv.setContentTitle("Lista de poblaciones preferidas");
                        scv.setContentText("Para eliminar una población guardada, simplemente desplácela a la derecha");
                        scv.setStyle(ShowcaseView.LEFT_OF);
                        scv.setStyle(R.style.CustomShowcaseTheme);
                        scv.setShowcase(new ViewTarget(mDrawerList), false);
                        scv.setButtonText("Entendido");
                        break;
                    case 2:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        setListaCodPoblaciones(new String[] {"", "", ""});
                        mViewPager.setVisibility(View.VISIBLE);

                        scv.setContentTitle("Visualizar una estación en el mapa");
                        scv.setContentText("Puede visualizar la posición de una estación en el mapa, simplemente seleccionando una estación de la lista");
                        //scv.setStyle(ShowcaseView..LEFT_OF);
                        scv.setStyle(R.style.CustomShowcaseTheme);
                        scv.setShowcase(new ViewTarget(mViewPager), false);
                        scv.setButtonText("Entendido");
                        break;

                    case 3:
                        scv.hide();
                        break;
                }

            }
        });


    }

    public static View getToolbarNavigationIcon(Toolbar toolbar){
        //check if contentDescription previously was set
        boolean hadContentDescription = TextUtils.isEmpty(toolbar.getNavigationContentDescription());
        String contentDescription = !hadContentDescription ? toolbar.getNavigationContentDescription().toString() : "navigationIcon";
        toolbar.setNavigationContentDescription(contentDescription);
        ArrayList<View> potentialViews = new ArrayList<View>();
        //find the view based on it's content description, set programatically or with android:contentDescription
        toolbar.findViewsWithText(potentialViews,contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        //Nav icon is always instantiated at this point because calling setNavigationContentDescription ensures its existence
        View navIcon = null;
        if(potentialViews.size() > 0){
            navIcon = potentialViews.get(0); //navigation icon is ImageButton
        }
        //Clear content description if not previously present
        if(hadContentDescription)
            toolbar.setNavigationContentDescription(null);
        return navIcon;
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
        progressBar.setMessage("Conectando Api Google ...");
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


    public void infoFalloConexión(Boolean falloGps) {
        final AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = MitycRubi.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.info_conexion, null);
        TextView tv = (TextView) dialogView.findViewById(R.id.conexionText);

        if (falloGps) {
            tv.setText(getString(R.string.inform_conexion_text_gps));
        } else {
            tv.setText(getString(R.string.inform_conexion_text_red));
        }

        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mGoogleApiClient.disconnect();
                getProgressBar().dismiss();
                conectarApiGoogle();
            }
        });

        alert.setInverseBackgroundForced(true);
        alert.setView(dialogView);
        alert.show();
    }

    public void infoFalloMitycDB(String poblacion) {
        final AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = MitycRubi.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.info_mityc_db_poblacion, null);
        TextView tv = (TextView) dialogView.findViewById(R.id.conexionText);

        tv.setText(getString(R.string.inform_conexion_text_nopoblaciondb));

        alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mGoogleApiClient.disconnect();
                getProgressBar().dismiss();
            }
        });

        alert.setInverseBackgroundForced(true);
        alert.setView(dialogView);
        alert.show();
    }

    public Boolean locationEnabled() {
        Boolean gps_enabled = false;
        Boolean network_enabled = false;

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        return (gps_enabled || network_enabled);
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
        ArrayList<String> perms = new ArrayList<>();

        if (!(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED))
            perms.add(Manifest.permission.INTERNET);

        if (perms.size() > 0) {
            ActivityCompat.requestPermissions(this, perms.toArray(new String[] {}), PETICION_PERMISO_MULTIPLE);
        }
        return perms.size() == 0;
    }

    /*
     * Recibimos el resultado de la petición de permisos realizada en el anterior método.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PETICION_PERMISO_MULTIPLE: {
                boolean bperm = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        bperm = false;
                        break;
                    }
                }
                // If request is cancelled, the result arrays are empty.
                if (bperm) {
                    enableLocationUpdates();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        infoPermisos(getString(R.string.sinpermisoRechazado));
                    } else {
                        infoPermisos(getString(R.string.sinpermisoRechazadoPermanente));
                    }
                    return;
                }

            }
        }
    }

    public void infoPermisos(String t) {
        final AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = MitycRubi.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.info_permisos, null);

        if (!t.equals(getString(R.string.sinpermisoRechazadoPermanente))) {
            alert.setPositiveButton("Conceder permisos", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    permisosGps();
                }
            });
            alert.setNegativeButton("No conceder permisos", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        } else {
            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

        alert.setInverseBackgroundForced(true);
        alert.setView(dialogView);
        TextView tv = (TextView) dialogView.findViewById(R.id.textoInfoPerm);
        tv.setText(t);
        alert.show();
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

        // test Les Fonts
        //mLastLocation.setLatitude(41.528145);
        //mLastLocation.setLongitude(2.033808);

        // test Alicante / Alacant
        //mLastLocation.setLatitude(38.362262);
        //mLastLocation.setLongitude(-0.483169);

        // test VILLAJOYOSA / VILA JOIOSA (LA)|1204|38.5079606|-0.2285807
        //mLastLocation.setLatitude(38.50790606);
        //mLastLocation.setLongitude(-0.228507);

        // test Moixent 46|MOGENTE/MOIXENT|62498|38.8743266|-0.7520927
        //mLastLocation.setLatitude(38.8743266);
        //mLastLocation.setLongitude(-0.7520927);

        // test SantCugat
        //mLastLocation.setLatitude(41.473538);
        //mLastLocation.setLongitude(2.085244);

        // test Barcelona Guinardó
        //mLastLocation.setLatitude(41.402615);
        //mLastLocation.setLongitude(2.166605);

        // test Barcelona La Floresta
        //mLastLocation.setLatitude(41.444987);
        //mLastLocation.setLongitude(2.072265);

        // test Hospitalet
        //mLastLocation.setLatitude(41.346317);
        //mLastLocation.setLongitude(2.124958);

        // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
        // se llame a onMapReady()
        // iniciamos servicio de geolocalizacion.
        mCityNameResultReceiverFromGeocoder = new CityNameResultReceiverFromGeocoder(new Handler(), this);
        new GeocoderHelperConAsynctask().fetchCityNameFromLocation(this, mCityNameResultReceiverFromGeocoder, mLastLocation);

        disableLocationUpdates();
    }


    /*
        activamos la localización de posición real.
     */
    private void enableLocationUpdates() {

        mlocationRequest = LocationRequest.create();
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
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    //try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        infoFalloConexión(true);
                        //ResolvableApiException resolvable = (ResolvableApiException) e;
                        //resolvable.startResolutionForResult(MitycRubi.this,
                        //        PETICION_CONFIG_UBICACION);
                    //} catch (IntentSender.SendIntentException sendEx) {
                    //    infoFalloConexión();
                    //}
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
            infoFalloConexión(true);
        }
    }

    private void actualizar() {
        mostrarTituloBuscando(null, null);

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
                // security exception
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
     * [listaCodPoblaciones, listaNomPoblaciones, listaProvPoblaciones]
     */
    public String[] getListaCodPoblaciones() {
        SharedPreferences ultimaLocalidad = getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0);
        listaCodPoblaciones = ultimaLocalidad.getString(Constantes.SHARED_LISTA_CODIGOS_POBLACIONES, "");
        listaNomPoblaciones = ultimaLocalidad.getString(Constantes.SHARED_LISTA_NOMS_POBLACIONES, "");
        listaProvPoblaciones = ultimaLocalidad.getString(Constantes.SHARED_LISTA_PROV_POBLACIONES, "");

        /*
         * Resolvemos el bug Furelos en esta parte.
         * Comprobamos si existe en la lista, la localidad Furelos y si es cierto, reseteamos
         * la lista de poblaciones.
         * Se debe a que en un código de comprobación se añadió estas poblaciones (Rubí, Terrassa,
         * Barcelona, Sant Cugat del Vallès, Furelos) para hacer pruebas pero luego quedaron
         * grabadas en los terminales, con códigos de población erróneos.
         */
        if (listaNomPoblaciones.contains("Furelos")) {
            setListaCodPoblaciones(new String[] {"", "", ""});
            listaCodPoblaciones = "";
            listaNomPoblaciones = "";
            listaProvPoblaciones = "";
        }


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

        if (listaProvPoblaciones.startsWith("#"))
            listaProvPoblaciones = listaProvPoblaciones.substring(1, listaProvPoblaciones.length());
        if (listaProvPoblaciones.endsWith("#"))
            listaProvPoblaciones = listaProvPoblaciones.substring(0, listaProvPoblaciones.length() - 1);
        listaProvPoblaciones = listaProvPoblaciones.replace("##", "#");
        String[] retorno = { listaCodPoblaciones, listaNomPoblaciones, listaProvPoblaciones };

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
        if (!bloqueo) return;
        String[] l = getListaCodPoblaciones();
        String listaCod = l[0];
        String listaNom = l[1];
        String listaProv = l[2];

        int i = 0;
        String[] arrCod = listaCod.split("#");
        String[] arrNom = listaNom.split("#");
        String[] arrProv = listaProv.split("#");

        for (i = 0; i < arrNom.length; i++) {
            if (poblacion.equals(arrNom[i])) {
                break;
            }
        }

        if ((i < arrNom.length) && (!arrNom[i].equals(poblacion))) return;
        if (i == arrNom.length) return;

        List<String> alCod = new ArrayList<>(Arrays.asList(arrCod));
        alCod.remove(i);
        listaCod = TextUtils.join("#", alCod);

        List<String> alNom = new ArrayList<>(Arrays.asList(arrNom));
        alNom.remove(i);
        listaNom = TextUtils.join("#", alNom);

        List<String> alProv = new ArrayList<>(Arrays.asList(arrProv));
        alProv.remove(i);
        listaProv = TextUtils.join("#", alProv);

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

        if (listaProv.startsWith("#"))
            listaProv = listaProv.substring(1, listaProv.length());
        if (listaProv.endsWith("#"))
            listaProv = listaProv.substring(0, listaProv.length() - 1);
        listaProv = listaProv.replace("##", "#");

        setListaCodPoblaciones(new String[] { listaCod, listaNom, listaProv });
    }

    public void setListaCodPoblaciones(String[] lista) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0).edit();
        editor.putString(Constantes.SHARED_LISTA_CODIGOS_POBLACIONES, lista[0]);
        editor.putString(Constantes.SHARED_LISTA_NOMS_POBLACIONES, lista[1]);
        editor.putString(Constantes.SHARED_LISTA_PROV_POBLACIONES, lista[2]);
        editor.commit();

    }

    public void preparaListaMenuDrawer() {

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] lista = getListaCodPoblaciones();
        String[] listaCod = lista[0].split("#");
        String[] listaNom = lista[1].split("#");

        String listaPoblaciones = null;
        if (!listaCod[0].isEmpty()) {
            listaPoblaciones = NOM_LOCALIDAD + "#" + TextUtils.join("#", listaNom).concat("#"+getString(R.string.otras_poblaciones));
        } else {
            listaPoblaciones = NOM_LOCALIDAD.concat("#"+getString(R.string.otras_poblaciones));
        }
        listaNom = listaPoblaciones.split("#");
        mDrawerList.setAdapter(new DrawerListAdapter(getApplicationContext(), Arrays.asList(listaNom)));
    }


    public void prepararDrawerList() {

        // preparamos el drawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar mDrawerToolbar = (Toolbar) findViewById(R.id.toolbar);
        AutoResizeTextView td = (AutoResizeTextView) findViewById(R.id.tituloDrawer);
        td.setText(NOM_LOC_DRAWERLIST);

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
        st es ell nombre del barrio, en caso de tenerlo o "" en caso contrario.
     */
    public void mostrarTituloBuscando(String t, String st) {

        AutoResizeTextView titulo = (AutoResizeTextView) findViewById(R.id.titulo);
        if (t != null) {
            TextView tv = (TextView) findViewById(R.id.titulo_barrio);
            TextView tv2 = (TextView) findViewById(R.id.titulo_barrio2);
            if ((st != null) && (!st.isEmpty()) && (!Parseo.sinAcentos(t).equals(Parseo.sinAcentos(st)))) {
                titulo.setText(t);
                tv.setText(st);
                tv.setVisibility(View.VISIBLE);
                if (st.length() < 25) {
                    tv2.setVisibility(View.VISIBLE);
                } else {
                    tv2.setVisibility(View.GONE);
                }
            } else {
                titulo.setText(t);
                tv.setText("");
                tv.setVisibility(View.GONE);
                tv2.setVisibility(View.GONE);
            }
        }
        titulo.setTextColor(Color.parseColor("#FFFF0000"));
    }

    /*
        Cuando se han descargado los datos de una población se muestra el título en
        granate.
        Si el título t es null, sólo cambia de color el título actual.
        st es ell nombre del barrio, en caso de tenerlo o "" en caso contrario.
     */
    public void mostrarTituloEncontrado(String t, String st) {
        AutoResizeTextView titulo = (AutoResizeTextView) findViewById(R.id.titulo);
        if (t != null) {
            TextView tv = (TextView) findViewById(R.id.titulo_barrio);
            TextView tv2 = (TextView) findViewById(R.id.titulo_barrio2);
            if ((st != null) && (!st.isEmpty()) && ((!Parseo.sinAcentos(t).equals(Parseo.sinAcentos(st))))) {
                titulo.setHint(t);
                tv.setText(st);
                tv.setVisibility(View.VISIBLE);
                if (st.length() < 25) {
                    tv2.setVisibility(View.VISIBLE);
                } else {
                    tv2.setVisibility(View.GONE);
                }
            } else {
                titulo.setHint(t);
                tv.setText("");
                tv.setVisibility(View.GONE);
                tv2.setVisibility(View.GONE);
            }
        }
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

            final ColorDrawable[] BackGroundColor = {
                    new ColorDrawable(Color.parseColor("#EF9A9A")),
                    new ColorDrawable(Color.BLACK)
            };

            LayoutInflater inflater = getLayoutInflater();
            rowView = inflater.inflate(R.layout.drawer_list_item, null, true);
            holder = new ViewHolder();

            holder.textView1 = (TextView) rowView.findViewById(R.id.text1);
            holder.textView1.setText(labels.get(position));
            if (holder.textView1.getText().toString().equalsIgnoreCase(NOM_LOC_DRAWERLIST)) {
                holder.textView1.setTextColor(Color.parseColor("#FF9800"));
                holder.textView1.setTypeface(holder.textView1.getTypeface(), Typeface.BOLD_ITALIC);
            }
            holder.icon_1 = (ImageView) rowView.findViewById(R.id.locationnow);
            if (position == 0) {
                holder.icon_1.setVisibility(View.VISIBLE);
            } else if (labels.get(position).contains(getString(R.string.otras_poblaciones))){
                holder.icon_1.setImageResource(R.drawable.ic_playlist_add_black_24dp);
                holder.icon_1.setColorFilter(Color.WHITE);
                holder.icon_1.setVisibility(View.VISIBLE);
            } else {
                holder.icon_1.setVisibility(View.GONE);
            }
            rowView.setTag(holder);

            rowView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    Float currentX = event.getX();
                    Float currentY = event.getY();

                    TextView tv = ((ViewHolder) v.getTag()).textView1;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            if (!NOM_LOC_DRAWERLIST.equals(tv.getText())) {
                                TransitionDrawable transitionDrawable = new TransitionDrawable(BackGroundColor);
                                //tv.setBackground(transitionDrawable);
                                v.setBackground(transitionDrawable);
                                transitionDrawable.startTransition(1000);
                            }

                            mLastX = currentX;
                            mLastY = currentY;
                            break;
                        case MotionEvent.ACTION_MOVE:

                            if ((currentX > mLastX + 20) &&
                                    !labels.get(position).contains(getString(R.string.otras_poblaciones)) &&
                                    position > 0) {
                                if (bloqueo) {
                                    return false;
                                }
                                TranslateAnimation translateAnimation1 = new TranslateAnimation(
                                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 10f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
                                synchronized (bloqueo) {
                                    bloqueo = true;
                                    tv.setBackgroundColor(Color.CYAN);
                                    v.setEnabled(false);
                                    v.setOnClickListener(null);
                                    translateAnimation1.setDuration(1500);
                                    v.startAnimation(translateAnimation1);
                                    tv.setBackgroundColor(Color.CYAN);
                                    deleteListaCodPoblaciones(tv.getText().toString());
                                    bloqueo = false;
                                }
                                translateAnimation1.setAnimationListener(new Animation.AnimationListener() {
                                    public void onAnimationStart(Animation a) {
                                    }

                                    public void onAnimationRepeat(Animation a) {
                                    }

                                    public void onAnimationEnd(Animation a) {
                                        prepararDrawerList();
                                    }
                                });
                            }
                            break;
                        case MotionEvent.ACTION_UP:

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
        progressBar.setMessage("Buscando población ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        if (position > 0) {
            COD_LOC_DRAWERLIST = getListaCodPoblaciones()[0].split("#")[position - 1];
            NOM_LOC_DRAWERLIST = getListaCodPoblaciones()[1].split("#")[position - 1];
            PROV_DRAWERLIST = getListaCodPoblaciones()[2].split("#")[position - 1];
        } else {
            COD_LOC_DRAWERLIST = COD_LOCALIDAD;
            NOM_LOC_DRAWERLIST = NOM_LOCALIDAD;
            cambioPoblacion = false;
        }
        mostrarTituloBuscando(NOM_LOC_DRAWERLIST, SUBNOM_LOC_DRAWERLIST);

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
                        tvNom.setLines(3);
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
            //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);

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
                    return "NUEVO GASOLEO A";
                case 2:
                    return "SIN PLOMO \b 95";
                case 3:
                    return "SIN PLOMO \b 98";
            }
            return null;
        }
    }
}
