package com.corral.mityc.estaciones;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by javier on 1/07/16.
 * La clase Estacion contiene el nombre, dirección y una lista de los productos
 * que vende.
 */
public class Estacion implements Serializable {

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
    private Location location;

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
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
     */
    public void addProducto(String nom, String pvp) {
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


}

