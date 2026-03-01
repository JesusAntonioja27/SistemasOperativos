package clases;

/**
 * Representa la unidad fundamental del sistema: El Proceso.
 * Contiene todos los metadatos necesarios (PCB simulado) para que cualquier
 * algoritmo de planificación pueda tomar decisiones (Round Robin, Prioridades,
 * SJF, etc.).
 * <p>
 * Es una clase robusta que no solo guarda datos, sino que proporciona métodos
 * de utilidad
 * para evaluar condiciones (ej. para inyectar reglas dinámicas que el profesor
 * pueda pedir).
 * </p>
 */
public class Proceso {

    // --- Atributos base ---
    /** Identificador único del proceso (1, 2, 3...) */
    private int id;

    /**
     * Unidades de tiempo que le faltan al proceso para terminar (va disminuyendo a
     * 0)
     */
    private int tiempoRestante;

    /** Almacena la ráfaga original del proceso para cálculos estadísticos */
    private int tiempoInicial;

    /** Estado del proceso en la máquina de estados (LISTO, BLOQUEADO, etc.) */
    private EstadoProceso estado;

    /** Prioridad del proceso (mayor número = más importante) */
    private int prioridad;

    /**
     * Número de boletos asignados para los algoritmos basados en Lotería (1 a 5)
     */
    private int boletos;

    /** Identificador del dueño/usuario del proceso (ej. "Usuario1") */
    private String usuario;

    // --- Atributos estadísticos y de seguimiento ---
    /**
     * Cuántas veces este proceso ha sido despachado a la CPU (cambios de contexto
     * hacia él)
     */
    private int vecesUsoCPU;

    /**
     * Tiempo real (histórico acumulado) que ha estado este proceso dentro de la CPU
     * ejecutando
     */
    private int tiempoUsoCPU;

    /** En qué momento exacto del reloj global (t) fue creado este proceso */
    private int tiempoCreacion;

    // --- Atributos específicos (Alg. Garantizada) ---
    /**
     * Cuánto porcentaje o cuota de CPU le toca legítimamente a este proceso (1/n)
     */
    private double cpuDerecho;

    /**
     * Qué tanta atención ha recibido en relación a su derecho (tiempoUsoCPU /
     * cpuDerecho)
     */
    private double proporcion;

    /**
     * Constructor del Proceso.
     * Inicializa los atributos principales y establece métricas en cero.
     *
     * @param id             ID numérico que identifica al proceso unívocamente.
     * @param tiempoRestante Unidades de ejecución requeridas por este proceso.
     * @param estado         EstadoProceso inicial (normalmente LISTO o BLOQUEADO).
     * @param prioridad      Importancia frente a otros (escala de 1 a 10 aprox.).
     * @param boletos        Tickets habilitados para lotería (1 a 5).
     * @param usuario        Nombre del usuario dueño.
     * @param tiempoActual   El tick de reloj de la Simulación en el que nace el
     *                       proceso.
     */
    public Proceso(int id, int tiempoRestante, EstadoProceso estado, int prioridad,
            int boletos, String usuario, int tiempoActual) {
        this.id = id;
        this.tiempoRestante = tiempoRestante;
        this.tiempoInicial = tiempoRestante; // Se guarda la ráfaga original
        this.estado = estado;
        this.prioridad = prioridad;
        this.boletos = boletos;
        this.usuario = usuario;

        // Contadores inician en 0
        this.vecesUsoCPU = 0;
        this.tiempoUsoCPU = 0;
        this.tiempoCreacion = tiempoActual;
        this.cpuDerecho = 0.0;
        this.proporcion = 0.0;
    }

    // =========================================================================
    // MÉTODOS ROBUSTOS PARA REGLAS DINÁMICAS (Para peticiones del profesor)
    // =========================================================================

    /**
     * Valida si el ID de este proceso es Par.
     * Utilidad: Permite que el profesor diga "solo quiero planificar los procesos
     * pares".
     * 
     * @return true si el id es módulo 2 igual a 0, false de lo contrario.
     */
    public boolean esIdPar() {
        return this.id % 2 == 0;
    }

    /**
     * Valida si este proceso está en condiciones de ser despachado a la CPU.
     * Depende netamente de que su estado sea LISTO y tenga tiempo sobrante por
     * correr.
     * 
     * @return true si se puede ejecutar.
     */
    public boolean puedeEjecutar() {
        return this.estado == EstadoProceso.LISTO && this.tiempoRestante > 0;
    }

    /**
     * Función utilitaria para abortar el proceso de inmediato.
     * Coloca el tiempo restante en cero y el estado a TERMINADO.
     */
    public void forzarTerminacion() {
        this.tiempoRestante = 0;
        this.estado = EstadoProceso.TERMINADO;
    }

    /**
     * Función utilitaria para abortar el proceso por inanición.
     * Coloca el tiempo restante en cero y el estado a MUERTO.
     */
    public void forzarMuerte() {
        this.tiempoRestante = 0;
        this.estado = EstadoProceso.MUERTO;
    }

    // =========================================================================
    // GETTERS Y SETTERS TRADICIONALES
    // =========================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }

    public void setTiempoRestante(int tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }

    public int getTiempoInicial() {
        return tiempoInicial;
    }
    // No hay setter para tiempoInicial para proteger el dato original estadístico.

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getBoletos() {
        return boletos;
    }

    public void setBoletos(int boletos) {
        this.boletos = boletos;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getVecesUsoCPU() {
        return vecesUsoCPU;
    }

    public void setVecesUsoCPU(int vecesUsoCPU) {
        this.vecesUsoCPU = vecesUsoCPU;
    }

    public int getTiempoUsoCPU() {
        return tiempoUsoCPU;
    }

    public void setTiempoUsoCPU(int tiempoUsoCPU) {
        this.tiempoUsoCPU = tiempoUsoCPU;
    }

    public int getTiempoCreacion() {
        return tiempoCreacion;
    }

    public void setTiempoCreacion(int tiempoCreacion) {
        this.tiempoCreacion = tiempoCreacion;
    }

    public double getCpuDerecho() {
        return cpuDerecho;
    }

    public void setCpuDerecho(double cpuDerecho) {
        this.cpuDerecho = cpuDerecho;
    }

    public double getProporcion() {
        return proporcion;
    }

    public void setProporcion(double proporcion) {
        this.proporcion = proporcion;
    }

    @Override
    public String toString() {
        return "Proceso[ID=" + id + ", Estado=" + estado + ", TRest=" + tiempoRestante + "]";
    }
}
