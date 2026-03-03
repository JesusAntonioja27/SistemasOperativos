package algoritmos;

import clases.*;
import java.util.List;

public class PlanificacionGarantizada implements AlgoritmosPlanificacion {

    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        System.out.println("\n--- [INICIO] Planificación Garantizada ---");
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

            // 2. Obtener listos con obtenerProcesosListos() del PCB
            List<Proceso> listos = gp.getPcb().obtenerProcesosListos();

            if (listos.isEmpty()) {
                System.out.println("  -> [IDLE] No hay procesos listos.");
                sim.incrementarTiempo();
                continue;
            }

            // 3. Calcular derecho (1/n) y proporcion de cada proceso
            int n = listos.size();
            double derecho = 1.0 / n;

            for (Proceso p : listos) {
                p.setCpuDerecho(derecho);
                p.setProporcion(p.getTiempoUsoCPU() / derecho);
            }

            // 4. Elegir el de MENOR proporcion (el más desfavorecido)
            Proceso elegido = listos.stream()
                    .min((a, b) -> Double.compare(a.getProporcion(), b.getProporcion()))
                    .orElse(null);

            System.out.println("  -> [DESPACHO] P" + elegido.getId()
                    + " | Proporción=" + String.format("%.2f", elegido.getProporcion())
                    + " | Derecho=1/" + n);

            // 5. Entra a CPU
            elegido.setEstado(EstadoProceso.EN_EJECUCION);
            elegido.setVecesUsoCPU(elegido.getVecesUsoCPU() + 1);
            sim.registrarCambioContexto();

            // 6. Validar si se bloquea (aviso profe: entra -> valida -> ejecuta)
            if (elegido.getTiempoRestante() > 1 && Math.random() < 0.3) {
                elegido.setEstado(EstadoProceso.BLOQUEADO);
                System.out.println("  -> [I/O] P" + elegido.getId() + " se bloqueó.");
                sim.incrementarTiempo();
                continue;
            }

            // 7. Ejecutar sim.quantum ticks
            for (int q = 0; q < sim.quantum && sim.iteracionValida()
                    && elegido.getTiempoRestante() > 0; q++) {

                elegido.setTiempoRestante(elegido.getTiempoRestante() - 1);
                elegido.setTiempoUsoCPU(elegido.getTiempoUsoCPU() + 1);
                System.out.println("  -> [EJECUTANDO] P" + elegido.getId()
                        + " | Restante=" + elegido.getTiempoRestante()
                        + " | TiempoUsoCPU=" + elegido.getTiempoUsoCPU());
                sim.incrementarTiempo();
            }

            // 8. ¿Terminó?
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