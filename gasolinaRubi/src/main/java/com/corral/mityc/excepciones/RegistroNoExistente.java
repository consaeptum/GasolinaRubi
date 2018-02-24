package com.corral.mityc.excepciones;

/**
 * Created by javier on 18/07/16.
 */
public class RegistroNoExistente extends Exception {
    private static final long serialVersionUID = 10L;

    public RegistroNoExistente() {
    }

    public RegistroNoExistente(String mensaje) {
        super(mensaje);
    }
}
