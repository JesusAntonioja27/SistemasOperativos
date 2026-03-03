import java.util.Scanner;
import algoritmos.*;
import clases.*;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================================");
        System.out.println("            SIMULADOR DE PLANIFICADOR DE PROCESOS (OS)            ");
        System.out.println("==================================================================");

        // 1. Instanciaci�n de variables de entorno globales.
        Simulacion sim = new Simulacion();
        GestorProcesos gestorP = new GestorProcesos();
        GestorInterrupciones gestorI = new GestorInterrupciones();
        Planificador planif = new Planificador();
        ReporteFinal reporte = new ReporteFinal();

        // 2. Poblamos la lista de procesos inicial
        gestorP.inicializarProcesos(sim.tiempoActual);

        // 3. Men�
        int opcion = -1;
        while (opcion < 1 || opcion > 10) {
            System.out.println("\nSelecciona el algoritmo de planificaci�n a simular:");
            System.out.println(" 1. Round Robin Apropiativo");
            System.out.println(" 2. Round Robin No Apropiativo (FCFS)");
            System.out.println(" 3. Prioridades Apropiativo");
            System.out.println(" 4. Prioridades No Apropiativo");
            System.out.println(" 5. M�ltiples Colas de Prioridad (Multi-level Feedback Queue)");
            System.out.println(" 6. Proceso M�s Corto Primero (SJF No Apropiativo)");
            System.out.println(" 7. Planificaci�n Garantizada");
            System.out.println(" 8. Loter�a Apropiativo");
            System.out.println(" 9. Loter�a No Apropiativo");
            System.out.println("10. Participaci�n Equitativa");
            System.out.print("\nTu elecci�n -> ");

            try {
                String inputStr = scanner.nextLine();
                opcion = Integer.parseInt(inputStr);

                if (opcion < 1 || opcion > 10) {
                    System.out.println("\n>>> [WARN] Opci�n no disponible. Elige un n�mero entre 1 y 10.\n");
                    opcion = -1;
                }
            } catch (NumberFormatException e) {
                System.out.println("\n>>> [ERROR] Por favor, ingresa un n�mero v�lido.\n");
            }
        }

        // 4. Asignamos algoritmo seg�n opci�n
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
                System.out.println("Algoritmo a�n no implementado.");
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
