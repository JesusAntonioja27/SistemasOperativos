package algoritmos;

import clases.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Optional;

/**
 * Al carecer de expropiación por límite de Quantum, esta variante funciona
 * puramente como un First Come, First Served (FCFS).
 * El primer proceso en llegar a la cola de Listos, toma la CPU y la acapara
 * hasta terminar toda su ráfaga o bloquearse por I/O.
 */
public class RoundRobinNoAprop implements AlgoritmosPlanificacion {

    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        System.out.println("\n--- [INICIO] Algoritmo: Round Robin (No Apropiativo / FCFS) ---");

        Queue<Proceso> colaListos = new LinkedList<>();

        while (sim.iteracionValida()) {
            System.out.println("\n[Tick " + sim.tiempoActual + "]");

            List<Proceso> todosLosProcesos = gp.getPcb().obtenerProcesos();

            // 1. Tratar de despertar a los bloqueados I/O
            for (Proceso p : todosLosProcesos) {
                if (p.getEstado() == EstadoProceso.BLOQUEADO) {
                    gi.intentarDesbloquear(p);
                }
            }

            // 2. Encolar los procesos LISTOS que aún no estén en la Cola (Orden de llegada)
            for (Proceso p : todosLosProcesos) {
                if (p.getEstado() == EstadoProceso.LISTO && !colaListos.contains(p)) {
                    colaListos.offer(p);
                    System.out.println("  -> [ENCOLADO] P" + p.getId() + " entró a la cola de espera FIFO.");
                }
            }

            // 3. Revisar CPU
            Optional<Proceso> procesoEnCPU = todosLosProcesos.stream()
                    .filter(p -> p.getEstado() == EstadoProceso.EN_EJECUCION)
                    .findFirst();

            Proceso pActivo = null;

            if (procesoEnCPU.isPresent()) {
                pActivo = procesoEnCPU.get();

                // Disminuir solo recursos de tiempo, el quantum no expulsa en esta variante.
                pActivo.setTiempoRestante(pActivo.getTiempoRestante() - 1);
                pActivo.setTiempoUsoCPU(pActivo.getTiempoUsoCPU() + 1);

                System.out.println("  -> [EJECUTANDO] P" + pActivo.getId()
                        + " | Le faltan: " + pActivo.getTiempoRestante()
                        + " (Acaparando - No Apropiativo)");

                // Condición de Fin
                if (pActivo.getTiempoRestante() <= 0) {
                    pActivo.setEstado(EstadoProceso.TERMINADO);
                    System.out.println("  -> [FIN] P" + pActivo.getId() + " ha terminado su ejecución total.");
                    pActivo = null; // El próximo tick permitirá a otro entrar
                }
            }

            // 4. Si la CPU está libre (porque no había nadie o el proceso terminó en el if
            // de arriba)
            if (pActivo == null) {
                if (!colaListos.isEmpty()) {
                    // Tomamos estrictamente el primero en la fila
                    Proceso nuevoP = colaListos.poll();

                    // Doble validación en caso de que su estado haya mutado a otra cosa
                    if (nuevoP.getEstado() == EstadoProceso.LISTO && nuevoP.getTiempoRestante() > 0) {
                        nuevoP.setEstado(EstadoProceso.EN_EJECUCION);
                        nuevoP.setVecesUsoCPU(nuevoP.getVecesUsoCPU() + 1); // Registrar despacho para el reporte
                        sim.registrarCambioContexto();

                        System.out.println("  -> [DESPACHO FIFO] Entra a CPU: P" + nuevoP.getId()
                                + " (Ráfaga restante: " + nuevoP.getTiempoRestante() + ")");
                    }
                } else {
                    // CPU Ociosa
                    System.out.println("  -> [IDLE] La CPU está inactiva en este instante.");
                }
            }

            // 5. Avanzar reloj
            sim.incrementarTiempo();
        }

        System.out.println("\n--- [FIN] Simulación Finalizada ---");
    }
}
