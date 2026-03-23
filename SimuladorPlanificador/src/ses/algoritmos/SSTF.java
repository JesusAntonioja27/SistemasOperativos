package ses.algoritmos;

import ses.AlgoritmosDisco;
import ses.utils.PeticionDisco;
import ses.utils.UtilsDisco;

import java.util.List;
import java.util.Random;

/**
 * Implementación del algoritmo SSTF (Shortest Service Time First).
 * Siempre atiende la petición más cercana a la posición actual de la cabeza,
 * minimizando el tiempo de búsqueda en cada paso.
 */
public class SSTF implements AlgoritmosDisco {

    /** ID del proceso dueño de las peticiones. */
    private int idProceso;

    /** Generador de números aleatorios para nuevas peticiones. */
    private Random random;

    /**
     * Constructor de SSTF.
     *
     * @param idProceso ID del proceso dueño.
     * @param random    Generador de aleatorios.
     */
    public SSTF(int idProceso, Random random) {
        this.idProceso = idProceso;
        this.random = random;
    }

    /**
     * Ejecuta el algoritmo SSTF sobre la lista de peticiones.
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

        System.out.println("  [SES] Algoritmo: SSTF (Shortest Service Time First)");
        UtilsDisco.imprimirListaPendientes(peticiones);

        while (!peticiones.isEmpty()) {
            // Buscar la petición más cercana
            PeticionDisco elegida = null;
            int menorDistancia = Integer.MAX_VALUE;

            for (PeticionDisco p : peticiones) {
                int dist = UtilsDisco.calcularDistancia(posActual, p.getSector());
                if (dist < menorDistancia) {
                    menorDistancia = dist;
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
        UtilsDisco.imprimirMetricasFinales("SSTF", totalMovimiento, totalRotacional,
                totalLecturas, totalEscrituras, peticionesAtendidas);

        return posActual;
    }
}
