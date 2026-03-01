package algoritmos;

import clases.Simulacion;
import clases.GestorProcesos;
import clases.GestorInterrupciones;

/**
 * Contrato base para el Patrón Estrategia (Strategy Pattern).
 * Determina que todos los algoritmos de planificación a crear (SJF, RR,
 * Prioridades, etc.)
 * deben implementar estrictamente el método abstracto 'ejecutar()'.
 * <p>
 * Esto flexibiliza enormemente el sistema, permitiendo intercambiar el
 * algoritmo actual en
 * tiempo de ejecución sin cambiar la infraestructura de las clases maestras.
 * Si el profesor demanda implementar un nuevo algoritmo sorpresa, sólo hay que
 * implementar
 * esta interfaz.
 * </p>
 */
public interface AlgoritmosPlanificacion {

    /**
     * Método core que invoca la ejecución del algoritmo específico.
     * 
     * @param sim Configuración global de entorno (tiempo, reloj, quantum).
     * @param gp  Responsable de proveer el PCB con todos los procesos simulados.
     * @param gi  Gestor capaz de desencadenar intentos de I/O sobre procesos
     *            bloqueados.
     */
    void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi);
}
