package algoritmos;

import clases.*;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;

/**
 * Planificación Apropiativa basada en Prioridades.
 * <p>
 * En cada tick se selecciona el proceso READY con la prioridad más alta (mayor
 * número).
 * Si un nuevo proceso con prioridad superior entra a READY, el actual en CPU
 * es expropiado.
 * </p>
 */
public class PrioridadesAprop implements AlgoritmosPlanificacion {

    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        System.out.println("\n--- [INICIO] Prioridades Apropiativo ---");
        System.out.println("    Quantum: " + sim.quantum + " ticks");

        while (sim.iteracionValida()) {
            System.out.println("\n[Tick " + sim.tiempoActual + "]");

            List<Proceso> todos = gp.getPcb().obtenerProcesos();

            // 1. Intentar despertar bloqueados
            for (Proceso p : todos) {
                if (p.getEstado() == EstadoProceso.BLOQUEADO) {
                    gi.intentarDesbloquear(p);
                }
            }

            // 2. Elegir LISTO con mayor prioridad usando obtenerProcesosListos() del PCB
            Proceso elegido = gp.getPcb().obtenerProcesosListos()
                    .stream()
                    .max(Comparator.comparingInt(Proceso::getPrioridad))
                    .orElse(null);

            if (elegido == null) {
                System.out.println("  -> [IDLE] No hay procesos listos.");
                sim.incrementarTiempo();
                continue;
            }

            System.out.println("  -> [DESPACHO] P" + elegido.getId()
                    + " entra a CPU (Prio=" + elegido.getPrioridad() + ")");

            // 3. Entra a CPU
            elegido.setEstado(EstadoProceso.EN_EJECUCION);
            elegido.setVecesUsoCPU(elegido.getVecesUsoCPU() + 1);
            sim.registrarCambioContexto();

            // 4. Validar si se bloquea (aviso profe: entra -> valida -> ejecuta)
            if (elegido.getTiempoRestante() > 1 && Math.random() < 0.3) {
                elegido.setEstado(EstadoProceso.BLOQUEADO);
                System.out.println("  -> [I/O] P" + elegido.getId() + " se bloqueó.");
                sim.incrementarTiempo();
                continue;
            }

            // 5. Ejecutar sim.quantum ticks
            for (int q = 0; q < sim.quantum && sim.iteracionValida()
                    && elegido.getTiempoRestante() > 0; q++) {

                elegido.setTiempoRestante(elegido.getTiempoRestante() - 1);
                elegido.setTiempoUsoCPU(elegido.getTiempoUsoCPU() + 1);
                System.out.println("  -> [EJECUTANDO] P" + elegido.getId()
                        + " | Prio=" + elegido.getPrioridad()
                        + " | Restante=" + elegido.getTiempoRestante());
                sim.incrementarTiempo();
            }

            // 6. ¿Terminó?
            if (elegido.getTiempoRestante() <= 0) {
                elegido.forzarTerminacion();
                System.out.println("  -> [FIN] P" + elegido.getId() + " terminó.");
            } else {
                elegido.setEstado(EstadoProceso.LISTO);
                System.out.println("  -> [QUANTUM] P" + elegido.getId() + " regresa a cola.");
            }

            gp.getPcb().mostrarTabla();
        }

        System.out.println("\n--- [FIN] Simulación Finalizada ---");
    }
}