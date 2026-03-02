package algoritmos;

import clases.*;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;

/**
 * Versión No Apropiativa (Non-Preemptive): Una vez que un proceso toma el CPU,
 * no lo suelta hasta terminar su ráfaga completa (tiempoRestante = 0), a menos
 * que se bloquee, pero en este simulador base no hay interrupciones por I/O
 * que saquen voluntariamente a un proceso en ejecución.
 */
public class ProcesoMasCorto implements AlgoritmosPlanificacion {

    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        System.out.println("\n--- [INICIO] Algoritmo: Proceso Más Corto (SJF) No Apropiativo ---");

        // El reloj de simulación dicta cuántos ciclos vivirá el programa
        while (sim.iteracionValida()) {
            System.out.println("\n[Tick " + sim.tiempoActual + "]");

            List<Proceso> todosLosProcesos = gp.getPcb().obtenerProcesos();

            // 1. Tratar de despertar a los bloqueados
            for (Proceso p : todosLosProcesos) {
                if (p.getEstado() == EstadoProceso.BLOQUEADO) {
                    gi.intentarDesbloquear(p);
                }
            }

            // 2. Verificar si hay alguien ocupando actualmente la CPU
            Optional<Proceso> procesoEnCPU = todosLosProcesos.stream()
                    .filter(p -> p.getEstado() == EstadoProceso.EN_EJECUCION)
                    .findFirst();

            Proceso pActivo = null;

            if (procesoEnCPU.isPresent()) {
                pActivo = procesoEnCPU.get();
                // Como es No Apropiativo, si ya está en CPU, se queda ahí hasta terminar.
            } else {
                // 3. Si la CPU está libre, elegimos al proceso LISTO con MENOR tiempo restante.
                Optional<Proceso> elMasCorto = todosLosProcesos.stream()
                        .filter(p -> p.getEstado() == EstadoProceso.LISTO && p.getTiempoRestante() > 0)
                        .min(Comparator.comparingInt(Proceso::getTiempoRestante));

                if (elMasCorto.isPresent()) {
                    pActivo = elMasCorto.get();
                    pActivo.setEstado(EstadoProceso.EN_EJECUCION);
                    sim.registrarCambioContexto();
                    System.out.println("  -> [DESPACHO] Entra a CPU: P" + pActivo.getId()
                            + " (Ráfaga restante: " + pActivo.getTiempoRestante() + ")");
                }
            }

            // 4. Ejecutar 1 unidad de tiempo sobre el proceso en CPU
            if (pActivo != null) {
                pActivo.setTiempoRestante(pActivo.getTiempoRestante() - 1);
                pActivo.setTiempoUsoCPU(pActivo.getTiempoUsoCPU() + 1);

                System.out.println("  -> [EJECUTANDO] P" + pActivo.getId()
                        + " | Le faltan: " + pActivo.getTiempoRestante());

                // 5. Si su ráfaga llegó a cero, lo marcamos como TERMINADO para que lo suelte
                // el sig. tick
                if (pActivo.getTiempoRestante() <= 0) {
                    pActivo.setEstado(EstadoProceso.TERMINADO);
                    System.out.println("  -> [FIN] P" + pActivo.getId() + " ha terminado su ejecución total.");
                }
            } else {
                // CPU Ociosa (todos bloqueados o terminados)
                System.out.println("  -> [IDLE] La CPU está inactiva en este instante.");
            }

            // 6. Avanzar el reloj
            sim.incrementarTiempo();

            // Pausa sutil si se quisiera observar lento, pero para simulación base no es
            // necesario:
            /*
             * try {
             * Thread.sleep(500);
             * } catch (InterruptedException e) {}
             */
        }

        System.out.println("\n--- [FIN] Simulación Finalizada ---");
    }
}
