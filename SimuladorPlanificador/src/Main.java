import java.util.Scanner;
import algoritmos.*;
import clases.*;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================================");
        System.out.println("            SIMULADOR DE PLANIFICADOR DE PROCESOS (OS)            ");
        System.out.println("==================================================================");

        // 1. Instanciación de variables de entorno globales.
        Simulacion sim = new Simulacion();
        GestorProcesos gestorP = new GestorProcesos();
        GestorInterrupciones gestorI = new GestorInterrupciones(scanner);
        Planificador planif = new Planificador();
        ReporteFinal reporte = new ReporteFinal();

        // 2. Poblamos la lista de procesos inicial
        gestorP.inicializarProcesos(sim.tiempoActual);

        // 2.5 Preguntar si activar SES-HHDD
        System.out.println("\n¿Activar Subsistema de E/S para procesos bloqueados? [s/n]: ");
        String respSES = scanner.nextLine().trim().toLowerCase();
        if (respSES.equals("n")) {
            // Forzar todos los bloqueados a LISTO antes de empezar
            for (clases.Proceso p : gestorP.getPcb().obtenerProcesos()) {
                if (p.getEstado() == clases.EstadoProceso.BLOQUEADO) {
                    p.setEstado(clases.EstadoProceso.LISTO);
                    p.getPeticionesHHDD().clear();
                }
            }
            System.out.println("[CONFIG] SES-HHDD desactivado. Procesos bloqueados forzados a LISTO.");
        } else {
            System.out.println("[CONFIG] SES-HHDD activado. Se preguntara el algoritmo al primer proceso bloqueado.");
        }

        // 3. Menú
        int opcion = -1;
        while (opcion < 1 || opcion > 10) {
            System.out.println("\nSelecciona el algoritmo de planificacion a simular:");
            System.out.println(" 1. Round Robin Apropiativo");
            System.out.println(" 2. Round Robin No Apropiativo (FCFS)");
            System.out.println(" 3. Prioridades Apropiativo");
            System.out.println(" 4. Prioridades No Apropiativo");
            System.out.println(" 5. Multiples Colas de Prioridad (Multi-level Feedback Queue)");
            System.out.println(" 6. Proceso Mas Corto Primero (SJF No Apropiativo)");
            System.out.println(" 7. Planificacion Garantizada");
            System.out.println(" 8. Loteria Apropiativo");
            System.out.println(" 9. Loteria No Apropiativo");
            System.out.println("10. Participacion Equitativa");
            System.out.print("\nTu eleccion -> ");

            try {
                String inputStr = scanner.nextLine();
                opcion = Integer.parseInt(inputStr);

                if (opcion < 1 || opcion > 10) {
                    System.out.println("\n>>> [WARN] Opcion no disponible. Elige un numero entre 1 y 10.\n");
                    opcion = -1;
                }
            } catch (NumberFormatException e) {
                System.out.println("\n>>> [ERROR] Por favor, ingresa un numero valido.\n");
            }
        }

        // 4. Asignamos algoritmo según opción
        switch (opcion) {
            case 1:
                planif.setAlgoritmo(new RoundRobinAprop());
                break;
            case 2:
                planif.setAlgoritmo(new RoundRobinNoAprop());
                break;
            case 3:
                planif.setAlgoritmo(new PrioridadesAprop());
                break;
            case 4:
                planif.setAlgoritmo(new PrioridadesNoAprop());
                break;
            case 5:
                planif.setAlgoritmo(new MultiplesColas());
                break;
            case 6:
                planif.setAlgoritmo(new ProcesoMasCorto());
                break;
            case 7:
                planif.setAlgoritmo(new PlanificacionGarantizada());
                break;
            case 8:
                planif.setAlgoritmo(new LoteriaAprop());
                break;
            case 9:
                planif.setAlgoritmo(new LoteriaNoAprop());
                break;
            case 10:
                planif.setAlgoritmo(new ParticipacionEquitativa());
                break;
            default:
                System.out.println("Algoritmo aun no implementado.");
                return;
        }

        // 5. Arranque
        System.out.println("\nPresiona [ENTER] para comenzar...");
        scanner.nextLine();

        planif.iniciar(sim, gestorP, gestorI);

        // 6. Reporte final
        reporte.generarReporte(gestorP.getPcb().obtenerProcesos(), sim.cambiosProceso);

        scanner.close();
    }
}
