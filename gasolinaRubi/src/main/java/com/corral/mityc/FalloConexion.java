package com.corral.mityc;

/**
 * Created by javier on 18/07/16.
 */
public class FalloConexion extends Exception {
    private static final long serialVersionUID = 10L;

    public FalloConexion() {
    }

    public FalloConexion(String mensaje) {
        super(mensaje);
    }
}
