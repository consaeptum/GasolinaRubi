package com.corral.mityc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
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
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.v4.view.ViewPager.LayoutParams.MATCH_PARENT;
import static com.corral.mityc.MitycRubi.PlaceholderFragment.omrc;
import static com.corral.mityc.Parseo.buscarCodigoAPoblacion;
import static com.corral.mityc.Parseo.buscarCodigoPoblacion;
import static com.corral.mityc.Parseo.cargaCodigosPoblacion;
import static com.corral.mityc.R.id.container;
import static com.google.ads.AdRequest.LOGTAG;

//import static com.corral.mityc.MitycRubi.PlaceholderFragment.mapFragment;
//import static com.corral.mityc.MitycRubi.PlaceholderFragment.contexto;


public class MitycRubi extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        LocationListener {

    private LocationRequest locRequest;
    private static Context contexto;

    private static final Logger log = Logger.getLogger(Constantes.class.getName());
    private static final int RESULT_COD_POB = 1;
    public static final Integer PETICION_CONFIG_UBICACION = 1024;

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

    private String listaCodPoblaciones;
    private static DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    public OnMapReadyCallback mapCallBack = this;


    /**
     * La tabla de precios
     */
    private static TablaPrecios tp;

    private ResponseReceiver mScrappingResponseReceiver;
    private AddressResultReceiver mAddressResultReceiver;
    private CoordenadasResultReceiver mLocationResultReceiver;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    // la dirección reversa resultado
    private static String mDireccionResultado;
    private ActionBarDrawerToggle mDrawerToggle;
    private GoogleMap googleMap;

    // Si el usuario pulsa sobre una estación esta variable deja de ser null
    // y onMapReady sabe que el usuario quiere mostrar en el mapa la estación
    private static Estacion estacionVer = null;

    // el resultreceier para cuando el usuario haga clic en una estación.
    // Cuenado se tenga la posición geografica de la estación, iniciará el mapa.
    private static GeocoderLocationResultReceiver mGeocoderLocationResultReceiver;

    // la barra de progreso que indica que está cargando los datos.
    private ProgressDialog progressBar;

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

