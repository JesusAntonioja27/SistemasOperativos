package ses.algoritmos;

import ses.AlgoritmosDisco;
import ses.utils.PeticionDisco;
import ses.utils.UtilsDisco;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Implementación del algoritmo C-SCAN (Circular SCAN).
 * La cabeza se mueve siempre en dirección ascendente atendiendo peticiones.
 * Al llegar al último sector con peticiones, salta al sector 0 sin atender
 * peticiones en el regreso (el salto no cuenta en métricas).
 */
public class CScan implements AlgoritmosDisco {

    /** ID del proceso dueño de las peticiones. */
    private int idProceso;

    /** Generador de números aleatorios para nuevas peticiones. */
    private Random random;

    /**
     * Constructor de C-SCAN.
     *
     * @param idProceso ID del proceso dueño.
     * @param random    Generador de aleatorios.
     */
    public CScan(int idProceso, Random random) {
        this.idProceso = idProceso;
        this.random = random;
    }

    /**
     * Ejecuta el algoritmo C-SCAN sobre la lista de peticiones.
     *
     * @param peticiones Lista viva de peticiones pendientes (se modifica in-place).
     * @param posInicial Posición actual de la cabeza.
     * @return Posición final de la cabeza.
     */
    @Override
    public int ejecutar(List<PeticionDisco> peticiones, int posInicial) {
        int posActual = posInicial;
        int totalMovimiento = 0;
        int totalRotacional = 0;
        int totalLecturas = 0;
        int totalEscrituras = 0;
        int peticionesAtendidas = 0;
        int tick = 1;

        System.out.println("  [SES] Algoritmo: C-SCAN (Circular SCAN)");
        UtilsDisco.imprimirListaPendientes(peticiones);

        while (!peticiones.isEmpty()) {
            // Filtrar peticiones con sector > posActual (dirección ascendente)
            List<PeticionDisco> adelante = new ArrayList<>();
            for (PeticionDisco p : peticiones) {
                if (p.getSector() > posActual) {
                    adelante.add(p);
                }
            }

            if (adelante.isEmpty()) {
                // Saltar al sector 0 sin contar en métricas
                System.out.println("  [SES] No hay peticiones adelante. Cabeza salta al sector 0 (sin costo).");
                posActual = 0;
                continue;
            }

            // Elegir la de menor sector mayor a posActual (siguiente en orden ascendente)
            PeticionDisco elegida = null;
            int menorSector = Integer.MAX_VALUE;
            for (PeticionDisco p : adelante) {
                if (p.getSector() < menorSector) {
                    menorSector = p.getSector();
                    elegida = p;
                }
            }

            // Calcular métricas
            int busqueda = UtilsDisco.calcularDistancia(posActual, elegida.getSector());
            int rotacional = UtilsDisco.calcularRetardoRotacional(elegida.getSector());
            int transferencia = UtilsDisco.calcularTiempoTransferencia(elegida.getTipo());
            int total = busqueda + rotacional + transferencia;

            // Acumular métricas
            totalMovimiento += busqueda;
            totalRotacional += rotacional;
            if (elegida.esLectura()) {
                totalLecturas++;
            } else {
                totalEscrituras++;
            }
            peticionesAtendidas++;

            // Imprimir movimiento
            int posAnterior = posActual;
            posActual = elegida.getSector();
            UtilsDisco.imprimirMovimiento(tick, posAnterior, posActual, elegida,
                    busqueda, rotacional, transferencia, total);

            // Eliminar petición atendida
            peticiones.remove(elegida);

            // Generar nuevas peticiones después de cada atención
            int nuevas = UtilsDisco.generarNuevasPeticiones(peticiones, idProceso, random);
            if (nuevas > 0) {
                UtilsDisco.imprimirListaPendientes(peticiones);
            }

            tick++;
        }

        // Imprimir métricas finales
        UtilsDisco.imprimirMetricasFinales("C-SCAN", totalMovimiento, totalRotacional,
                totalLecturas, totalEscrituras, peticionesAtendidas);

        return posActual;
    }
}
