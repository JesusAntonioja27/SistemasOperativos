package algoritmos;

import clases.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PrioridadesNoAprop implements AlgoritmosPlanificacion {
    
    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        
        List<Proceso> procesos = gp.getPcb().obtenerProcesos();
        Map<Integer, Integer> esperaPorID = new HashMap<>();
        
        for (Proceso p : procesos) {
            esperaPorID.put(p.getId(), 0);
        }
        
        System.out.println("\n=== Prioridades NO APROPIATIVO | Aging cada 3 ciclos ===\n");
        pausa();
        
        while (sim.tiempoActual < sim.tiempoMonitoreo) {
            
            // Encontrar procesos LISTOS
            List<Proceso> candidatos = new ArrayList<>();
            for (Proceso p : procesos) {
                if (p.getEstado() == EstadoProceso.LISTO) {
                    candidatos.add(p);
                }
            }
            
            if (candidatos.isEmpty()) {
                System.out.println("[t=" + sim.tiempoActual + "] CPU inactiva.");
                sim.incrementarTiempo();
                pausa();
                continue;
            }
            
            // AGING: si llevan 3 ciclos esperando, aumentar su prioridad
            for (Proceso p : candidatos) {
                int espera = esperaPorID.get(p.getId()) + 1;
                esperaPorID.put(p.getId(), espera);
                
                if (espera % 3 == 0) {
                    p.setPrioridad(p.getPrioridad() + 1);
                    System.out.println("  [Aging] P" + p.getId() + " prioridad=" + p.getPrioridad());
                }
            }
            
            // Seleccionar el de MAYOR prioridad (en empate, menor ID)
            Proceso proceso = null;
            for (Proceso p : candidatos) {
                if (proceso == null || 
                    p.getPrioridad() > proceso.getPrioridad() ||
                    (p.getPrioridad() == proceso.getPrioridad() && p.getId() < proceso.getId())) {
                    proceso = p;
                }
            }
            
            esperaPorID.put(proceso.getId(), 0);
            
            if (proceso.getEstado() == EstadoProceso.BLOQUEADO) {
                gi.intentarDesbloquear(proceso);
                continue;
            }
            
            // Ejecutar sin apropiación (completo)
            proceso.setEstado(EstadoProceso.EN_EJECUCION);
            System.out.println("[t=" + sim.tiempoActual + "] P" + proceso.getId() +
                    " (prioridad=" + proceso.getPrioridad() + ") corre hasta finalizar");
            
            while (proceso.getTiempoRestante() > 0 && sim.tiempoActual < sim.tiempoMonitoreo) {
                proceso.setTiempoRestante(proceso.getTiempoRestante() - 1);
                proceso.setTiempoUsoCPU(proceso.getTiempoUsoCPU() + 1);
                sim.incrementarTiempo();
            }
            
            proceso.setVecesUsoCPU(proceso.getVecesUsoCPU() + 1);
            sim.registrarCambioContexto();
            
            if (proceso.getTiempoRestante() == 0) {
                proceso.setEstado(EstadoProceso.TERMINADO);
                System.out.println("  → P" + proceso.getId() + " TERMINADO");
            } else {
                proceso.setEstado(EstadoProceso.LISTO);
            }
            
            gp.getPcb().mostrarTabla();
            pausa();
        }
    }
    
    private void pausa() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
