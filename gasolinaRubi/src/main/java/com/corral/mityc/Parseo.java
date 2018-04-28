package com.corral.mityc;

import com.corral.mityc.excepciones.RegistroNoExistente;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.corral.mityc.Constantes.codigosProvincia;


/**
 * Created by javier on 30/08/16.
 */
public class Parseo {


    /**
     * Obtenemos el código de la provincia.
     * @param provincia
     * @return El código de la provincia (dos dígitos numéricos) o null si no
     * la encuentra.
     */
    public static String buscarCodigoProvincia(String provincia) {

        provincia = sinAcentos(provincia);

        // recorrer la lista de provincias.
        for (String[] p: codigosProvincia) {

            if (sinAcentos(p[0]).equalsIgnoreCase(provincia)) return p[1];
        }
        return null;
    }

    /**
     * Obtenemos la provincia dado un código
     * @param provinciaCodigo
     * @return El nombre de la provincia (en formato 2 dígitos ) o null si no la encuentra.
     */
    public static String buscarProvinciaCodigo(String provinciaCodigo) {

        // recorrer la lista de provincias.
        for (String[] p: codigosProvincia) {

            if ((p[1]).equals(provinciaCodigo)) return p[0];
        }
        return null;
    }


    /**
     * Obtenemos el código de la población a partir de los parámetros
     * codProvincia y población.
     * @param hm La lista de codigos y poblaciones mityc
     * @param poblacion
     * @return Devuelve el código de la población como String de dígitos numéricos
     * o null si no la encuentra.
     */
    public static String buscarCodigoPoblacionMityc(HashMap<String, String> hm, String poblacion)
            throws RegistroNoExistente {
        String[] poblSplited = poblacion.split(" ");
        String codPoblacion = "";
        // Map<codigo_poblacion, numero_coincidencias>
        Map<String, Integer> coincidencias = new HashMap<String, Integer>();

        // recorremos los elementos del array con igual codProvincia
        for (String cod: hm.keySet()) {
            String toponimioMityc = hm.get(cod);

            String[] pobOrigen = toponimioMityc.split(" ");
            // recorrer las palabras de una poblacion de la lista.
            for (String o : pobOrigen) {
                //
                // para cada palabra de poblacion a buscar comparamos con o
                for (String b : poblSplited) {
                    //
                    if (sinAcentos(b).equalsIgnoreCase(sinAcentos(o))) {
                        //
                        if (!coincidencias.containsKey(cod)) {
                            coincidencias.put(cod, 1);
                        } else {
                            coincidencias.put(cod, coincidencias.get(cod) + 1);
                        }
                    }
                }
            }
        }
        // finalizamos con HashMap coincidencias conteniendo los resultados.
        // si hay un solo resultado con valor máximo, correcto..
        return codResultTablaCoincidenciasMityc(hm, coincidencias, poblacion);
    }


    /*
 * Analiza la tabla de coincidencias y devuelve el código que tiene más coincidencias
 *
 */
    public static String codResultTablaCoincidenciasMityc(Map<String, String> hm, Map<String, Integer> coincidencias,
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
                    String nompob = "";
                    for (String cl: hm.keySet()) {
                        if (cl.equals(k)) {
                            nompob = hm.get(cl);
                            break;
                        }
                    }
                    if (cuentaPalabras(nompob) ==
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
                        "((^|\\s)L')|" +
                        "((^|\\s)N')|" +
                        "((^|\\s)D')";
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

    public static String quitarPreposiciones(String as) {
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
                        "((^|\\s)L')|" +
                        "((^|\\s)N')|" +
                        "((^|\\s)D')";
        String sinPreps = as.toUpperCase().replaceAll(patronPreposiciones, " ");
        sinPreps.trim();
        return sinPreps;
    }
}


