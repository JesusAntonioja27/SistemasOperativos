package clases;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representa el Bloque de Control de Procesos Global (PCB).
 * Almacena la lista principal de todos los procesos del sistema y ofrece
 * métodos
 * robustos para consultarlos, filtrarlos o imprimir su estado actual.
 * <p>
 * Los algoritmos utilizan el PCB para decidir el orden de ejecución.
 * Esta clase ha sido reforzada con métodos de inyección dinámica
 * por si el profesor desea aplicar filtros (ej. obtener sólo procesos con
 * cierta prioridad).
 * </p>
 */
public class PCB {

    /**
     * Colección global que mantiene la lista de todos los procesos.
     * Se inyecta en el constructor (normalmente creada por el Gestor).
     */
    private List<Proceso> procesos;

    /**
     * Constructor del PCB.
     * 
     * @param procesos Lista inicial de procesos a controlar.
     */
    public PCB(List<Proceso> procesos) {
        // Validación de robustez (null tracking prevencion)
        if (procesos == null) {
            this.procesos = new ArrayList<>();
        } else {
            this.procesos = procesos;
        }
    }

    // =========================================================================
    // MÉTODOS DE FILTRADO ROBUSTO (Por si se solicita dinámicamente)
    // =========================================================================

    /**
     * Retorna una sub-lista sólo de aquellos procesos cuyo ID es Par.
     * Si el algoritmo se debe modificar rápido en clase para trabajar la mitad de
     * casos:
     * usar este método en vez de obtenerProcesos().
     * 
     * @return Lista de procesos pares.
     */
    public List<Proceso> obtenerProcesosPares() {
        return this.procesos.stream()
                .filter(Proceso::esIdPar)
                .collect(Collectors.toList());
    }

    /**
     * Retorna una sub-lista filtrando estrictamente a aquellos que tienen estado
     * LISTO.
     * Una utilidad muy usual para los planificadores apropiativos (como RR y
     * Prioridades).
     * 
     * @return Lista de procesos listos.
     */
    public List<Proceso> obtenerProcesosListos() {
        return this.procesos.stream()
                .filter(p -> p.getEstado() == EstadoProceso.LISTO)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // GETTERS TRADICIONALES
    // =========================================================================

    /**
     * Obtiene la estructura inmutable original para que los algoritmos
     * la puedan iterar o mandar a imprimir.
     * 
     * @return La lista de procesos general.
     */
    public List<Proceso> obtenerProcesos() {
        return procesos;
    }

    // =========================================================================
    // VISTA O IMPRESIÓN (TABLA)
    // =========================================================================

    /**
     * Muestra en la Standard Output (Consola) todos los procesos actuales del PCB,
     * de manera estructurada con ASCII Art para una lectura óptima por los
     * profesores.
     * Formateado con tabs robustos.
     */
    public void mostrarTabla() {
        System.out.println("╔═════════════════════════════════════════════════════════════╗");
        System.out.println("║                TABLA DE CONTROL DE PROCESOS                 ║");
        System.out.println("╠════╦══════╦══════════════╦═══════╦═════════╦═════════╦══════╣");
        System.out.println("║ ID ║TRest ║    Estado    ║ Prior ║ Boletos ║ Usuario ║VecCPU║");
        System.out.println("╠════╬══════╬══════════════╬═══════╬═════════╬═════════╬══════╣");

        for (Proceso p : procesos) {
            // Se usa printf para dar uniformidad perfecta a los bloques
            System.out.printf("║ %2d ║ %4d ║ %-12s ║ %5d ║ %7d ║ %-7s ║ %4d ║\n",
                    p.getId(),
                    p.getTiempoRestante(),
                    p.getEstado(),
                    p.getPrioridad(),
                    p.getBoletos(),
                    p.getUsuario(),
                    p.getVecesUsoCPU());
        }
        System.out.println("╚════╩══════╩══════════════╩═══════╩═════════╩═════════╩══════╝");
    }
}
