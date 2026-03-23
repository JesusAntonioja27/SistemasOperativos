package ses.utils;

import java.util.List;
import java.util.Random;

/**
 * Clase de utilidades estáticas reutilizadas por los 4 algoritmos de disco.
 * Contiene cálculos de métricas, generación dinámica de peticiones y
 * funciones de impresión con formato para el subsistema SES-HHDD.
 */
public class UtilsDisco {

    /**
     * Calcula la distancia absoluta (tiempo de búsqueda) entre la posición
     * actual de la cabeza y el sector destino.
     *
     * @param posActual      Posición actual de la cabeza.
     * @param sectorDestino  Sector al que se desea mover.
     * @return Distancia absoluta en sectores.
     */
    public static int calcularDistancia(int posActual, int sectorDestino) {
        return Math.abs(posActual - sectorDestino);
    }

    /**
     * Calcula el retardo rotacional para un sector dado.
     * Se asume 1 unidad de tiempo por cada sector (cabeza inicia en sector 0).
     *
     * @param sector Sector destino.
     * @return Retardo rotacional en unidades de tiempo.
     */
    public static int calcularRetardoRotacional(int sector) {
        return sector * 1;
    }

    /**
     * Calcula el tiempo de transferencia según el tipo de operación.
     *
     * @param tipo 'L' para lectura (1 unidad), 'E' para escritura (2 unidades).
     * @return Tiempo de transferencia en unidades.
     */
    public static int calcularTiempoTransferencia(char tipo) {
        return (tipo == 'L') ? 1 : 2;
    }

    /**
     * Calcula el tiempo total de atender una petición desde la posición actual.
     * Suma: distancia + retardo rotacional + tiempo de transferencia.
     *
     * @param posActual Posición actual de la cabeza.
     * @param peticion  La petición a atender.
     * @return Tiempo total en unidades.
     */
    public static int calcularTiempoTotal(int posActual, PeticionDisco peticion) {
        return calcularDistancia(posActual, peticion.getSector())
             + calcularRetardoRotacional(peticion.getSector())
             + calcularTiempoTransferencia(peticion.getTipo());
    }

    /**
     * Genera aleatoriamente nuevas peticiones de disco después de cada atención.
     * <ul>
     *   <li>Un booleano aleatorio decide si se generan nuevas peticiones.</li>
     *   <li>Si sí: se generan entre 1 y 3 nuevas peticiones.</li>
     *   <li>Sector aleatorio [1–20], tipo aleatorio ('L' o 'E').</li>
     *   <li>No se agrega si el sector ya existe en la lista pendiente.</li>
     *   <li>No se agrega si la lista ya tiene 10 o más peticiones.</li>
     * </ul>
     *
     * @param pendientes Lista viva de peticiones pendientes (se modifica in-place).
     * @param idProceso  ID del proceso dueño.
     * @param random     Generador de números aleatorios.
     * @return Cantidad de nuevas peticiones agregadas.
     */
    public static int generarNuevasPeticiones(List<PeticionDisco> pendientes, int idProceso, Random random) {
        int agregadas = 0;

        // Decidir si se crean nuevas peticiones
        boolean crear = random.nextBoolean();
        if (!crear) {
            return 0;
        }

        // Generar entre 1 y 3 nuevas peticiones
        int cantidad = random.nextInt(3) + 1;

        for (int i = 0; i < cantidad; i++) {
            // No superar el máximo de 10 peticiones por proceso
            if (pendientes.size() >= 10) {
                break;
            }

            int sector = random.nextInt(20) + 1; // [1–20]
            char tipo = random.nextBoolean() ? 'L' : 'E';

            // Verificar que el sector no esté ya en la lista
            boolean duplicado = false;
            for (PeticionDisco p : pendientes) {
                if (p.getSector() == sector) {
                    duplicado = true;
                    break;
                }
            }

            if (!duplicado) {
                PeticionDisco nueva = new PeticionDisco(sector, tipo, idProceso);
                pendientes.add(nueva);
                System.out.println("  [SES] Nueva peticion generada: " + nueva);
                agregadas++;
            }
        }

        return agregadas;
    }

    /**
     * Imprime el movimiento de la cabeza al atender una petición con formato alineado.
     *
     * @param tick          Número de tick de atención.
     * @param posAnterior   Posición de la cabeza antes del movimiento.
     * @param posNueva      Posición de la cabeza después del movimiento.
     * @param peticion      Petición atendida.
     * @param busqueda      Tiempo de búsqueda (distancia).
     * @param rotacional    Retardo rotacional.
     * @param transferencia Tiempo de transferencia.
     * @param total         Tiempo total.
     */
    public static void imprimirMovimiento(int tick, int posAnterior, int posNueva,
            PeticionDisco peticion, int busqueda, int rotacional, int transferencia, int total) {
        System.out.printf("  [SES][Tick %2d] Cabeza: %2d -> %2d | Pet: %-10s | TBusqueda: %2d | TRotacional: %2d | TTransf: %2d | Total: %3d%n",
                tick, posAnterior, posNueva, peticion.toString(), busqueda, rotacional, transferencia, total);
    }

    /**
     * Imprime el resumen de métricas finales del algoritmo de disco con ASCII art.
     *
     * @param nombreAlgoritmo  Nombre del algoritmo ejecutado.
     * @param totalMovimiento  Movimiento total de la cabeza en sectores.
     * @param totalRotacional  Retardo rotacional total acumulado.
     * @param totalLecturas    Cantidad de peticiones de lectura atendidas.
     * @param totalEscrituras  Cantidad de peticiones de escritura atendidas.
     * @param peticionesAtendidas Total de peticiones atendidas.
     */
    public static void imprimirMetricasFinales(String nombreAlgoritmo, int totalMovimiento,
            int totalRotacional, int totalLecturas, int totalEscrituras, int peticionesAtendidas) {

        int tiempoTransfTotal = (totalLecturas * 1) + (totalEscrituras * 2);
        int tiempoTotalDisco = totalMovimiento + totalRotacional + tiempoTransfTotal;

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.printf( "  ║   METRICAS FINALES - %-27s║%n", nombreAlgoritmo);
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.printf( "  ║ Peticiones atendidas       : %3d                ║%n", peticionesAtendidas);
        System.out.printf( "  ║ Movimiento total cabeza    : %3d sectores       ║%n", totalMovimiento);
        System.out.printf( "  ║ Retardo rotacional total   : %3d unidades       ║%n", totalRotacional);
        System.out.printf( "  ║ Tiempo transferencia total : %3d unidades       ║%n", tiempoTransfTotal);
        System.out.printf( "  ║   (Lecturas:  %2d x 1 = %3d)                    ║%n", totalLecturas, totalLecturas * 1);
        System.out.printf( "  ║   (Escrituras: %2d x 2 = %3d)                    ║%n", totalEscrituras, totalEscrituras * 2);
        System.out.printf( "  ║ Tiempo total disco         : %3d unidades       ║%n", tiempoTotalDisco);
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Imprime la lista de peticiones pendientes de forma compacta.
     *
     * @param pendientes Lista de peticiones pendientes.
     */
    public static void imprimirListaPendientes(List<PeticionDisco> pendientes) {
        StringBuilder sb = new StringBuilder("  [SES] Pendientes: [");
        for (int i = 0; i < pendientes.size(); i++) {
            sb.append(pendientes.get(i).toString());
            if (i < pendientes.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        System.out.println(sb.toString());
    }
}
