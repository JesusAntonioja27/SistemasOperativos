package algoritmos;

import clases.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Algoritmo de Múltiples Colas de Prioridad (Multi-level Feedback Queue).
 * Clasifica los procesos en diferentes colas o niveles según su prioridad
 * inicial.
 * El procesador atiende primero a los procesos en la cola de mayor prioridad
 * (Nivel 0).
 * <p>
 * Regla de Diseño (Apegada al proyecto):
 * - El tiempo asignado a un proceso en la CPU se calcula usando la fórmula:
 * prioridad * (vecesUsoCPU + 1).
 * - Cada vez que un proceso usa todo su tiempo asignado y no termina, es
 * penalizado
 * y "baja" al siguiente nivel de cola inferior (ej. Nivel 0 pasa a Nivel 1).
 * - Se incorporaron validaciones robustas para manejar dinámicamente cuántas
 * colas
 * se desean a través de la constante NUM_NIVELES.
 * </p>
 */
public class MultiplesColas implements AlgoritmosPlanificacion {

    /**
     * Cantidad predefinida de colas (niveles).
     * Nivel 0 es la mayor prioridad, Nivel 3 es la menor prioridad.
     */
    private final int NUM_NIVELES = 4;

    /**
     * Ciclo principal de ejecución del algoritmo.
     * 
     * @param sim Objeto global de tiempo y métricas.
     * @param gp  Fábrica central de Procesos.
     * @param gi  Módulo para resolución de operaciones I/O bloqueantes.
     */
    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {

        List<Proceso> procesos = gp.getPcb().obtenerProcesos();

        // 1. Inicializar las 4 colas (una por nivel de prioridad)
        List<List<Proceso>> colas = new ArrayList<>();
        for (int i = 0; i < NUM_NIVELES; i++) {
            colas.add(new ArrayList<>());
        }

        // 2. Meter cada proceso en su cola según prioridad.
        // Se incluyen LISTO y BLOQUEADO para que el GestorInterrupciones
        // pueda intentar desbloquearlos cuando les llegue su turno.
        for (Proceso p : procesos) {
            if (p.getEstado() == EstadoProceso.LISTO || p.getEstado() == EstadoProceso.BLOQUEADO) {
                int nivel = calcularNivelPorPrioridad(p.getPrioridad());
                colas.get(nivel).add(p);
            }
        }

        System.out.println("\n--- Iniciando algoritmo: Multiples Colas de Prioridad ---");
        System.out.println("    4 niveles de cola | Cola 0 = mayor prioridad\n");
        pausa();

        // 3. Ciclo central
        while (sim.iteracionValida()) {

            Proceso candidato = null;
            int nivelCandidato = -1;

            // Buscar de mayor a menor prioridad el primer proceso que pueda correr
            for (int nivel = 0; nivel < NUM_NIVELES; nivel++) {

                List<Proceso> listaNivel = colas.get(nivel);

                for (Proceso p : listaNivel) {
                    // Si está bloqueado, intentamos desbloquearlo ANTES de evaluar si es el
                    // candidato
                    if (p.getEstado() == EstadoProceso.BLOQUEADO) {
                        gi.intentarDesbloquear(p);
                    }

                    // Ahora sí evaluamos: puede ser que ya estuviera LISTO o que se
                    // haya desbloqueado exitosamente justo arriba ^
                    if (p.getEstado() == EstadoProceso.LISTO) {
                        candidato = p;
                        nivelCandidato = nivel;
                        break;
                    }
                }

                if (candidato != null && candidato.getEstado() == EstadoProceso.LISTO) {
                    break;
                }
            }

            // Si no hay ningún proceso listo en ninguna cola: un ciclo de CPU inactiva
            if (candidato == null) {
                System.out.println("[t=" + sim.tiempoActual + "] CPU inactiva - no hay procesos listos.");
                sim.incrementarTiempo();
                pausa();
                continue;
            }

            // 4. Calcular cuanto tiempo le toca segun la formula del proyecto
            // tiempo = prioridad * (vecesUsoCPU + 1)
            int tiempoAsignado = candidato.getPrioridad() * (candidato.getVecesUsoCPU() + 1);
            int tiempoEjecucion = Math.min(tiempoAsignado, candidato.getTiempoRestante());

            candidato.setEstado(EstadoProceso.EN_EJECUCION);

            System.out.println("[t=" + sim.tiempoActual + "] Proceso P" + candidato.getId()
                    + " entra al CPU (cola " + nivelCandidato
                    + ", prioridad " + candidato.getPrioridad()
                    + ") - se le asignan " + tiempoEjecucion + " de " + tiempoAsignado + " ticks posibles");

            // 5. Ejecutar tick a tick
            boolean fueInterrumpido = false;
            int ticksEjecutados = 0;
            for (int e = 0; e < tiempoEjecucion && sim.iteracionValida(); e++) {

                // En cada tick, hay un 30% de probabilidad de que pida I/O y se bloquee.
                // Excepto cuando sólo le queda 1 tick de vida total, dejémoslo terminar.
                if (candidato.getTiempoRestante() > 1 && Math.random() < 0.3) {
                    fueInterrumpido = true;
                    candidato.setEstado(EstadoProceso.BLOQUEADO);
                    System.out.println(
                            "    -> [I/O] P" + candidato.getId() + " pidió I/O y se bloqueó sorpresivamente en el tick "
                                    + (e + 1) + " de su turno asignado");
                    break; // Corta su ejecución y sale de la CPU
                }

                candidato.setTiempoRestante(candidato.getTiempoRestante() - 1);
                candidato.setTiempoUsoCPU(candidato.getTiempoUsoCPU() + 1);
                sim.incrementarTiempo();
                ticksEjecutados++;
            }

            if (ticksEjecutados > 0) {
                candidato.setVecesUsoCPU(candidato.getVecesUsoCPU() + 1);
                sim.registrarCambioContexto();
            }
            colas.get(nivelCandidato).remove(candidato);

            // 6. Ver si terminó o si hay que bajarlo de cola
            if (fueInterrumpido) {
                // El proceso no agotó su tiempo asignado, se detuvo voluntariamente por I/O.
                // Regla clásica de Feedback Queue: No se le penaliza bajándolo de cola,
                // se queda en su mismo nivel, pero en estado BLOQUEADO.
                colas.get(nivelCandidato).add(candidato);
                // Si no ejecutó nada, avanzar el reloj 1 tick para evitar que el
                // while gire indefinidamente sin progreso de tiempo
                if (ticksEjecutados == 0) {
                    sim.incrementarTiempo();
                }
            } else if (candidato.getTiempoRestante() <= 0) {
                candidato.setEstado(EstadoProceso.TERMINADO);
                System.out.println("    -> P" + candidato.getId() + " termino en t=" + sim.tiempoActual);
            } else if (!sim.iteracionValida()) {
                // El tiempo global se agotó durante su ejecución, no es penalización
                candidato.setEstado(EstadoProceso.LISTO);
                colas.get(nivelCandidato).add(candidato);
            } else {
                // Penalización: Agotó TODO su tiempo asignado en la CPU sin bloquearse y no
                // terminó. Baja de cola.
                candidato.setEstado(EstadoProceso.LISTO);
                int nuevoNivel = Math.min(nivelCandidato + 1, NUM_NIVELES - 1);
                colas.get(nuevoNivel).add(candidato);
                System.out.println("    -> P" + candidato.getId()
                        + " usó TODO su tiempo asignado, es penalizado y baja a cola " + nuevoNivel
                        + " (le quedan " + candidato.getTiempoRestante() + " ticks)");
            }

            System.out.println();
            gp.getPcb().mostrarTabla();
            pausa();
        }
    }

    /**
     * Clasifica la prioridad (1-10) en uno de los 4 niveles de cola.
     * 
     * @param prioridad Valor del proceso (1 al 10).
     * @return Nivel de cola (0 al 3). 0 es la cola más alta.
     */
    private int calcularNivelPorPrioridad(int prioridad) {
        if (prioridad >= 9)
            return 0; // prioridad muy alta
        if (prioridad >= 7)
            return 1; // prioridad alta
        if (prioridad >= 4)
            return 2; // prioridad media
        return 3; // prioridad baja
    }

    /**
     * Pausa de 1 segundo entre pasos para que la salida se pueda leer a tiempo.
     * El try-catch es necesario porque Thread.sleep puede lanzar
     * InterruptedException.
     */
    private void pausa() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
