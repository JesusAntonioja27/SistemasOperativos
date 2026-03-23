package clases;

import java.util.Random;

/**
 * Simula el comportamiento de E/S (Entrada/Salida) en un Sistema Operativo.
 * Trabaja específicamente sobre Procesos que se encuentran en estado BLOQUEADO.
 * <p>
 * Si el proceso tiene peticiones de disco pendientes, delega al subsistema
 * SES-HHDD para atenderlas. Si no tiene peticiones de disco, mantiene
 * la lógica aleatoria original de intentos de desbloqueo.
 * </p>
 */
public class GestorInterrupciones {

    /** Generador de probabilidades para intentar el desbloqueo. */
    private Random random;

    /**
     * Constructor por defecto.
     * Instancia el generador probabilístico.
     */
    public GestorInterrupciones() {
        this.random = new Random();
    }

    /**
     * Intenta desbloquear un proceso.
     * Si el proceso tiene peticiones de disco HHDD pendientes, delega al SES-HHDD.
     * Si no, realiza hasta 3 intentos aleatorios de desbloqueo.
     * 
     * @param proceso El proceso cuya reactivación está siendo intentada.
     * @return true si el proceso logró desbloquearse a LISTO; false si no pudo.
     */
    public boolean intentarDesbloquear(Proceso proceso) {

        if (proceso.getEstado() != EstadoProceso.BLOQUEADO) {
            return false;
        }

        if (proceso.tienePeticionesHHDD()) {
            // El proceso tiene peticiones de disco: delegar al SES-HHDD
            System.out.println("  [I/O] P" + proceso.getId() + " tiene peticiones HHDD pendientes. Activando SES-HHDD...");
            ses.SES_HHDD sesHhdd = new ses.SES_HHDD();
            return sesHhdd.atenderProceso(proceso);
        } else {
            // Sin peticiones de disco: mantener lógica aleatoria original
            int intentos = 0;
            final int MAX_INTENTOS = 3;

            while (intentos < MAX_INTENTOS) {
                System.out.println("  [I/O] Intento " + (intentos + 1) + " de desbloquear P" + proceso.getId());

                if (random.nextInt(2) == 1) {
                    proceso.setEstado(EstadoProceso.LISTO);
                    System.out.println("  [I/O] P" + proceso.getId() + " desbloqueado, vuelve a LISTO.");
                    return true;
                }

                System.out.println("  [I/O] P" + proceso.getId() + " sigue bloqueado.");
                intentos++;
            }

            // 3 intentos fallidos: el proceso muere por inanición
            System.out.println("  [INANICION] P" + proceso.getId() + " muere por inanicion.");
            System.out.println();
            proceso.forzarMuerte();

            return false;
        }
    }
}
