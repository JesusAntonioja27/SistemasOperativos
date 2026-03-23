package ses.algoritmos;

import ses.AlgoritmosDisco;
import ses.utils.PeticionDisco;
import ses.utils.UtilsDisco;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Implementación del algoritmo SCAN (Elevator).
 * La cabeza se mueve en una dirección atendiendo peticiones; al agotar
 * las peticiones en esa dirección, invierte el sentido de movimiento.
 */
public class SCAN implements AlgoritmosDisco {

    /** ID del proceso dueño de las peticiones. */
    private int idProceso;

    /** Generador de números aleatorios para nuevas peticiones. */
    private Random random;

    /**
     * Constructor de SCAN.
     *
     * @param idProceso ID del proceso dueño.
     * @param random    Generador de aleatorios.
     */
    public SCAN(int idProceso, Random random) {
        this.idProceso = idProceso;
        this.random = random;
    }

    /**
     * Ejecuta el algoritmo SCAN sobre la lista de peticiones.
     *
     * @param peticiones Lista viva de peticiones pendientes (se modifica in-place).
     * @param posInicial Posición actual de la cabeza.
     * @return Posición final de la cabeza.
     */
    @Override
    public int ejecutar(List<PeticionDisco> peticiones, int posInicial) {
        int posActual = posInicial;
        int direccion = 1; // 1 = hacia sectores mayores, -1 = hacia menores
        int totalMovimiento = 0;
        int totalRotacional = 0;
        int totalLecturas = 0;
        int totalEscrituras = 0;
        int peticionesAtendidas = 0;
        int tick = 1;

        System.out.println("  [SES] Algoritmo: SCAN (Elevator)");
        UtilsDisco.imprimirListaPendientes(peticiones);

        while (!peticiones.isEmpty()) {
            // Filtrar peticiones en la dirección actual
            List<PeticionDisco> enDireccion = new ArrayList<>();
            for (PeticionDisco p : peticiones) {
                if (direccion == 1 && p.getSector() >= posActual) {
                    enDireccion.add(p);
                } else if (direccion == -1 && p.getSector() <= posActual) {
                    enDireccion.add(p);
                }
            }

            if (enDireccion.isEmpty()) {
                // Invertir dirección y continuar sin avanzar al extremo
                direccion *= -1;
                System.out.println("  [SES] No hay peticiones en la direccion actual. Invirtiendo direccion.");
                continue;
            }

            // Elegir la más cercana en la dirección actual
            PeticionDisco elegida = null;
            int menorDistancia = Integer.MAX_VALUE;
            for (PeticionDisco p : enDireccion) {
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
        UtilsDisco.imprimirMetricasFinales("SCAN", totalMovimiento, totalRotacional,
                totalLecturas, totalEscrituras, peticionesAtendidas);

        return posActual;
    }
}
