package clases;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidad encargada de recopilar los resultados finales cuando la simulación
 * termina,
 * iterando sobre el PCB actual y mostrando estadísticas generales.
 * <p>
 * Separa a los procesos en tres grupos principales: Los que lograron terminar,
 * los que nunca ejecutaron (inanición o falta de tiempo), y los que quedaron
 * truncados
 * a la mitad de su ejecución por límite de tiempoMonitoreo global.
 * </p>
 */
public class ReporteFinal {

    /**
     * Imprime el informe general resumiendo lo acontecido durante la ejecución del
     * Algoritmo.
     * 
     * @param procesos     La lista de procesos obtenida del PCB una vez acaba la
     *                     simulación.
     * @param totalCambios La cantidad de veces que cambió el proceso en CPU.
     */
    public void generarReporte(List<Proceso> procesos, int totalCambios) {

        List<Proceso> terminados = procesos.stream()
                .filter(p -> p.getEstado() == EstadoProceso.TERMINADO && p.getVecesUsoCPU() > 0)
                .collect(Collectors.toList());

        List<Proceso> muertos = procesos.stream()
                .filter(p -> p.getEstado() == EstadoProceso.MUERTO)
                .collect(Collectors.toList());

        List<Proceso> nuncaEjecutados = procesos.stream()
                .filter(p -> p.getVecesUsoCPU() == 0 && p.getEstado() != EstadoProceso.MUERTO)
                .collect(Collectors.toList());

        List<Proceso> noTerminaron = procesos.stream()
                .filter(p -> p.getEstado() != EstadoProceso.TERMINADO && p.getEstado() != EstadoProceso.MUERTO
                        && p.getVecesUsoCPU() > 0)
                .collect(Collectors.toList());

        System.out.println("\n=======================================================");
        System.out.println("              REPORTE FINAL DE SIMULACION              ");
        System.out.println("=======================================================");

        System.out.println("Procesos exitosos              : " + obtenerCadenaIDs(terminados));
        System.out.println("Procesos muertos (inanicion)   : " + obtenerCadenaIDs(muertos));
        System.out.println("Procesos que nunca ejecutaron  : " + obtenerCadenaIDs(nuncaEjecutados));
        System.out.println("Procesos que no alcanzaron     : " + obtenerCadenaIDs(noTerminaron));

        System.out.println("-------------------------------------------------------");
        System.out.println("Total cambios de contexto      : " + totalCambios);
        System.out.println("=======================================================\n");
    }

    /**
     * Convierte una lista de procesos en una cadena legible de IDs, ej: [P1, P4,
     * P5].
     * 
     * @param lista Lista a procesar.
     * @return String con los IDs formateados.
     */
    private String obtenerCadenaIDs(List<Proceso> lista) {
        if (lista.isEmpty()) {
            return "[ninguno]";
        }
        return "[" + lista.stream()
                .map(p -> "P" + p.getId())
                .collect(Collectors.joining(", ")) + "]";
    }
}
