package com.corral.mityc;

import android.content.Context;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    TablaPrecios(Context c) {
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
     * método realizarPeticionHttp
     * recibe tipoProducto según el LProductos[0]
     * primero envía la petición, después comprueba si el código de status es
     * correcto, en cuyo caso guarda en htmlRequest la página web leída.
     * Si el status code es incorrecto, htmlRequest queda a None indicando que
     * hubo error.
     */
    public synchronized boolean realizarPeticionHttp(String codLocalidad) {
        iniciaResultados();
        for (String[] lp: Constantes.LProductos) {
            String urlTotal = Constantes.url1 + lp[0] + Constantes.urlLocalidad + codLocalidad;
            ArrayList<Estacion> alEstacion = resultados.get(lp[1]);
            try {
                Connection.Response response = Jsoup.connect(urlTotal).method(Connection.Method.GET).execute();

                String body = response.body();
                BufferedReader reader = new BufferedReader(new StringReader(body));
                String linea = null;
                boolean inTabla = false;
                String tabla = "";

                Pattern patronNombre = Pattern.compile(".*href=.*>(.*)</a>");
                Pattern patronDireccion = Pattern.compile(".*<td>(.*)</td>");
                Pattern patronPrecio = Pattern.compile(".*<td><span>(.*)</span.*</td>");
                Matcher m;
                int nivel = 0;
                String nom = "";
                String dir = "";
                String pvp = "";

                while ((linea = reader.readLine()) != null) {
                    if (linea.contains("<table class=\"tableResults\">")) {
                        inTabla = true;
                    }
                    if (inTabla) {
                        if (linea.contains("</table>")) break;

                        m = patronNombre.matcher(linea);
                        if (m.matches()) {
                            nivel = 1;
                            nom = m.group(1);
                        }
                        if (nivel == 1) {
                            m = patronDireccion.matcher(linea);
                            if (m.matches()) {
                                nivel = 2;
                                dir = m.group(1);
                            }
                        }
                        if (nivel == 2) {
                            m = patronPrecio.matcher(linea);
                            if (m.matches()) {
                                nivel = 0;
                                pvp = m.group(1);
                                Estacion e = new Estacion(nom, dir);
                                e.addProducto(lp[1], pvp);
                                alEstacion.add(e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                bCache = true;
                return false;
            }
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
    private Boolean guardaCache(String localidad) {
        FileOutputStream fos;
        ObjectOutputStream os;
        try {
            fos = contexto.openFileOutput(localidad + ".cache", contexto.MODE_PRIVATE);
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
