package ses.utils;

/**
 * Modelo de datos que representa una petición individual de disco duro.
 * Cada petición tiene un sector destino, un tipo (Lectura o Escritura)
 * y el ID del proceso que la originó.
 */
public class PeticionDisco {

    /** Sector destino en el disco [1–20]. */
    private int sector;

    /** Tipo de operación: 'L' para lectura, 'E' para escritura. */
    private char tipo;

    /** ID del proceso dueño de esta petición. */
    private int idProceso;

    /**
     * Construye una nueva petición de disco.
     *
     * @param sector    Sector destino [1–20].
     * @param tipo      'L' (lectura) o 'E' (escritura).
     * @param idProceso ID del proceso que genera la petición.
     */
    public PeticionDisco(int sector, char tipo, int idProceso) {
        this.sector = sector;
        this.tipo = tipo;
        this.idProceso = idProceso;
    }

    /**
     * Indica si la petición es de lectura.
     *
     * @return true si tipo == 'L'.
     */
    public boolean esLectura() {
        return this.tipo == 'L';
    }

    /**
     * Indica si la petición es de escritura.
     *
     * @return true si tipo == 'E'.
     */
    public boolean esEscritura() {
        return this.tipo == 'E';
    }

    /** @return Sector destino de la petición. */
    public int getSector() {
        return sector;
    }

    /** @param sector Nuevo sector destino. */
    public void setSector(int sector) {
        this.sector = sector;
    }

    /** @return Tipo de operación ('L' o 'E'). */
    public char getTipo() {
        return tipo;
    }

    /** @param tipo Nuevo tipo de operación. */
    public void setTipo(char tipo) {
        this.tipo = tipo;
    }

    /** @return ID del proceso dueño. */
    public int getIdProceso() {
        return idProceso;
    }

    /** @param idProceso Nuevo ID de proceso. */
    public void setIdProceso(int idProceso) {
        this.idProceso = idProceso;
    }

    /**
     * Representación compacta de la petición.
     *
     * @return Cadena en formato "3L(P2)" o "14E(P5)".
     */
    @Override
    public String toString() {
        return sector + "" + tipo + "(P" + idProceso + ")";
    }
}
