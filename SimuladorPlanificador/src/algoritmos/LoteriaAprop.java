package algoritmos;

import clases.*;
import java.util.List;
import java.util.Random;
import java.util.Optional;

/**
 * Algoritmo de Lotería Apropiativa (Lottery Scheduling).
 * 
 * Cada proceso posee un número de "boletos" (tickets).
 * En cada iteración:
 * 1. Se calcula el total de boletos disponibles entre procesos LISTOS/EN_EJECUCION.
 * 2. Se genera un número aleatorio y se elige el "ganador" de la lotería.
 * 3. El ganador es despachado/continúa en la CPU.
 * 4. Es apropiativo: si otro proceso LISTO gana, el actual es expropiado.
 * 
 * Ventajas: Justo probabilísticamente, evita inanición.
 */
 public class LoteriaAprop implements AlgoritmosPlanificacion {

    private Random random = new Random();

    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        System.out.println("\n--- [INICIO] Lotería Apropiativa ---");
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

            // 2. Candidatos con obtenerProcesosListos() del PCB
            List<Proceso> candidatos = gp.getPcb().obtenerProcesosListos();

            if (candidatos.isEmpty()) {
                System.out.println("  -> [IDLE] No hay procesos listos.");
                sim.incrementarTiempo();
                continue;
            }

            // 3. Sortear ganador
            Proceso ganador = sortear(candidatos);
            System.out.println("  -> [LOTERÍA] Ganó P" + ganador.getId()
                    + " (boletos: " + ganador.getBoletos() + ")");

            // 4. Entra a CPU
            ganador.setEstado(EstadoProceso.EN_EJECUCION);
            ganador.setVecesUsoCPU(ganador.getVecesUsoCPU() + 1);
            sim.registrarCambioContexto();

            // 5. Validar si se bloquea (aviso profe: entra -> valida -> ejecuta)
            if (ganador.getTiempoRestante() > 1 && Math.random() < 0.3) {
                ganador.setEstado(EstadoProceso.BLOQUEADO);
                System.out.println("  -> [I/O] P" + ganador.getId() + " se bloqueó.");
                sim.incrementarTiempo();
                continue;
            }

            // 6. Ejecutar sim.quantum ticks
            for (int q = 0; q < sim.quantum && sim.iteracionValida()
                    && ganador.getTiempoRestante() > 0; q++) {

                ganador.setTiempoRestante(ganador.getTiempoRestante() - 1);
                ganador.setTiempoUsoCPU(ganador.getTiempoUsoCPU() + 1);
                System.out.println("  -> [EJECUTANDO] P" + ganador.getId()
                        + " | Restante: " + ganador.getTiempoRestante());
                sim.incrementarTiempo();
            }

            // 7. ¿Terminó?
            if (ganador.getTiempoRestante() <= 0) {
                ganador.forzarTerminacion();
                System.out.println("  -> [FIN] P" + ganador.getId() + " terminó.");
            } else {
                ganador.setEstado(EstadoProceso.LISTO);
                System.out.println("  -> [QUANTUM] P" + ganador.getId() + " regresa a cola.");
            }

            gp.getPcb().mostrarTabla();
        }

        System.out.println("\n--- [FIN] Simulación Finalizada ---");
    }

    private Proceso sortear(List<Proceso> candidatos) {
        int total = candidatos.stream().mapToInt(Proceso::getBoletos).sum();
        int numero = random.nextInt(total) + 1;
        int acumulado = 0;
        for (Proceso p : candidatos) {
            acumulado += p.getBoletos();
            if (numero <= acumulado) return p;
        }
        return candidatos.get(candidatos.size() - 1);
    }
}