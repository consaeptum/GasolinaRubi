package com.corral.mityc.estaciones;

import android.location.Location;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by javier on 1/07/16.
 * La clase Estacion contiene el nombre, dirección y una lista de los productos
 * que vende.
 */
public class Estacion implements Serializable, Comparable<Estacion> {

    /**
     * El nombre de la estación (Las multinacionales tienen el mismo siempre.
     */
    private String nombre;

    /**
     * La dirección (calle nº, carretera km, etc)
     */
    private String direccion;

    /**
     * La lista de productos de que dispone esta estación.
     */
    private List<Producto> productos = new ArrayList<>();

    /**
     * Latitud y longitud
     */
    private transient Location location;
    private double latitud, longitud;

    /**
     * Constructor
     */
    public Estacion(String nom, String dir) {
        nombre = nom;
        direccion = dir;
    }

    /**
     * Obtiene el nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la dirección
     */
    public String getDireccion() {
        return direccion;
    }

    public Location getLocation() {
        location = new Location(getNombre());
        location.setLatitude(latitud);
        location.setLongitude(longitud);
        return location;
    }

    public void setLocation(Location location) {
        latitud = location.getLatitude();
        longitud = location.getLongitude();
    }



    /**
     * Obtiene la unión de nombre+direccion como identificador único.
     */
    public String getId() {
        return nombre.concat(direccion);
    }

    /**
     * Obten el número de productos en total
     */
    public int getNumeroProductos() {
        return productos.size();
    }

    /**
     * Obten el objeto que coincida con el nombre o null.
     */
    public Producto getProducto(String nom) {
        for (Producto p: productos) {
            if (p.getNombre().equalsIgnoreCase(nom)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Añade un producto.  Si el producto ya existe, lo modifica.
     * Si pvp es nulo , no hace nada.
     */
    public void addProducto(String nom, String pvp) {
        if (pvp == null) return;
        Producto p = getProducto(nom);
        pvp = pvp.trim().replace("€", "").replace(",", ".");
        Float f;
        try {
            f = Float.parseFloat(pvp);
        } catch(NumberFormatException nfe) {
            f = 0f;
        }
        if ( p != null ) {
            ((Producto) p).setPrecio(f);
        } else {
            productos.add(new Producto(nom, f));
        }
    }

    /**
     * Devuelve El nombre y los precios de productos separados por '\n'
     * @return
     */
    public String getResumen() {
        String r = getNombre() + '\n';
        for (Producto p: productos) {
            r = r.concat(p.getNombre() + ' ' + p.getPrecioFormat() + '\n');
        }
        return r;
    }


    /**
     * Compara en función del precio.
     * Solo funciona si sólo hay un articulo, en caso contrario no puede decidir
     * qué precio de qué artículo comparar.
     * Es útil por que en Tabla precios se maneja una lista por producto, así que cada
     * estación en esa lista contiene un sólo producto.
     * @param o
     * @return El resultado de comparar el precio
     */
    @Override
    public int compareTo(@NonNull Estacion o) {
        if ((o.getNumeroProductos() == 1) && (getNumeroProductos() == 1)) {
            if (o.productos.get(0).getPrecio() == productos.get(0).getPrecio()) return 0;
            return (o.productos.get(0).getPrecio() < productos.get(0).getPrecio())?1:-1;
        } else {
            return 0;
        }
    }
}

