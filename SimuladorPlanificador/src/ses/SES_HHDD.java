package ses;

import clases.EstadoProceso;
import clases.Proceso;
import ses.algoritmos.SSTF;
import ses.algoritmos.SCAN;
import ses.algoritmos.CScan;
import ses.algoritmos.ScanNPasos;

import java.util.Random;
import java.util.Scanner;

/**
 * Controlador principal del Subsistema de Entrada/Salida para Disco Duro (SES-HHDD).
 * Es el único punto de entrada desde {@link clases.GestorInterrupciones}.
 * <p>
 * Gestiona la posición de la cabeza del disco (persistente entre llamadas),
 * selecciona el algoritmo de disco una sola vez y ejecuta la atención
 * de peticiones de forma síncrona.
 * </p>
 */
public class SES_HHDD {

    /** Posición actual de la cabeza del disco, persiste entre llamadas. */
    private static int posActualCabeza = 0;

    /** Scanner compartido estático para no crear múltiples instancias. */
    private static final Scanner scanner = new Scanner(System.in);

    /** Algoritmo seleccionado: -1 = no elegido aún. */
    private static int algoritmoSeleccionado = -1;

    /** Generador de aleatorios para los algoritmos de disco. */
    private Random random;

    /**
     * Constructor del controlador SES-HHDD.
     */
    public SES_HHDD() {
        this.random = new Random();
    }

    /**
     * Atiende todas las peticiones de disco de un proceso bloqueado.
     * Ejecuta el algoritmo de disco de forma síncrona (bloqueante) y retorna.
     * Al terminar, el proceso pasa a estado LISTO.
     *
     * @param proceso El proceso bloqueado con peticiones de disco pendientes.
     * @return true si el proceso fue atendido correctamente.
     */
    public boolean atenderProceso(Proceso proceso) {
        System.out.println();
        System.out.println("  [SES] ════ Subsistema E/S activado para P" + proceso.getId() + " ════");

        // Si no tiene peticiones, simplemente desbloquearlo
        if (proceso.getPeticionesHHDD().isEmpty()) {
            System.out.println("  [SES] P" + proceso.getId() + " no tiene peticiones de disco pendientes.");
            proceso.setEstado(EstadoProceso.LISTO);
            System.out.println("  [SES] P" + proceso.getId() + " regresa a LISTO.");
            return true;
        }

        // Mostrar menú UNA SOLA VEZ en toda la ejecución
        if (algoritmoSeleccionado == -1) {
            System.out.println();
            System.out.println("  [SES] Selecciona el algoritmo de atencion a disco:");
            System.out.println("    1. SSTF (Shortest Service Time First)");
            System.out.println("    2. SCAN");
            System.out.println("    3. C-SCAN");
            System.out.println("    4. N-Step SCAN (N=4)");
            System.out.print("  [SES] Tu eleccion -> ");

            int eleccion = -1;
            while (eleccion < 1 || eleccion > 4) {
                try {
                    String input = scanner.nextLine().trim();
                    eleccion = Integer.parseInt(input);
                    if (eleccion < 1 || eleccion > 4) {
                        System.out.print("  [SES] Opcion invalida. Elige entre 1 y 4 -> ");
                    }
                } catch (NumberFormatException e) {
                    System.out.print("  [SES] Ingresa un numero valido -> ");
                }
            }
            algoritmoSeleccionado = eleccion;
        }

        // Instanciar el algoritmo según la selección
        AlgoritmosDisco algoritmo;
        switch (algoritmoSeleccionado) {
            case 1:
                algoritmo = new SSTF(proceso.getId(), random);
                break;
            case 2:
                algoritmo = new SCAN(proceso.getId(), random);
                break;
            case 3:
                algoritmo = new CScan(proceso.getId(), random);
                break;
            case 4:
                algoritmo = new ScanNPasos(4, proceso.getId(), random);
                break;
            default:
                algoritmo = new SSTF(proceso.getId(), random);
                break;
        }

        // Ejecutar el algoritmo de disco de forma síncrona
        int nuevaPos = algoritmo.ejecutar(proceso.getPeticionesHHDD(), posActualCabeza);

        // Actualizar posición de la cabeza para siguientes llamadas
        posActualCabeza = nuevaPos;

        // Limpiar peticiones y cambiar estado
        proceso.getPeticionesHHDD().clear();
        proceso.setEstado(EstadoProceso.LISTO);

        System.out.println("  [SES] P" + proceso.getId() + " atendido. Cabeza queda en sector "
                + nuevaPos + ". Proceso regresa a LISTO.");
        System.out.println("  [SES] ════ Fin de atencion SES-HHDD para P" + proceso.getId() + " ════");
        System.out.println();

        return true;
    }
}
