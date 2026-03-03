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
        GestorInterrupciones gestorI = new GestorInterrupciones();
        Planificador planif = new Planificador();
        ReporteFinal reporte = new ReporteFinal();

        // 2. Poblamos la lista de procesos inicial
        gestorP.inicializarProcesos(sim.tiempoActual);

        // 3. Menú
        int opcion = -1;
        while (opcion < 1 || opcion > 10) {
            System.out.println("\nSelecciona el algoritmo de planificación a simular:");
            System.out.println(" 5. Múltiples Colas de Prioridad (Multi-level Feedback Queue)");
            System.out.println(" 7. Planificación Garantizada");
            System.out.println(" 8. Lotería Apropiativo");
            System.out.println(" 3. Prioridades Apropiativo");
            System.out.print("\nTu elección -> ");

            try {
                String inputStr = scanner.nextLine();
                opcion = Integer.parseInt(inputStr);

                if (opcion != 5 && opcion != 7 && opcion != 8 && opcion != 3) {
                    System.out.println("\n>>> [WARN] Elige una opción válida (3, 5, 7 u 8).\n");
                    opcion = -1;
                }
            } catch (NumberFormatException e) {
                System.out.println("\n>>> [ERROR] Por favor, ingresa un número válido.\n");
            }
        }

        // 4. Asignamos algoritmo según opción
        switch (opcion) {
            case 3:
                planif.setAlgoritmo(new PrioridadesAprop());
                break;
            case 5:
                planif.setAlgoritmo(new MultiplesColas());
                break;
            case 7:
                planif.setAlgoritmo(new PlanificacionGarantizada());
                break;
            case 8:
                planif.setAlgoritmo(new LoteriaAprop());
                break;
            default:
                System.out.println("Algoritmo aún no implementado.");
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