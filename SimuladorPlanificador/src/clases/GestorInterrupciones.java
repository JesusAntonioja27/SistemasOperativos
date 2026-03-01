package clases;

import java.util.Random;

/**
 * Simula el comportamiento de E/S (Entrada/Salida) en un Sistema Operativo.
 * Trabaja específicamente sobre Procesos que se encuentran en estado BLOQUEADO.
 * <p>
 * Su mecánica principal implica realizar intentos por "despertar" al proceso
 * (probabilidad 50%).
 * Si un proceso falla repetidamente en todos sus intentos máximos (3),
 * este gestor lo decreta con MUERTE POR INANICIÓN (pasa a estado TERMINADO).
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
     * Intenta sacar repetidamente a un proceso concreto de su estado BLOQUEADO.
     * Tiene un límite de hasta 3 intentos por ciclo en el que sea invocado.
     * 
     * @param proceso El proceso cuya reactivación está siendo intentada.
     * @return true si el proceso logró desbloquearse a LISTO; false si no pudo.
     */
    public boolean intentarDesbloquear(Proceso proceso) {

        if (proceso.getEstado() != EstadoProceso.BLOQUEADO) {
            return false;
        }

        int intentos = 0;
        final int MAX_INTENTOS = 3;

        while (intentos < MAX_INTENTOS) {
            System.out.println("  [I/O] Intento " + (intentos + 1) + " de desbloquear P" + proceso.getId());

            int resultado = random.nextInt(2); // 0 o 1, 50% de exito

            if (resultado == 1) {
                proceso.setEstado(EstadoProceso.LISTO);
                System.out.println("  [I/O] P" + proceso.getId() + " fue desbloqueado, vuelve a la cola de listos.");
                return true;
            }

            System.out.println("  [I/O] P" + proceso.getId() + " sigue bloqueado en ese intento.");
            intentos++;
        }

        // 3 intentos fallidos: el proceso muere por inanicion
        System.out.println("  [INANICION] P" + proceso.getId()
                + " no se pudo desbloquear en 3 intentos. El proceso muere.");
        System.out.println();
        proceso.forzarMuerte();

        return false;
    }
}
