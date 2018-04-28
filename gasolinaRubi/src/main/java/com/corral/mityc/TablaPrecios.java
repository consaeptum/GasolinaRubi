package com.corral.mityc;

import android.content.Context;
import android.location.Location;

import com.corral.mityc.estaciones.Estacion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by javier on 1/07/16.
 * La clase TablaPrecios contiene una lista múltiple para cada tipo de producto
 * que exista.
 * La lista resultados, sera un mapa de tipo <String, ArrayList<Estacion>>
 *     La clave será un nombre (código) de producto y el valor una lista de
 *     estaciones en el mismo orden que la lista de la web.
 * Esta clase se ocupa de realizar la petición http y cargar los datos que se
 * le requieran.
 */
public class TablaPrecios implements Serializable {


    // La lista de estaciones ordenadas y separadas por producto.
    private static Map<String, ArrayList<Estacion>> resultados;

    // La estación usada como referencia de mejor precio para cada lista.
    private Estacion estacionReferencia;

    // Contexto de Activity
    private static Context contexto;

    // guarda el boolean que indica si se ha utilizado el recuperCache o no, la última vez
    private static Boolean bCache = true;

    /**
     * constructor
     */
    public TablaPrecios(Context c) {
        resultados = null;
        estacionReferencia = null;
        contexto = c;
    }

