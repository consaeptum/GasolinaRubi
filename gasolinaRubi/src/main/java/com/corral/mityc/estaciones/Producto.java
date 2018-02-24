package com.corral.mityc.estaciones;

import java.io.Serializable;

/**
 * Created by javier on 1/07/16.
 * La clase Producto contiene la información del nombre del producto, el precio
 * y la diferencia al precio de referencia (el cual es externo).
 * Cada estación debe tener un objeto Producto por cada producto de carburante
 * que venda.
 */
public class Producto implements Serializable {

    // El nombre del producto.
    private String nombre;

    // El precio de este producto en concreto
    private float precio;

    // La diferencia de este precio respecto al precio de referencia externo.
    private float diff;

    // byte ordinal, no es necesario en java.

    /**
     * Constructor
     */
    public Producto(String nom, float pvp) {
        nombre = nom;
        precio = pvp;
        diff = 0;
    }

    public float getPrecio() {
        return precio;
    }

    public String getPrecioFormat() {
        return String.format("%.3f", precio);
    }

    public String getNombre() {
        return nombre;
    }

    public void setPrecio(float pvp) {
        precio = pvp;
    }

    /**
     * calcularDiferencia asigna valor a diff y lo retorna al mismo tiempo.
     * @param pvpRef
     * @return Devuelve la diferencia restando pvpRef a precio.
     */
    public float calcularDiferencia(float pvpRef) {
        diff = precio - pvpRef;
        return diff;
    }
}
