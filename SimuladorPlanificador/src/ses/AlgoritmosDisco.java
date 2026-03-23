package ses;

import ses.utils.PeticionDisco;
import java.util.List;

/**
 * Interfaz Strategy para los algoritmos de atención a disco duro.
 * Cada implementación concreta (SSTF, SCAN, C-SCAN, N-Step SCAN) debe
 * proveer su propia lógica de ordenamiento y atención de peticiones.
 */
public interface AlgoritmosDisco {

    /**
     * Ejecuta el algoritmo de atención a peticiones de disco.
     *
     * @param peticiones Lista viva de peticiones pendientes (se modifica in-place).
     * @param posInicial Posición actual de la cabeza del disco al momento de la llamada.
     * @return Posición final de la cabeza al terminar de atender todas las peticiones.
     */
    int ejecutar(List<PeticionDisco> peticiones, int posInicial);
}