    /**
     * Obtiene la Estacion en el mapa resultados que coincida con el nombre y
     * la dirección.  Si no existe devuelve null.
     */
    public Estacion getEstacion(String nom, String dir, String prod) {
        if (resultados != null) {
            ArrayList<Estacion> listaProducto = resultados.get(prod);
            if (listaProducto != null) {
                for (Estacion e : listaProducto) {
                    if (e.getId().equalsIgnoreCase(nom.concat(dir))) {
                        return e;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Obtiene el número de estaciones para un producto.
     */
    public int getNumeroEstaciones(String prod) {
        boolean existKey = (resultados != null) && (resultados.containsKey(prod));
        if (existKey) {
            List estaciones = resultados.get(prod);
            if (estaciones != null) {
                return estaciones.size();
            }

        }
        return 0;
    }

    /**
     * Obtiene el número de estaciones en total en la localidad seleccionada.
     */
    public int getTotalEstaciones() {
        int total = 0;
        for (String[] p: Constantes.LProductos) {
            // p[1] -> código producto (GA, SP95, ...)
            int n = getNumeroEstaciones(p[1]);
            if (n > total) total = n;
        }
        return total;
    }

    /**
     * Inicia resultados
     */
    private void iniciaResultados() {
        resultados = new HashMap<String, ArrayList<Estacion>>();
        for (String[] lp : Constantes.LProductos) {
            resultados.put(lp[1], new ArrayList<Estacion>());
        }
    }


    /**
     * método realizarPeticionWS
     * Llama a la clase WSJsonGetEstacionesPorPoblacionConAsyncTask.obtenEstacionsPor
     * de forma que se ejecuta la llamada al servicio web que nos devuelve la
     * lista de estaciones para una población.  Como esta lista incluye
     * latitud/longitud, tendremos todos los datos necesarios.
     * Devuelve true si toda la consulta es correcta, false en caso contrario.
     *
     * El servicio web es del tipo:
     *
     * {
         "Fecha":"Contenido de la cadena",
         "ListaEESSPrecio":[{
         "C.P.":"Contenido de la cadena",
         "Dirección":"Contenido de la cadena",
         "Horario":"Contenido de la cadena",
         "Latitud":"Contenido de la cadena",
         "Localidad":"Contenido de la cadena",
         "Longitud_x0020__x0028_WGS84_x0029_":"Contenido de la cadena",
         "Margen":"Contenido de la cadena",
         "Municipio":"Contenido de la cadena",
         "Precio_x0020_Biodiesel":"Contenido de la cadena",
         "Precio_x0020_Bioetanol":"Contenido de la cadena",
         "Precio_x0020_Gas_x0020_Natural_x0020_Comprimido":"Contenido de la cadena",
         "Precio_x0020_Gas_x0020_Natural_x0020_Licuado":"Contenido de la cadena",
         "Precio_x0020_Gases_x0020_licuados_x0020_del_x0020_petróleo":"Contenido de la cadena",
         "Precio_x0020_Gasoleo_x0020_A":"Contenido de la cadena",
         "Precio_x0020_Gasoleo_x0020_B":"Contenido de la cadena",
         "Precio_x0020_Gasolina_x0020_95_x0020_Protección":"Contenido de la cadena",
         "Precio_x0020_Gasolina_x0020__x0020_98":"Contenido de la cadena",
         "Precio_x0020_Nuevo_x0020_Gasoleo_x0020_A":"Contenido de la cadena",
         "Provincia":"Contenido de la cadena",
         "Remisión":"Contenido de la cadena",
         "Rótulo":"Contenido de la cadena",
         "Tipo_x0020_Venta":"Contenido de la cadena",
         "_x0025__x0020_BioEtanol":"Contenido de la cadena",
         "_x0025__x0020_Éster_x0020_metílico":"Contenido de la cadena",
         "IDEESS":"Contenido de la cadena",
         "IDMunicipio":"Contenido de la cadena",
         "IDProvincia":"Contenido de la cadena",
         "IDCCAA":"Contenido de la cadena"
     }],
     "Nota":"Contenido de la cadena",
     "ResultadoConsulta":"Contenido de la cadena"
     }
     *
     */
    public synchronized boolean realizarPeticionJSON(String jsonEstaciones) {
        String codLocalidad = "";
        iniciaResultados();
        for (String[] lp: Constantes.LProductos) {
            ArrayList<Estacion> alEstacion = resultados.get(lp[1]);
            try {
                JSONObject jsonSource = new JSONObject(jsonEstaciones);
                JSONArray jsonArray = jsonSource.getJSONArray("ListaEESSPrecio");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject est = jsonArray.getJSONObject(i);

                    String nom = est.getString("Rótulo");
                    String dir = est.getString("Dirección");

                    if (codLocalidad.isEmpty()) {
                        codLocalidad = est.getString("IDMunicipio");
                    }

                    Estacion e = new Estacion(nom, dir);

                    if (lp[1].equals(Constantes.GA)) { e.addProducto(lp[1], est.getString(Constantes.GA_JSON) ); }
                    if (lp[1].equals(Constantes.GAN)) { e.addProducto(lp[1], est.getString(Constantes.GAN_JSON) ); }
                    if (lp[1].equals(Constantes.SP95)) { e.addProducto(lp[1], est.getString(Constantes.SP95_JSON) ); }
                    if (lp[1].equals(Constantes.SP98)) { e.addProducto(lp[1], est.getString(Constantes.SP98_JSON) ); }

                    Location l = new Location(nom.concat(dir));
                    try {
                        l.setLatitude(Double.parseDouble(est.getString("Latitud").replace(',','.')));
                        l.setLongitude(Double.parseDouble(est.getString("Longitud (WGS84)").replace(',','.')));
                    } catch (NumberFormatException nfe) {
                        l.setLatitude(0d);
                        l.setLongitude(0d);
                    }
                    e.setLocation(l);

                    if (e.getProducto(lp[1]).getPrecio() > 0) alEstacion.add(e);
                }

            } catch (Exception e) {
                bCache = true;
                return false;
            }
            Collections.sort(alEstacion);
        }
        // Si hay al menos una estación es correcto
        if (getTotalEstaciones() > 0) {
            guardaCache(codLocalidad);
            bCache = false;
            return true;

            // Si no hay ninguna estación, es que se inicio resultados y continúa vacío.
        } else {
            bCache = true;
            return false;
        }
    }



    /**
     * guarda resultados serializado en el fichero "<codLocalidad>.cache"
     */
    private Boolean guardaCache(String codlocalidad) {
        FileOutputStream fos;
        ObjectOutputStream os;
        try {
            fos = contexto.openFileOutput(codlocalidad + ".cache", contexto.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(resultados);
            os.close();
            fos.close();
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * recupera el ArrayList correspondiente a producto de la localidad indicada.
     */
    public synchronized Map<String, ArrayList<Estacion>> recuperaCache(String codLocalidad, Context c) {
        Map<String, ArrayList<Estacion>> res = null;
        try {

            FileInputStream fis = c.openFileInput(codLocalidad + ".cache");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream is = new ObjectInputStream(bis);

            res = (Map<String, ArrayList<Estacion>>) is.readObject();
            is.close();
            fis.close();
            resultados = res;
            bCache = true;
            return res;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    /**
     * Devuelve la array de Estaciones para un producto.
     */
    public ArrayList<Estacion> getEstaciones(String codLocalidad, String prod) {
        if ((resultados != null) && (resultados.containsKey(prod))) {
            if (resultados.get(prod).size() > 0) {
                return resultados.get(prod);
            } else {
                Map<String, ArrayList<Estacion>> res = recuperaCache(codLocalidad, contexto);
                if (res != null) {
                    return res.get(prod); //recuperaCache(localidad).get(prod);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Indica si getEstaciones devolvió la tabla resultado directamente o usando
     * recuperaCache.
     */
    public Boolean usandoCache() {
        return bCache;
    }
}
