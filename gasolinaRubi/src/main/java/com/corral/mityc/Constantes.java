package com.corral.mityc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Constantes {

    public static final Logger log= Logger.getLogger(Constantes.class.getName());
    public static final String URL_REVERSE_GEOCODING = "http://services.gisgraphy.com/reversegeocoding/search?format=json";
    public static final String MAPBOX_ACCESS_TOKEN = "com.mapbox.geocoder.android.AndroidGeocoder";
    // claves SharedPreferences
    public static final String SHARED_PREFS_ULTIMA_LOCALIDAD = "ULTIMA_LOCALIDAD";
    public static final String SHARED_PREFS_FILE = "SH_FILE";

    // Defines a custom Intent action
    public static final String BROADCAST_ACTION =
            "com.corral.gasolinarubi.BROADCAST";
    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA =
            "TablaPrecios";
    public static final String ACTION_SCRAP = "com.corral.gasolinarubi.action.SCRAP";
    public static final String PARAMETRO_COD_LOCALIDAD = "COD_LOCALIDAD";

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";


    /**
     * los productos existentes.
     * {"código_en_mityc", "código_en_la_aplicación", "descripción"}
     */
    public static final String[][] LProductos = {
            {"4", "GA", "gasóleoa A"},
            {"5", "GAN", "gasóleo A Nuevo"},
            {"1", "SP95", "sin plomo 95"},
            {"3", "SP98"," sin plomo 98"}
    };

    public static final String url1 =
            "http://geoportalgasolineras.es/geoportalmovil/eess/search.do?tipoCarburante=";
    public static final String url2 = "&rotulo=&venta=T&provincia=";
    //public static final String urlProvincia = "08";
    public static final String url3 = "&localidad=&calle=&numero=&codPostal=";
    public static final String urlLocalidad = "&venta=T&localidad=";

    public static final String[][] codigosProvincia = {
            {"ÁLAVA", "01"},
            {"ALBACETE", "02"},
            {"ALICANTE", "03"},
            {"ALMERÍA", "04"},
            {"ASTURIAS", "33"},
            {"ÁVILA", "05"},
            {"BADAJOZ", "06"},
            {"ILLES BALEARS", "07"},
            {"BARCELONA", "08"},
            {"BURGOS", "09"},
            {"CÁCERES", "10"},
            {"CÁDIZ", "11"},
            {"CANTABRIA", "39"},
            {"CASTELLÓ", "12"},
            {"CEUTA", "51"},
            {"CIUDAD REAL", "13"},
            {"CÓRDOBA", "14"},
            {"A CORUÑA", "15"},
            {"CUENCA", "16"},
            {"GIRONA", "17"},
            {"GRANADA", "18"},
            {"GUADALAJARA", "19"},
            {"GUIPÚZCOA", "20"},
            {"HUELVA", "21"},
            {"HUESCA", "22"},
            {"JAÉN", "23"},
            {"LEÓN", "24"},
            {"LLEIDA", "25"},
            {"LUGO", "27"},
            {"MADRID", "28"},
            {"MÁLAGA", "29"},
            {"MELILLA", "52"},
            {"MURCIA", "30"},
            {"NAVARRA", "31"},
            {"OURENSE", "32"},
            {"PALENCIA", "34"},
            {"LAS PALMAS", "35"},
            {"PONTEVEDRA", "36"},
            {"LA RIOJA", "26"},
            {"SALAMANCA", "37"},
            {"SANTA CRUZ DE TENERIFE", "38"},
            {"SEGOVIA", "40"},
            {"SEVILLA", "41"},
            {"SORIA", "42"},
            {"TARRAGONA", "43"},
            {"TERUEL", "44"},
            {"TOLEDO", "45"},
            {"VALÈNCIA", "46"},
            {"VALLADOLID", "47"},
            {"VIZCAYA", "48"},
            {"ZAMORA", "49"},
            {"ZARAGOZA", "50"}
    };

    public static final List<String[]> codigosPoblacion = new ArrayList<String[]>();
    public static final String SHARED_LISTA_CODIGOS_POBLACIONES = "LISTA_POBLACIONES";

}
