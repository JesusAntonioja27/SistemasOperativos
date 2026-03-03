package algoritmos;

import clases.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class ParticipacionEquitativa implements AlgoritmosPlanificacion {
    
    @Override
    public void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi) {
        
        List<Proceso> procesos = gp.getPcb().obtenerProcesos();
        Proceso procesoAnterior = null;
        
        // Agrupar procesos por usuario
        Map<String, List<Proceso>> mapaUsuarios = new HashMap<>();
        for (Proceso p : procesos) {
            mapaUsuarios.putIfAbsent(p.getUsuario(), new ArrayList<>());
            mapaUsuarios.get(p.getUsuario()).add(p);
        }
        
        List<String> usuarios = new ArrayList<>(mapaUsuarios.keySet());
        int indiceUsuario = 0;
        Map<String, Integer> indiceProceso = new HashMap<>();
        for (String u : usuarios) {
            indiceProceso.put(u, 0);
        }
        
        System.out.println("\n=== Participación Equitativa ===");
        System.out.println("Usuarios: " + usuarios + "\n");
        pausa();
        
        while (sim.tiempoActual < sim.tiempoMonitoreo) {
            
            // Buscar siguiente usuario con procesos activos
            int intentos = 0;
            while (intentos < usuarios.size()) {
                String usuario = usuarios.get(indiceUsuario);
                List<Proceso> activos = new ArrayList<>();
                for (Proceso p : mapaUsuarios.get(usuario)) {
                    if (p.getEstado() != EstadoProceso.TERMINADO) {
                        activos.add(p);
                    }
                }
                if (!activos.isEmpty()) break;
                indiceUsuario = (indiceUsuario + 1) % usuarios.size();
                intentos++;
            }
            
            if (intentos == usuarios.size()) break;
            
            // Seleccionar proceso del usuario actual (round-robin)
            String usuario = usuarios.get(indiceUsuario);
            List<Proceso> listos = new ArrayList<>();
            for (Proceso p : mapaUsuarios.get(usuario)) {
                if (p.getEstado() == EstadoProceso.LISTO) {
                    listos.add(p);
                }
            }
            
            if (listos.isEmpty()) {
                indiceUsuario = (indiceUsuario + 1) % usuarios.size();
                continue;
            }
            
            int idx = indiceProceso.get(usuario) % listos.size();
            Proceso proceso = listos.get(idx);
            indiceProceso.put(usuario, idx + 1);
            
            if (proceso.getEstado() == EstadoProceso.BLOQUEADO) {
                gi.intentarDesbloquear(proceso);
                indiceUsuario = (indiceUsuario + 1) % usuarios.size();
                continue;
            }
            
            // Ejecutar 1 unidad (turno equitativo)
            proceso.setEstado(EstadoProceso.EN_EJECUCION);
            System.out.println("[t=" + sim.tiempoActual + "] " + usuario + " → P" + proceso.getId());
            
            proceso.setTiempoRestante(proceso.getTiempoRestante() - 1);
            proceso.setTiempoUsoCPU(proceso.getTiempoUsoCPU() + 1);
            sim.incrementarTiempo();
            
            if (proceso != procesoAnterior) {
                proceso.setVecesUsoCPU(proceso.getVecesUsoCPU() + 1);
                sim.registrarCambioContexto();
            }
            procesoAnterior = proceso;
            
            if (proceso.getTiempoRestante() == 0) {
                proceso.setEstado(EstadoProceso.TERMINADO);
                System.out.println("  → P" + proceso.getId() + " TERMINADO");
            } else {
                proceso.setEstado(EstadoProceso.LISTO);
            }
            
            indiceUsuario = (indiceUsuario + 1) % usuarios.size();
            
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
