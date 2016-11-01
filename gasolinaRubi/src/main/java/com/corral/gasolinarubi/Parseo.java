package com.corral.gasolinarubi;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;

import com.opencsv.CSVReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.corral.gasolinarubi.Constantes.codigosPoblacion;
import static com.corral.gasolinarubi.Constantes.codigosProvincia;
import static com.corral.gasolinarubi.Constantes.log;


/**
 * Created by javier on 30/08/16.
 */
public class Parseo {

    /**
     * Carga los datos del archivo poblaciones.csv en el ArrayList codigosPoblacion.
     * El formato que tendrán los datos en cada elemento son:
     * <codigo_provincia>,<nombre_poblacion>,<codigo_poblacion>
     * @param context
     */
    public static void cargaCodigosPoblacion(Context context) {
        AssetManager assetManager = context.getAssets();

        try {
            InputStream csvStream = assetManager.open("poblaciones.csv");
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader, '|');
            String[] line;

            // throw away the header
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                codigosPoblacion.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Obtenemos el nombre de la localidad a partir del código
     * @param codigo
     * @return El nombre de la localidad o null si no lo encuentra.
     */
    public static String buscarCodigoAPoblacion(String codigo) {

        Map<String, Integer> coincidencias = new HashMap<String, Integer>();
        // recorrer la lista de provincias.
        Iterator i = codigosPoblacion.iterator();
        while (i.hasNext()) {
            String[] s = (String[]) i.next();
            if (codigo.equals(s[2])) {
                return s[1];
            }
        }
        return "";
    }


    /**
     * Obtenemos el nombre de la provincia a partir del código de población
     * @param codigo población
     * @return El nombre de la provincia o null si no lo encuentra.
     */
    public static String buscarCodigoAProvincia(String codigo) {

        Map<String, Integer> coincidencias = new HashMap<String, Integer>();
        // recorrer la lista de provincias.
        Iterator i = codigosPoblacion.iterator();
        while (i.hasNext()) {
            String[] s = (String[]) i.next();
            if (codigo.equals(s[2])) {
                String codprov = (s[0].length() > 1)? s[0] : "0" + s[0];
                int j = 0;
                while (!codigosProvincia[j][1].equals(codprov)) {
                    j++;
                }
                return codigosProvincia[j][0];
            }
        }
        return "";
    }

    /**
     * Obtenemos la lista de poblaciones para la provincia dada.
     * @param provincia código de provincia
     * @return La lista de las poblaciones de esa provincia.
     */
    public static List<String> listaNomPobXProv(String provincia) {

        if (provincia.startsWith("0")) provincia = provincia.substring(1);
        List<String> poblaciones = new ArrayList<String>();
        // recorrer la lista de provincias.
        Iterator i = codigosPoblacion.iterator();
        while (i.hasNext()) {
            String[] s = (String[]) i.next();
            if (provincia.equals(s[0])) {
                poblaciones.add(s[1]);
            }
        }
        return poblaciones;
    }


    /**
     * Obtenemos el código de la provincia.
     * @param provincia
     * @return El código de la provincia (dos dígitos numéricos) o null si no
     * la encuentra.
     */
    public static String buscarCodigoProvincia(String provincia) {
        String[] provSplited = provincia.split(" ");

        String codProvincia = "";
        Map<String, Integer> coincidencias = new HashMap<String, Integer>();
        // recorrer la lista de provincias.
        for (String[] p: codigosProvincia) {
            String[] provOrigen = p[0].split(" ");
            // recorrer las palabras de una provincia de la lista.
            for (String o: provOrigen) {
                // para cada palabra de provincia a buscar comparamos con o
                for (String b: provSplited) {
                    if (sinAcentos(b).equalsIgnoreCase(sinAcentos(o))) {
                        if (!coincidencias.containsKey(p[1])) {
                            coincidencias.put(p[1], 1);
                        } else {
                            coincidencias.put(p[1], coincidencias.get(p[1])+1);
                        }
                    }
                }
            }
        }
        // finalizamos con HashMap coincidencias conteniendo los resultados.
        // si hay un solo resultado con valor máximo, correcto..
        try {
            return codResultTablaCoincidencias(coincidencias, provincia);
        } catch (RegistroNoExistente rne) {
            return null;
        }
    }


    /**
     * Obtenemos el código de la población a partir de los parámetros
     * codProvincia y población.
     * @param codProvincia sin ceros delante.
     * @param poblacion
     * @return Devuelve el código de la población como String de dígitos numéricos
     * o null si no la encuentra.
     */
    public static String buscarCodigoPoblacion(String codProvincia, String poblacion) throws RegistroNoExistente {
        String[] poblSplited = poblacion.split(" ");
        String codPoblacion = "";
        // Map<codigo_poblacion, numero_coincidencias>
        Map<String, Integer> coincidencias = new HashMap<String, Integer>();

        // recorremos los elementos del array con igual codProvincia
        for (String[] linea: codigosPoblacion) {
            //
            if (linea[0].equals(codProvincia)) {
                String[] pobOrigen = linea[1].split(" ");
                // recorrer las palabras de una poblacion de la lista.
                for (String o: pobOrigen) {
                    //
                    // para cada palabra de poblacion a buscar comparamos con o
                    for (String b: poblSplited) {
                        //
                        if (sinAcentos(b).equalsIgnoreCase(sinAcentos(o))) {
                            //
                            if (!coincidencias.containsKey(linea[2])) {
                                coincidencias.put(linea[2], 1);
                            } else {
                                coincidencias.put(linea[2], coincidencias.get(linea[2])+1);
                            }
                        }
                    }
                }
            }
        }
        // finalizamos con HashMap coincidencias conteniendo los resultados.
        // si hay un solo resultado con valor máximo, correcto..
        return codResultTablaCoincidencias(coincidencias, poblacion);
    }


    /*
     * Analiza la tabla de coincidencias y devuelve el código que tiene más coincidencias
     *
     */
    public static String codResultTablaCoincidencias(Map<String, Integer> coincidencias,
                                                     String poblacion) throws RegistroNoExistente {
        int max = 0;
        String codigo = "";
        for (String k: coincidencias.keySet()) {
            int actual = coincidencias.get(k);
            if (actual > max) {
                max = actual;
                codigo = k;
            } else {
                if (actual == max) {
                    if (cuentaPalabras(buscarCodigoAPoblacion(k)) ==
                            (cuentaPalabras(poblacion) - cuentaPreposiciones(poblacion))) {
                        codigo = k;
                    }
                }
            }
        }
        if (max > 0) {
            return codigo;
        } else {
            throw new RegistroNoExistente();
        }
    }


    /*
        usamos el api de maps con http.
        esquema:
        http://maps.google.com/maps/api/geocode/json?latlng=41.4833,2.0333&sensor=true
     */
    public static String getAddress(Double lat, Double lng) throws FalloConexion, RegistroNoExistente {
        String urlLatLng = "?latlng=" + lat.toString() + "," + lng.toString() + "&sensor=true";
        String urlTotal = "http://maps.google.com/maps/api/geocode/json" + urlLatLng;
        urlTotal = urlTotal.replace(" ", "+");

        try {
            URL url = new URL(urlTotal);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(10000);
            InputStream url_in = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(url_in));

            String strTemp = "";
            String strFinal = "";
            while (null != (strTemp = br.readLine())) {
                strFinal = strFinal.concat(strTemp);
            }

            JSONObject jsonaddress = new JSONObject(strFinal);
            JSONArray jRes = jsonaddress.getJSONArray("results");
            JSONObject jLocat = jRes.getJSONObject(0);
            String direccion = jLocat.getString("formatted_address");

            return direccion;
        } catch (MalformedURLException mfue) {
            log.log(Level.ALL, mfue.getMessage());
        } catch (JSONException e) {
            log.log(Level.ALL, e.getMessage());
        } catch (IOException ioe) {
            throw new FalloConexion();
        }

        throw new RegistroNoExistente();
    }


