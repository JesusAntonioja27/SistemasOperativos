import algoritmos.AlgoritmosPlanificacion;
import clases.Simulacion;
import clases.GestorProcesos;
import clases.GestorInterrupciones;

/**
 * Clase Contexto dentro del Patrón de Diseño Estrategia.
 * Encapsula la lógica sobre qué algoritmo exacto va a ejecutar la simulación en
 * este ciclo.
 * <p>
 * Desacopla la invocación de `ejecutar()` con la implementación interna del
 * algoritmo,
 * inyectando las dependencias globales (Tiempo, Gestores) y lanzando la rutina
 * general.
 * </p>
 */
public class Planificador {

    /** Almacena la referencia genérica a la interfaz del algoritmo en uso. */
    private AlgoritmosPlanificacion algoritmoActual;

    /**
     * Inyecta / Configura el Algoritmo a usar. (Ej: new RoundRobinAprop()).
     * Al usar una Interfaz en vez del tipo nativo garantizamos el Polimorfismo.
     * 
     * @param algoritmo Una instancia de alguna clase que implemente
     *                  AlgoritmosPlanificacion.
     */
    public void setAlgoritmo(AlgoritmosPlanificacion algoritmo) {
        this.algoritmoActual = algoritmo;
    }

    /**
     * Desencadena el funcionamiento del Planificador que se configuró en
     * 'setAlgoritmo'.
     * Pasa el control y el estado íntegro de la corrida al objeto Algoritmo,
     * cediendo la responsabilidad matemática y condicional.
     * 
     * @param sim La Entidad controladora de la métrica de Tiempo del entorno.
     * @param gp  Repositorio y fábrica del PCB.
     * @param gi  Modulo de sub-rutinas con resoluciones I/O (Desbloqueos).
     * @throws IllegalStateException Si el planificador intenta iniciar sin haber
     *                               seteado el algoritmo.
     */
    public void iniciar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        if (this.algoritmoActual == null) {
            System.err.println("Error [Planificador]: No se puede iniciar, no hay un Algoritmo seleccionado.");
            throw new IllegalStateException("Algoritmo no ha sido asignado.");
        }

        System.out.println("Iniciando despacho desde Planificador. Delegando control a Estrategia...");
        algoritmoActual.ejecutar(sim, gp, gi);
    }
}
