package ar.edu.utn.frc.backend.tpi.transportes.entity;

/**
 * Tipos de tramo en una ruta de transporte.
 * Define los diferentes segmentos que puede tener un traslado.
 */
public enum TipoTramo {

    /**
     * Tramo directo desde origen hasta destino final.
     * Se usa cuando no hay depósitos intermedios.
     */
    ORIGEN_DESTINO,

    /**
     * Tramo desde origen hasta un depósito.
     * Primer tramo en rutas con depósitos.
     */
    ORIGEN_DEPOSITO,

    /**
     * Tramo entre dos depósitos.
     * Usado en rutas con múltiples depósitos.
     */
    DEPOSITO_DEPOSITO,

    /**
     * Tramo desde un depósito hasta el destino final.
     * Último tramo en rutas con depósitos.
     */
    DEPOSITO_DESTINO,

    /**
     * Tramo de retorno desde destino hacia origen.
     * Usado para devoluciones o viajes de regreso.
     */
    DESTINO_ORIGEN

}
