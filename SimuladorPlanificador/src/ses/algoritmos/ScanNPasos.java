package ses.algoritmos;

import ses.AlgoritmosDisco;
import ses.utils.PeticionDisco;
import ses.utils.UtilsDisco;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Implementación del algoritmo N-Step SCAN.
 * Divide las peticiones en grupos de tamaño N y atiende cada grupo
 * completo en orden SCAN antes de tomar el siguiente grupo.
 * Las nuevas peticiones generadas durante la atención de un grupo
 * se agregan a la cola de espera general, no al grupo en curso.
 */
public class ScanNPasos implements AlgoritmosDisco {

    /** Tamaño del grupo de peticiones a atender por pase. */
    private int n;

    /** ID del proceso dueño de las peticiones. */
    private int idProceso;

    /** Generador de números aleatorios para nuevas peticiones. */
    private Random random;

    /**
     * Constructor de N-Step SCAN.
     *
     * @param n         Tamaño del grupo por pase (por defecto 4).
     * @param idProceso ID del proceso dueño.
     * @param random    Generador de aleatorios.
     */
    public ScanNPasos(int n, int idProceso, Random random) {
        this.n = n;
        this.idProceso = idProceso;
        this.random = random;
    }

    /**
     * Ejecuta el algoritmo N-Step SCAN sobre la lista de peticiones.
     *
     * @param peticiones Lista viva de peticiones pendientes (se modifica in-place).
     * @param posInicial Posición actual de la cabeza.
     * @return Posición final de la cabeza.
     */
    @Override
    public int ejecutar(List<PeticionDisco> peticiones, int posInicial) {
        int posActual = posInicial;
        int direccion = 1; // 1 = ascendente, -1 = descendente
        int totalMovimiento = 0;
        int totalRotacional = 0;
        int totalLecturas = 0;
        int totalEscrituras = 0;
        int peticionesAtendidas = 0;
        int tick = 1;
        int numeroPase = 1;

        System.out.println("  [SES] Algoritmo: N-Step SCAN (N=" + n + ")");
        UtilsDisco.imprimirListaPendientes(peticiones);

        while (!peticiones.isEmpty()) {
            // Tomar las primeras N peticiones como colaActual (snapshot)
            int tamGrupo = Math.min(n, peticiones.size());
            List<PeticionDisco> colaActual = new ArrayList<>(peticiones.subList(0, tamGrupo));

            // Eliminar esas N peticiones de la cola principal
            for (int i = 0; i < tamGrupo; i++) {
                peticiones.remove(0);
            }

            System.out.println("  [SES] --- Pase N-Step #" + numeroPase + " con " + colaActual.size() + " peticiones ---");

            // Atender colaActual con lógica SCAN
            while (!colaActual.isEmpty()) {
                // Filtrar peticiones en la dirección actual
                List<PeticionDisco> enDireccion = new ArrayList<>();
                for (PeticionDisco p : colaActual) {
                    if (direccion == 1 && p.getSector() >= posActual) {
                        enDireccion.add(p);
                    } else if (direccion == -1 && p.getSector() <= posActual) {
                        enDireccion.add(p);
                    }
                }

                if (enDireccion.isEmpty()) {
                    // Invertir dirección
                    direccion *= -1;
                    System.out.println("  [SES] Invirtiendo direccion en pase N-Step.");
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

                // Eliminar de colaActual
                colaActual.remove(elegida);

                // Generar nuevas peticiones: van a peticiones (cola de espera), NO a colaActual
                int nuevas = UtilsDisco.generarNuevasPeticiones(peticiones, idProceso, random);
                if (nuevas > 0) {
                    System.out.println("  [SES] Peticiones en espera para siguientes pases:");
                    UtilsDisco.imprimirListaPendientes(peticiones);
                }

                tick++;
            }

            System.out.println("  [SES] --- Fin de pase N-Step. Peticiones en espera: " + peticiones.size() + " ---");
            numeroPase++;
        }

        // Imprimir métricas finales
        UtilsDisco.imprimirMetricasFinales("N-Step SCAN (N=" + n + ")", totalMovimiento,
                totalRotacional, totalLecturas, totalEscrituras, peticionesAtendidas);

        return posActual;
    }
}
