package algoritmos;

import clases.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Optional;

/**
 * El proceso activo en la CPU será expropiado (expulsado temporalmente de su
 * estado de ejecución, devuelto a LISTO y encolado al final de la fila)
 * si su ráfaga actual alcanza el límite del Quantum sin terminar.
 */
public class RoundRobinAprop implements AlgoritmosPlanificacion {

    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        System.out.println("\n--- [INICIO] Algoritmo: Round Robin (Apropiativo) ---");
        System.out.println("  * Quantum asignado por sistema: " + sim.quantum);

        Queue<Proceso> colaListos = new LinkedList<>();
        int limiteQuantum = sim.quantum;
        int quantumGastado = 0;

        while (sim.iteracionValida()) {
            System.out.println("\n[Tick " + sim.tiempoActual + "]");

            List<Proceso> todosLosProcesos = gp.getPcb().obtenerProcesos();

            // 1. Tratar de despertar a los bloqueados I/O
            for (Proceso p : todosLosProcesos) {
                if (p.getEstado() == EstadoProceso.BLOQUEADO) {
                    gi.intentarDesbloquear(p);
                }
            }

            // 2. Encolar los procesos LISTOS que aún no estén en la Cola
            for (Proceso p : todosLosProcesos) {
                if (p.getEstado() == EstadoProceso.LISTO && !colaListos.contains(p)) {
                    colaListos.offer(p);
                    System.out.println("  -> [ENCOLADO] P" + p.getId() + " entró a la cola de espera.");
                }
            }

            // 3. Revisar CPU
            Optional<Proceso> procesoEnCPU = todosLosProcesos.stream()
                    .filter(p -> p.getEstado() == EstadoProceso.EN_EJECUCION)
                    .findFirst();

            Proceso pActivo = null;

            if (procesoEnCPU.isPresent()) {
                pActivo = procesoEnCPU.get();

                // Disminuir recursos consumiendo 1 Tick de tiempo en la CPU
                pActivo.setTiempoRestante(pActivo.getTiempoRestante() - 1);
                pActivo.setTiempoUsoCPU(pActivo.getTiempoUsoCPU() + 1);
                quantumGastado++;

                System.out.println("  -> [EJECUTANDO] P" + pActivo.getId()
                        + " | Le faltan: " + pActivo.getTiempoRestante()
                        + " | Quantum usado: " + quantumGastado + "/" + limiteQuantum);

                // Condición de Fin
                if (pActivo.getTiempoRestante() <= 0) {
                    pActivo.setEstado(EstadoProceso.TERMINADO);
                    System.out.println("  -> [FIN] P" + pActivo.getId() + " ha terminado su ejecución total.");
                    pActivo = null;
                }
                // Condición de Expropiación (Límite de Quantum alcanzado)
                else if (quantumGastado >= limiteQuantum) {
                    pActivo.setEstado(EstadoProceso.LISTO);
                    // Pasa al final de la cola por no terminar a tiempo (Round Robin clasico)
                    colaListos.offer(pActivo);
                    System.out.println("  -> [EXPROPIADO] P" + pActivo.getId()
                            + " sobrepasó el Quantum. Es devuelto a la cola de LISTOS.");
                    sim.registrarCambioContexto();

                    pActivo = null;
                }
            }

            // 4. Si la CPU está libre (porque nadie la usaba o el anterior recién finalizó
            // o fue expropiado)
            if (pActivo == null) {
                if (!colaListos.isEmpty()) {
                    // Tomamos el primero en la cola FIFO
                    Proceso nuevoP = colaListos.poll();

                    // Doble validación por seguridad
                    if (nuevoP.getEstado() == EstadoProceso.LISTO && nuevoP.getTiempoRestante() > 0) {
                        nuevoP.setEstado(EstadoProceso.EN_EJECUCION);
                        sim.registrarCambioContexto();
                        quantumGastado = 0; // Se reinicia el contador al despachar un nuevo proceso

                        System.out.println("  -> [DESPACHO] Entra a CPU: P" + nuevoP.getId()
                                + " (Ráfaga restante: " + nuevoP.getTiempoRestante() + ")");
                    }
                } else {
                    // CPU Ociosa
                    System.out.println("  -> [IDLE] La CPU está inactiva y la cola está vacía en este instante.");
                }
            }

            // 5. Avanzar reloj
            sim.incrementarTiempo();

            // Pausa sutil opcional:
            /*
             * try {
             * Thread.sleep(500);
             * } catch (InterruptedException e) {}
             */
        }

        System.out.println("\n--- [FIN] Simulación Finalizada ---");
    }
}