    /*
    usamos el api de maps con http.
    esquema:
    http://maps.google.com/maps/api/geocode/json?latlng=41.4833,2.0333&sensor=true
    */
    public static Location getLocation(String localidad) throws FalloConexion, RegistroNoExistente {
        String urlLocalidad = "?address=" + buscarCodigoAPoblacion(localidad)
                + "," + buscarCodigoAProvincia(localidad) +"&sensor=true";
        String urlTotal = "http://maps.google.com/maps/api/geocode/json" + urlLocalidad;
        urlTotal = urlTotal.replace(" ", "+");

        try {
            URL url = new URL(urlTotal);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(10000);
            InputStream url_in = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(url_in));

            String strTemp = "";
            String strFinal = "";
            while (null != (strTemp = br.readLine())) {
                strFinal = strFinal.concat(strTemp);
            }

            JSONObject jsonaddress = new JSONObject(strFinal);
            JSONArray jRes = jsonaddress.getJSONArray("results");
            JSONObject jLocat = jRes.getJSONObject(0);
            JSONObject jLocation = jLocat.getJSONObject("geometry");
            JSONObject jloc = jLocation.getJSONObject("location");
            Location loc = new Location(localidad);
            loc.setLatitude(jloc.getDouble("lat"));
            loc.setLongitude(jloc.getDouble("lng"));
            String direccion = jLocat.getString("formatted_address");
            Pattern p = Pattern.compile("((^[0-9]{5} )|^)(.*), (.*), (.*)$");
            Matcher m = p.matcher(direccion);

            // si hay coincidencia, procedemos, si no, probamos en el caso que sea una provincia
            // y Google devolviera el formato "Provincia, Spain".
            if (m.matches()) {
                loc.setProvider(m.group(3));
            } else {
                p = Pattern.compile("(.*), (.*)");
                m = p.matcher(direccion);
                if (m.matches()) {
                    loc.setProvider(m.group(1));
                }
            }
            return loc;
        } catch (MalformedURLException mfue) {
            log.log(Level.ALL, mfue.getMessage());
        } catch (JSONException e) {
            log.log(Level.ALL, e.getMessage());
        } catch (IOException ioe) {
            throw new FalloConexion();
        }
        throw new RegistroNoExistente();
    }


    /*
     * Devuelve el número de palabras
     */
    public static Integer cuentaPalabras(String s) {
        int word = 1;
        for(int i=0;i<s.length();++i)
        {
            if((s.charAt(i)==' ') && (s.charAt(i-1)!=' '))
                word++;
        }
        return word;
    }


    public static Integer cuentaPreposiciones(String as) {
        String patronPreposiciones =
                "((^|\\s)EL($|\\s))|" +
                        "((^|\\s)LOS($|\\s))|" +
                        "((^|\\s)LA($|\\s))|" +
                        "((^|\\s)LAS($|\\s))|" +
                        "((^|\\s)O($|\\s))|" +
                        "((^|\\s)DO($|\\s))|" +
                        "((^|\\s)DAS($|\\s))|" +
                        "((^|\\s)A($|\\s))|" +
                        "((^|\\s)AS($|\\s))|" +
                        "((^|\\s)EN($|\\s))|" +
                        "((^|\\s)DE($|\\s))|" +
                        "((^|\\s)DEL($|\\s))|" +
                        "((^|\\s)DELS($|\\s))|" +
                        "((^|\\s)LES($|\\s))|" +
                        "((^|\\s)ELS($|\\s))|" +
                        "((^|\\s)L'($|\\s))|" +
                        "((^|\\s)N'($|\\s))|" +
                        "((^|\\s)D'($|\\s))";
        Pattern p = Pattern.compile(patronPreposiciones);
        Matcher m = p.matcher(as);
        int n;
        for (n = 0; m.find(); n++);
        return n;
    }


    /**
     * Quitamos acentos, tildes, / , - , (, ) para poder comparar sin problemas
     * los nombres de población.
     * @param s
     * @return la cadena sin tildes ni signos.
     */
    public static String sinAcentos(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replace('(', ' ');
        s = s.replace(')', ' ');
        s = s.replace('-', ' ');
        s = s.replace('/', ' ');
        s = s.trim();
        return s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }


}
