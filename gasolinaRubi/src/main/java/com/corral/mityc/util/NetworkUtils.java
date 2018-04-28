/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.corral.mityc.util;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Estas utilidades serán utilizadas para conectar con el servidor Mityc
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String MITYC_BASE_URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes";

    private static final String LISTADOS = "Listados";
    private static final String ESTACIONES = "EstacionesTerrestres";
    private static final String FILTROMUNICIPIO = "FiltroMunicipio";
    private static final String CONSULTA = "MunicipiosPorProvincia";

    /* The format we want our API to return */
    private static final String format = "json";


    /**
     * Construye la URL necesaria para la consulta de municipios de una provincia.
     *
     * @param provincia El código de la provincia a consultar (2 dígitos)
     * @return La Url para usar en la consulta a Mityc.
     */
    public static URL buildUrlMuniciposPorProvincia(String provincia) {
        Uri mitycQueryUri = Uri.parse(MITYC_BASE_URL).buildUpon()
                .appendPath(LISTADOS)
                .appendPath(CONSULTA)
                .appendPath(provincia)
                .build();

        try {
            URL mitycQueryUrl = new URL(mitycQueryUri.toString());
            return mitycQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Construye la URL usada para comunicarse con el servidor Mityc y obtener
     * las estaciones de una población.
     *
     * @param poblacion El código de la población a consultar.
     * @return La URL para enviar la consulta a mityc.
     */
    public static URL buildUrlEstacionesPorPoblacion(String poblacion) {
        Uri mitycQueryUri = Uri.parse(MITYC_BASE_URL).buildUpon()
                .appendPath(ESTACIONES)
                .appendPath(FILTROMUNICIPIO)
                .appendPath(poblacion)
                .build();

        try {
            URL mitycQueryUrl = new URL(mitycQueryUri.toString());
            return mitycQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Este método devuelve el resultado completo de la respuesta Http.
     *
     * @param url La URL a consultar
     * @return El contenido de la respuesta Http, null si no hay respuesta.
     * @throws IOException Relativo a la red y a la lectura de los datos.
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } catch (Exception e) {
            return null;
        } finally {
            urlConnection.disconnect();
        }
    }
}