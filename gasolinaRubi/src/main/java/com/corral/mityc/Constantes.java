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

    public static final String urlPrecios =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburante/PreciosCarburantes/" +
                    "EstacionesTerrestres/FiltroMunicipio/9999 ?";

    /**
     * {nombre_provincia, código_provincia_JS, código_provincia_REST}
     *
     * JS indica el código en la web móvil de Mityc mientras que REST indica el código
     * de la provincia en los servicios REST de Mityc.
     *
     */
    public static final String[][] codigosProvincia = {
            {"ÁLAVA", "01", "0116"},
            {"ALBACETE", "02", "0207"},
            {"ALICANTE", "03", "0310"},
            {"ALMERÍA", "04", "0401"},
            {"ASTURIAS", "33", "3303"},
            {"ÁVILA", "05", "0508"},
            {"BADAJOZ", "06", "0611"},
            {"ILLES BALEARS", "07", "0704"},
            {"BARCELONA", "08", "0809"},
            {"BURGOS", "09", "0908"},
            {"CÁCERES", "10", "1011"},
            {"CÁDIZ", "11", "1101"},
            {"CANTABRIA", "39", "3906"},
            {"CASTELLÓ", "12", "1210"},
            {"CEUTA", "51", "5118"},
            {"CIUDAD REAL", "13", "1307"},
            {"CÓRDOBA", "14", "1307"},
            {"A CORUÑA", "15", "1512"},
            {"CUENCA", "16", "1607"},
            {"GIRONA", "17", "1709"},
            {"GRANADA", "18", "1801"},
            {"GUADALAJARA", "19", "1907"},
            {"GUIPÚZCOA", "20", "2016"},
            {"HUELVA", "21", "2101"},
            {"HUESCA", "22", "2202"},
            {"JAÉN", "23", "2301"},
            {"LEÓN", "24", "2408"},
            {"LLEIDA", "25", "2509"},
            {"LUGO", "27", "2712"},
            {"MADRID", "28", "2813"},
            {"MÁLAGA", "29", "2901"},
            {"MELILLA", "52", "5219"},
            {"MURCIA", "30", "3014"},
            {"NAVARRA", "31", "3115"},
            {"OURENSE", "32", "3212"},
            {"PALENCIA", "34", "3408"},
            {"LAS PALMAS", "35", "3505"},
            {"PONTEVEDRA", "36", "3612"},
            {"LA RIOJA", "26", "2617"},
            {"SALAMANCA", "37", "3708"},
            {"SANTA CRUZ DE TENERIFE", "38", "3805"},
            {"SEGOVIA", "40", "4008"},
            {"SEVILLA", "41", "4101"},
            {"SORIA", "42", "4208"},
            {"TARRAGONA", "43", "4309"},
            {"TERUEL", "44", "4402"},
            {"TOLEDO", "45", "4507"},
            {"VALÈNCIA", "46", "4610"},
            {"VALLADOLID", "47", "4708"},
            {"VIZCAYA", "48", "4816"},
            {"ZAMORA", "49", "4908"},
            {"ZARAGOZA", "50", "5002"}
    };

    public static final List<String[]> codigosPoblacion = new ArrayList<String[]>();
    public static final String SHARED_LISTA_CODIGOS_POBLACIONES = "LISTA_POBLACIONES";

}