            // iniciamos servicio de coordenadas de otra población para no usar este hilo
            // al consultar las coordenadas via http.
            mLocationResultReceiver = new CoordenadasResultReceiver(new Handler());
            FetchLocationIntentService.startAction(this, mLocationResultReceiver, COD_LOC_DRAWERLIST);
        }

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

        // contexto y mGeocoderLocationResultReceiver se inicializan aquí porque en otras
        // partes del código al ser static no lo permite.
        contexto = this;
        mGeocoderLocationResultReceiver = new GeocoderLocationResultReceiver(new Handler());

        // recargamos los códigos de población.
        cargaCodigosPoblacion(this);

        tp = new TablaPrecios(this);
        SharedPreferences ultimaLocalidad = getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0);
        COD_LOCALIDAD = ultimaLocalidad.getString(Constantes.SHARED_PREFS_ULTIMA_LOCALIDAD, "");
        COD_LOC_DRAWERLIST = COD_LOCALIDAD;

        listaCodPoblaciones = getListaCodPoblaciones();

        ImageView iconDrawer = (ImageView) findViewById(R.id.imageIconDrawer);
        View.OnClickListener mToggleDrawerButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.openDrawer(Gravity.LEFT);
                // evita que se cierre el DrawerLayout al moverse por el mapa.
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
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

        mScrappingResponseReceiver = new ResponseReceiver();

        prepararDrawerList();

        conectarApiGoogle();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Verificamos ques sea la respuesta que solicitamos
        if (requestCode == RESULT_COD_POB) {
            // Verificamos que la respuesta sea correcta
            if (resultCode == RESULT_OK) {
                String pob = data.getStringExtra(NuevaPoblacion.RESULTADO);
                String lcp = getListaCodPoblaciones();
                if (!lcp.isEmpty()) lcp = lcp.concat("#");
                lcp = lcp.concat(pob);
                setListaCodPoblaciones(lcp);
                prepararDrawerList();
                COD_LOC_DRAWERLIST = pob;
                actualizar();
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
            unregisterReceiver(mScrappingResponseReceiver);
        } catch (Exception e) {
            //Log.e("onPause", e.getMessage());
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // recargamos los códigos de población.
        if (Constantes.codigosPoblacion == null)
            cargaCodigosPoblacion(this);


        // conectamos el cliente de geolocalización.
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(
                Constantes.BROADCAST_ACTION);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mScrappingResponseReceiver,
                mStatusIntentFilter);
    }


    @Override
    protected void onPause() {

        try {
            unregisterReceiver(mScrappingResponseReceiver);
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


    /*
        Cuando ya tenemos una localización sea por LastLocation o
        LocationRequest llamamos a este método para iniciar FetchAddressResult.
        Usamos getLastLocation porque si se consiguió obtener LocationRequest
        LastLocation nos dará esa última localización, si no, la anterior.
     */
    private void localizacionConseguida(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
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
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // iniciamos servicio de geolocalizacion.
        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        new GeocoderHelper().fetchCityName(this, mAddressResultReceiver, mLastLocation);

        disableLocationUpdates();
    }


    /*
        activamos la localización de posición real.
     */
    private void enableLocationUpdates() {

        locRequest = new LocationRequest();
        locRequest.setInterval(1000);
        locRequest.setFastestInterval(500);
        locRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest locSettingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locRequest)
                        .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, locSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        //Log.i(LOGTAG, "Configuración correcta");
                        startLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            //Log.i(LOGTAG, "Se requiere actuación del usuario");
                            status.startResolutionForResult(MitycRubi.this, PETICION_CONFIG_UBICACION);
                        } catch (IntentSender.SendIntentException e) {
                            //Log.i(LOGTAG, "Error al intentar solucionar configuración de ubicación");
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //Log.i(LOGTAG, "No se puede cumplir la configuración de ubicación necesaria");
                        break;
                }
            }
        });
    }


    /*
     * Cuando ya tenemos la posición real usamos este método para dejar de
     *Consultar la posición y ahorrar batería.
     */
    private void disableLocationUpdates() {

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Ojo: estamos suponiendo que ya tenemos concedido el permiso.
            //Sería recomendable implementar la posible petición en caso de no tenerlo.

            //Log.i(LOGTAG, "Inicio de recepción de ubicaciones");

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locRequest, MitycRubi.this);
        }
    }


    private void actualizar() {
        mostrarTituloBuscando(null);

        // usamos ApiGoogle solo para coordenadas
        if ((mGoogleApiClient != null) && (!mGoogleApiClient.isConnected())) {
            conectarApiGoogle();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
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

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // iniciamos servicio de geolocalizacion.
            mAddressResultReceiver = new AddressResultReceiver(new Handler());
            new GeocoderHelper().fetchCityName(this, mAddressResultReceiver, mLastLocation);
        }
    }


    /*******************************************************************************
     ************************** Métodos para manejar el menu DrawerList ************
     *******************************************************************************/

    public String getListaCodPoblaciones() {
        SharedPreferences ultimaLocalidad = getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0);
        listaCodPoblaciones = ultimaLocalidad.getString(Constantes.SHARED_LISTA_CODIGOS_POBLACIONES, "");
        if (listaCodPoblaciones.startsWith("#"))
            listaCodPoblaciones = listaCodPoblaciones.substring(1, listaCodPoblaciones.length());
        if (listaCodPoblaciones.endsWith("#"))
            listaCodPoblaciones = listaCodPoblaciones.substring(0, listaCodPoblaciones.length() - 1);
        listaCodPoblaciones = listaCodPoblaciones.replace("##", "#");
        return listaCodPoblaciones;
    }

    public int posicionListaCodPoblaciones(String poblacion) {
        if (poblacion.equals(buscarCodigoAPoblacion(COD_LOCALIDAD))) return 0;
        String lista = getListaCodPoblaciones();
        int i = 1;
        int j = -1;
        for (String c: lista.split("#")) {
            String p = buscarCodigoAPoblacion(c);
            if (poblacion.equals(p)) {
                poblacion = c;
                j = i;
                break;
            }
            i++;
        }
        return j;
    }

    public void deleteListaCodPoblaciones(String poblacion) {
        String lista = getListaCodPoblaciones();
        for (String c: lista.split("#")) {
            String p = buscarCodigoAPoblacion(c);
            if (poblacion.equals(p)) {
                poblacion = c;
                break;
            }
        }
        lista = lista.replaceAll(
                        "(^" + poblacion + "$)|(^" + poblacion
                        + "#)|(#" + poblacion + "$)|(#" + poblacion
                        + "#)"
                        , "#");
        if (lista.startsWith("#"))
            lista = lista.substring(1, lista.length());
        if (lista.endsWith("#"))
            lista = lista.substring(0, lista.length() - 1);
        lista = lista.replace("##", "#");
        setListaCodPoblaciones(lista);
    }

    public void setListaCodPoblaciones(String lista) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0).edit();
        editor.putString(Constantes.SHARED_LISTA_CODIGOS_POBLACIONES, lista);
        editor.commit();
    }

    public void preparaListaMenuDrawer() {

        int j = 0;
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        String[] listaCPob = getListaCodPoblaciones().split("#");
        for (int i = 0; i < listaCPob.length; i++) {
            listaCPob[i] = buscarCodigoAPoblacion(listaCPob[i]);
            if (COD_LOC_DRAWERLIST.equals(listaCPob[i])) j = i;
        }
        String listaPoblaciones = null;
        if ((!listaCPob[0].isEmpty()) && (listaCPob.length >= 1))
            listaPoblaciones = buscarCodigoAPoblacion(COD_LOCALIDAD) + "#"
                    + TextUtils.join("#", listaCPob).concat("#<otras poblaciones>");
        else
        if (COD_LOCALIDAD.isEmpty()) {
            listaPoblaciones = "< otras poblaciones >";
        } else {
            listaPoblaciones = buscarCodigoAPoblacion(COD_LOCALIDAD).concat("#<otras poblaciones>");
        }
        listaCPob = listaPoblaciones.split("#");

        mDrawerList.setAdapter(new DrawerListAdapter(getApplicationContext(), Arrays.asList(listaCPob)));
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
                if (holder.textView1.getText().toString().equalsIgnoreCase(buscarCodigoAPoblacion(COD_LOC_DRAWERLIST))) {
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
/*                            else {
                                if (currentX < mLastX - 5 ) {
                                    tv.setBackgroundColor(Color.BLACK);
                                }
                                if (currentX < mLastX - 10) {
                                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                                }
                            }
*/
                            break;
                        case MotionEvent.ACTION_UP:

                            tv.setBackgroundColor(Color.BLACK);
                            if ((currentX == mLastX) && (currentY == mLastY)) {
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
        Intent intent = new Intent(this, NuevaPoblacion.class);
        startActivityForResult(intent, RESULT_COD_POB);
        actualizar();
    }

    private void cambioPoblacion(int position) {

        // iniciar barra de progreso de carga
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Cargando datos del Ministerio de Industria ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        if (position > 0) {
            COD_LOC_DRAWERLIST = listaCodPoblaciones.split("#")[position - 1];
        } else {
            COD_LOC_DRAWERLIST = COD_LOCALIDAD;
        }
        // mientras se actualiza ViewPages, mostramos lo que tenemos en caché de esta población.
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        mostrarTituloBuscando(buscarCodigoAPoblacion(COD_LOC_DRAWERLIST));
        tp.recuperaCache(COD_LOC_DRAWERLIST, this);
        mViewPager.getAdapter().notifyDataSetChanged();
        actualizar();
    }



    /*******************************************************************************
     ************************** BroadCastReceiver que se activa cuando *************
     ************************** ServicioScraptMityc.HandleEvent envía un aviso. ****
     *******************************************************************************/

    private class ResponseReceiver extends BroadcastReceiver {
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {

            tp = (TablaPrecios) intent.getSerializableExtra(Constantes.EXTENDED_DATA);

            // detenemos la barra de progreso porque ya tenemos lo que buscabamos.
            progressBar.dismiss();

            try {
                mViewPager.getAdapter().notifyDataSetChanged();
            } catch (Exception e) {
                // se produce al pulsar back button y volver a la aplicación.
                e.printStackTrace();
            }
            mostrarTituloEncontrado(null);
        }
    }


    /*******************************************************************************
     ************************** A placeholder fragment containing a simple view ****
     *******************************************************************************/
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
//        private static Boolean concurriendo = false;

        // Como mapCallBack es non-static y dentro de este bloque sí es estático
        // creamos esta variable intermedia statica para poder manejar el map
        // cuando el usuario haga clic en una estación. Lo iniciamos en newInstance().
        static OnMapReadyCallback omrc;

        //private com.google.android.gms.maps.SupportMapFragment mapFragment = null;


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
                                        clickEnEstacion(e);
                                    }

                                });

                            }
                        });

                    }
                }
            }
        }

        private void clickEnEstacion(Estacion estacion) {

            // iniciamos servicio de geolocalizacion.
            new GeocoderHelper().fetchLocation(getContext(), mGeocoderLocationResultReceiver, estacion);

            // abrimos el Drawer
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
            return PlaceholderFragment.newInstance(position + 1, mapCallBack);
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


    /*******************************************************************************
     ************************* La acción a realizar cuando GeocoderHelper
     ************************* tiene los datos preparados con fetchLocation....
     *******************************************************************************/
    @SuppressLint("ParcelCreator")
    class GeocoderLocationResultReceiver extends ResultReceiver {

        public GeocoderLocationResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == Constantes.SUCCESS_RESULT) {

                // obtenemos cordenadas de la estación previamente preparada con coordenadasResultReciever.
                estacionVer = (Estacion) resultData.getSerializable(Constantes.RESULT_DATA_KEY);

                // !! quizás esta parte debería llamarse desde GeocoderLocationResultReceiver
                // !! porque mientras no tenemos la localización, no debería modificarse el mapa.

                // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
                // se llame a onMapReady()
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(omrc);

            }
        }
    }

    /*******************************************************************************
     ************************* La acción a realizar cuando GeocoderHelper
     ************************* tiene los datos preparados con fetchCityName
     *******************************************************************************/
    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // si falló la red al salir de suspend y Location devolvió null, lo intentamos
            // una vez más.
            if (resultCode == Constantes.FAILURE_RESULT) {
                if (!COD_LOC_DRAWERLIST.isEmpty()) {
                    ServicioScrapMityc.startActionScrap(getApplicationContext(), COD_LOC_DRAWERLIST);
                } else {
                    Toast.makeText(getApplicationContext(), "Problema conectando al servidor MITYC", Toast.LENGTH_SHORT).show();
                }
            } else {

                // Obtenemos el nombre de la población.
                mDireccionResultado = resultData.getString(Constantes.RESULT_DATA_KEY);

                if (mDireccionResultado != null) {

                    Pattern p = Pattern.compile("^(.*)\\s([0-9]{5})$"); // "poblacion códigoPostal"
                    //Pattern p = Pattern.compile("^(.*)([0-9]{5}) (.*), (.*),(.*)$");
                    Matcher m = p.matcher(mDireccionResultado);

                    if (m.matches()) {
                        String cp = m.group(2);
                        String poblacion = m.group(1);
                        String cpprov = cp.substring(0, 2);
                        mLastLocation.setProvider(poblacion); // trick
                        if (cpprov.startsWith("0")) cpprov = cpprov.substring(1, 2);

                        try {
                            String codpob = buscarCodigoPoblacion(cpprov, poblacion);

                            // Si COD_LOC_DRAWERLIST vacio -> primera vez que se utiliza la app
                            // OR COD_LOC_DRAWERLIST=COD_LOCALIDAD AND !=codpob -> habíamos seleccionado
                            // una población que ya descargamos o estamos en una población distinta a
                            // la anterior.
                            if ((COD_LOC_DRAWERLIST.isEmpty())
                                    || ((COD_LOC_DRAWERLIST.equals(COD_LOCALIDAD))
                                            && (!COD_LOCALIDAD.equals(codpob)))) {
                                COD_LOC_DRAWERLIST = codpob;
                            }

                            mostrarTituloBuscando(buscarCodigoAPoblacion(COD_LOC_DRAWERLIST));
                            tp.recuperaCache(COD_LOC_DRAWERLIST, getApplicationContext());

                            // una vez tenemos las coordenadas preparamos el mapa para que luego automaticamente
                            // se llame a onMapReady()
                            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                            mapFragment.getMapAsync(mapCallBack);

                            // ponemos en COD_LOCALIDAD la población recién detectada
                            // COD_LOC_DRAWERLIST mantendrá la que se hubiera seleccionado en la lista
                            COD_LOCALIDAD = codpob;

                            Toolbar t = (Toolbar) findViewById(R.id.toolbar);
                            setSupportActionBar(t);
                            mViewPager.getAdapter().notifyDataSetChanged();

                            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(Constantes.SHARED_PREFS_FILE, 0).edit();
                            editor.putString(Constantes.SHARED_PREFS_ULTIMA_LOCALIDAD, COD_LOC_DRAWERLIST);
                            editor.commit();

                            // aunque grabamos en SharedPreferences la Localidad actual, no descargamos
                            // los datos de esta sino de COD_LOC_DRAWERLIST (la que el usuario hubiera
                            // seleccionado)
                            ServicioScrapMityc.startActionScrap(getApplicationContext(), COD_LOC_DRAWERLIST);
                        } catch (RegistroNoExistente rne) {
                            //log.log(Level.ALL, rne.getMessage());
                        }
                    }
                }
            }
        }
    }


    /*********************************************************************************
     ************************** La acción a realizar cuando FetchLocationIntentService
     ************************** tiene los datos preparados. (map listo) **************
     *********************************************************************************/
    @SuppressLint("ParcelCreator")
    class CoordenadasResultReceiver extends ResultReceiver {
        public CoordenadasResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Location mAnotherLocation = mLastLocation;
            CameraUpdate cameraUpdate;

            if (estacionVer == null) {

                // si necesitamos el mapa de una localidad distinta a la que estamos actualmente ...
                if (((!COD_LOC_DRAWERLIST.equals(COD_LOCALIDAD)) && (resultCode == Constantes.SUCCESS_RESULT))) {
                    mAnotherLocation = (Location) resultData.getParcelable("location");
                }

                // si no conseguimos una localización precisa retornamos sin hacer nada.
                if (mAnotherLocation == null)
                    return;

                // Si habíamos guardado de Provider el nombre de la población obtenido con GeoCodeHelper
                if (!mAnotherLocation.getProvider().equals("fused")) {
                    mostrarTituloBuscando(mAnotherLocation.getProvider());
                    preparaListaMenuDrawer();
                }
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mAnotherLocation.getLatitude(), mAnotherLocation.getLongitude()))
                        .title(mLastLocation.getProvider())); //buscarCodigoAPoblacion(COD_LOC_DRAWERLIST)));
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mAnotherLocation.getLatitude(), mAnotherLocation.getLongitude()), 12);
            } else {
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(estacionVer.getLocation().getLatitude(), estacionVer.getLocation().getLongitude()))
                        .title(estacionVer.getNombre())).showInfoWindow();
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(estacionVer.getLocation().getLatitude(), estacionVer.getLocation().getLongitude()), 16);
                estacionVer = null;  // vuelve a ser null hasta que el usuario vuelva a hacer click en una estación.
            }

            // Gets to GoogleMap from the MapView and does initialization stuff
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
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
            googleMap.setMyLocationEnabled(false);

            // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
            try {
                MapsInitializer.initialize(getApplicationContext());
                // Updates the location and zoom of the MapView
                googleMap.moveCamera(cameraUpdate);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
