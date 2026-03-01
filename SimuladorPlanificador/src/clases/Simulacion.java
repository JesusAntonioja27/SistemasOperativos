package clases;

import java.util.Random;

/**
 * Objeto DTO/Controlador que almacena el contexto temporal global de toda la
 * ejecución.
 * Funciona como el "Reloj Principal" que los Algoritmos usan para calcular sus
 * ticks.
 * <p>
 * Además, contiene configuraciones globales obligatorias generadas
 * aleatoriamente al
 * principio, como el Límite de la Simulación (tiempoMonitoreo) y el Quantum
 * (para RR y Lotería).
 * </p>
 */
public class Simulacion {

    /**
     * Cuánto durará reloj-en-mano toda esta corrida [entre 20 y 35 unidades de
     * tiempo].
     */
    public int tiempoMonitoreo;

    /**
     * Cuántas unidades se asignan de ráfaga ininterrumpida a los algoritmos
     * Apropiativos [2 a 5].
     */
    public int quantum;

    /** El reloj actual (inicia en 0 y va escalando con incrementarTiempo()). */
    public int tiempoActual;

    /**
     * El número de veces que la CPU le fue quitada a un proceso para dársela a
     * otro.
     */
    public int cambiosProceso;

    /**
     * Constructor por defecto.
     * Auto-inicializa las variables pseudo-aleatorias base del proyecto.
     */
    public Simulacion() {
        Random r = new Random();

        // El proyecto exige tiempo entre 20 y 35 para finalizar la simulación por
        // fuerza bruta.
        this.tiempoMonitoreo = r.nextInt(16) + 20; // 0-15 + 20 -> 20-35

        // Quantum entre 2 y 5 inclusive.
        this.quantum = r.nextInt(4) + 2;

        this.tiempoActual = 0;
        this.cambiosProceso = 0;

        System.out.println("\n==================================================");
        System.out.println(" [CONFIG] Tiempo global limite  : " + this.tiempoMonitoreo + " ticks");
        System.out.println(" [CONFIG] Quantum asignado      : " + this.quantum + " ticks");
        System.out.println("==================================================\n");
    }

    // =========================================================================
    // MÉTODOS DE TICK DE RELOJ
    // =========================================================================

    /**
     * Hace avanzar el reloj central del Sistema Operativo por una unidad de tiempo.
     */
    public void incrementarTiempo() {
        this.tiempoActual++;
    }

    /**
     * Suma al contador estadístico de cambios de contexto un conteo extra.
     * Deberá ser llamado por el Planificador cada vez que un proceso abandona o
     * entra al uso del CPU.
     */
    public void registrarCambioContexto() {
        this.cambiosProceso++;
    }

    // =========================================================================
    // MÉTODOS DE COMPROBACIÓN ROBUSTA
    // =========================================================================

    /**
     * Utilidad que valida de forma rápida si la simulación todavía debería
     * continuar.
     * 
     * @return true si el tiempoActual es menor al tiempoMonitoreo absoluto.
     */
    public boolean iteracionValida() {
        return this.tiempoActual < this.tiempoMonitoreo;
    }
}
