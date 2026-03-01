package clases;

/**
 * Representa los diferentes estados por los que puede pasar un Proceso
 * dentro de la máquina de estados del simulador.
 * <p>
 * Sirve para controlar el flujo de ejecución: los procesos en LISTO compiten
 * por el CPU, los BLOQUEADO esperan un evento/interrupción para volver a LISTO,
 * los EN_EJECUCION están ocupando actualmente el CPU y los TERMINADO ya
 * acabaron.
 * </p>
 */
public enum EstadoProceso {

    /**
     * El proceso está cargado en memoria, esperando su turno en la CPU.
     * Compite activamente con otros procesos para ser planificado.
     */
    LISTO,

    /**
     * El proceso se detuvo temporalmente (ej. esperando una I/O o recurso).
     * No puede ser elegido por el planificador hasta que pase otra vez a LISTO.
     */
    BLOQUEADO,

    /**
     * El proceso es el único que está ocupando actualmente la CPU y ejecutando sus
     * ráfagas de código. Retorna a LISTO o BLOQUEADO si hay interrupciones o
     * quantum,
     * o bien pasa a TERMINADO si acaba.
     */
    EN_EJECUCION,

    /**
     * /**
     * El proceso finalizó completamente todas sus ráfagas de tiempoRestante.
     * Ya no compite por recursos.
     */
    TERMINADO,

    /**
     * El proceso murió por inanición (no se pudo desbloquear).
     * Tampoco compite por recursos, pero se diferencia de haber terminado con
     * éxito.
     */
    MUERTO
}
