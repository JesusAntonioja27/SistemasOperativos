package clases;

import ses.SES_HHDD;

import java.util.Scanner;

/**
 * Simula el comportamiento de E/S (Entrada/Salida) en un Sistema Operativo.
 * Trabaja específicamente sobre Procesos que se encuentran en estado BLOQUEADO.
 * <p>
 * Cuando un proceso bloqueado recibe tiempo de CPU, se activa el Subsistema
 * de Entrada/Salida para Disco Duro (SES-HHDD) que atiende todas las
 * peticiones de disco pendientes del proceso. Una vez atendidas, el proceso
 * regresa a estado LISTO y el planificador continúa su ejecución normal.
 * </p>
 */
public class GestorInterrupciones {

    /** Instancia del subsistema de E/S para disco duro. */
    private SES_HHDD sesHHDD;

    /**
     * Constructor por defecto.
     * Instancia el subsistema SES-HHDD pasándole el scanner principal.
     * 
     * @param scanner Scanner principal para evitar conflictos de lectura.
     */
    public GestorInterrupciones(Scanner scanner) {
        this.sesHHDD = new SES_HHDD(scanner);
    }

    /**
     * Atiende las peticiones de disco de un proceso bloqueado mediante el SES-HHDD.
     * El planificador se detiene, el SES-HHDD ejecuta el algoritmo de disco
     * seleccionado, y al terminar el proceso regresa a estado LISTO.
     *
     * @param proceso El proceso bloqueado con peticiones de disco pendientes.
     * @return true si el proceso fue atendido y desbloqueado correctamente.
     */
    public boolean intentarDesbloquear(Proceso proceso) {

        if (proceso.getEstado() != EstadoProceso.BLOQUEADO) {
            return false;
        }

        // Activar el SES-HHDD para atender las peticiones de disco
        // El SES-HHDD cambia el estado del proceso a LISTO al terminar
        boolean atendido = sesHHDD.atenderProceso(proceso);

        if (atendido) {
            System.out.println("  [I/O] P" + proceso.getId()
                    + " fue atendido por SES-HHDD, regresa a la cola de listos.");
        }

        return atendido;
    }
}
