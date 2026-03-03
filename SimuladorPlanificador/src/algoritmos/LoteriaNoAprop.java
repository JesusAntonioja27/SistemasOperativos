package algoritmos;

import clases.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class LoteriaNoAprop implements AlgoritmosPlanificacion {
    
    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        
        List<Proceso> procesos = gp.getPcb().obtenerProcesos();
        Random random = new Random();
        
        System.out.println("\n=== Lotería NO APROPIATIVO ===\n");
        pausa();
        
        while (sim.tiempoActual < sim.tiempoMonitoreo) {
            
            // 1. Encontrar procesos LISTOS
            List<Proceso> candidatos = new ArrayList<>();
            for (Proceso p : procesos) {
                if (p.getEstado() == EstadoProceso.LISTO) {
                    candidatos.add(p);
                }
            }
            
            // Si no hay candidatos, salir
            if (candidatos.isEmpty()) {
                System.out.println("[t=" + sim.tiempoActual + "] CPU inactiva - no hay procesos listos.");
                sim.incrementarTiempo();
                pausa();
                if (sim.tiempoActual < sim.tiempoMonitoreo) {
                    continue;
                }
                break;
            }
            
            // 2. Calcular total de boletos
            int totalBoletos = 0;
            for (Proceso p : candidatos) {
                totalBoletos += p.getBoletos();
            }
            
            // 3. Sortear un boleto
            int boletoBuscado = random.nextInt(totalBoletos) + 1;
            
            // 4. Encontrar ganador
            int acumulado = 0;
            Proceso ganador = null;
            for (Proceso p : candidatos) {
                acumulado += p.getBoletos();
                if (boletoBuscado <= acumulado) {
                    ganador = p;
                    break;
                }
            }
            
            // Si ganador está bloqueado, intentar desbloquearlo
            if (ganador.getEstado() == EstadoProceso.BLOQUEADO) {
                gi.intentarDesbloquear(ganador);
                pausa();
                continue;
            }
            
            // Cambiar a EN_EJECUCION
            ganador.setEstado(EstadoProceso.EN_EJECUCION);
            System.out.println("[t=" + sim.tiempoActual + "] Ganador: P" + ganador.getId() 
                    + " (" + ganador.getBoletos() + " boletos) → corre hasta finalizar");
            
            // 5. NO APROPIATIVO: ejecutar COMPLETO sin interrupciones
            while (ganador.getTiempoRestante() > 0 && sim.tiempoActual < sim.tiempoMonitoreo) {
                ganador.setTiempoRestante(ganador.getTiempoRestante() - 1);
                ganador.setTiempoUsoCPU(ganador.getTiempoUsoCPU() + 1);
                sim.incrementarTiempo();
            }
            
            // Registrar cambio de contexto
            ganador.setVecesUsoCPU(ganador.getVecesUsoCPU() + 1);
            sim.registrarCambioContexto();
            
            // 6. Ver si terminó
            if (ganador.getTiempoRestante() == 0) {
                ganador.setEstado(EstadoProceso.TERMINADO);
                System.out.println("  → P" + ganador.getId() + " TERMINADO");
            } else {
                ganador.setEstado(EstadoProceso.LISTO);
            }
            
            gp.getPcb().mostrarTabla();
            pausa();
        }
    }
    
    /**
     * Pausa de 1 segundo entre pasos para que la salida se pueda leer a tiempo.
     */
    private void pausa() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
