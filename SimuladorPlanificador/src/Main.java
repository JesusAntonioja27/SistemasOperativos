import java.util.Scanner;
import algoritmos.*;
import clases.*;

/**
 * Punto de Entrada Principal (Main) de la Simulación.
 * Se encarga de instanciar todas las dependencias globales requeridas (Tiempo,
 * Procesos, Interrupciones)
 * y de brindar el Menú Interactivo de Selección de Algoritmos al usuario.
 */
public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================================");
        System.out.println("            SIMULADOR DE PLANIFICADOR DE PROCESOS (OS)            ");
        System.out.println("==================================================================");

        // 1. Instanciación de variables de entorno globales.
        Simulacion sim = new Simulacion(); // Genera límites de tiempo y quantums
        GestorProcesos gestorP = new GestorProcesos();
        GestorInterrupciones gestorI = new GestorInterrupciones();
        Planificador planif = new Planificador();
        ReporteFinal reporte = new ReporteFinal();

        // 2. Poblamos la lista de procesos inicial y la metemos al PCB global
        gestorP.inicializarProcesos(sim.tiempoActual);

        // 3. Menú Interactivo de Consola
        int opcion = -1;
        while (opcion < 1 || opcion > 10) {
            System.out.println("\nSelecciona el algoritmo de planificación a simular:");
            // System.out.println(" 1. Round Robin Apropiativo");
            // System.out.println(" 2. Round Robin No Apropiativo");
            // System.out.println(" 3. Prioridades Apropiativo");
            // System.out.println(" 4. Prioridades No Apropiativo");
            System.out.println(" 5. Múltiples Colas de Prioridad (Multi-level Feedback Queue)");
            // System.out.println(" 6. Proceso Más Corto Primero (SJF)");
            // System.out.println(" 7. Planificación Garantizada");
            // System.out.println(" 8. Lotería Apropiativo");
            // System.out.println(" 9. Lotería No Apropiativo");
            // System.out.println("10. Participación Equitativa");
            System.out.print("\nTu elección (Sólo el de Colas en fase de prueba) -> ");

            try {
                // Validación para evitar que truene si ingresan letras
                String inputStr = scanner.nextLine();
                opcion = Integer.parseInt(inputStr);

                if (opcion != 5) {
                    System.out.println(
                            "\n>>> [WARN] Por ahora, solo tenemos codificado Múltiples Colas (Opción 5). \n>>> Por favor, elige la 5.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\n>>> [ERROR] Por favor, ingresa un número válido.\n");
            }
        }

        // 4. Asignamos dependencias según Patrón Estrategia
        switch (opcion) {
            case 5:
                planif.setAlgoritmo(new MultiplesColas());
                break;
            default:
                System.out.println("Algoritmo aún no implementado.");
                return;
        }

        // 5. Arranque de la Simulación en base a la abstracción
        System.out.println("\nPresiona [ENTER] para comenzar la ejecución del Algoritmo elegido...");
        scanner.nextLine();

        // El hilo de ejecución se cede enteramente al Gestor seleccionado
        planif.iniciar(sim, gestorP, gestorI);

        // 6. Recogida de Resultados Estadísticos
        reporte.generarReporte(gestorP.getPcb().obtenerProcesos(), sim.cambiosProceso);

        scanner.close();
    }
}
