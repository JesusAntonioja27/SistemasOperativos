package clases;

public class Proceso {
    public int id;
    public int tiempoRestante;
    public int tiempoInicial;
    public EstadoProceso estado;
    public int prioridad;
    public int boletos;
    public String usuario;
    public int vecesUsoCPU;
    public int tiempoUsoCPU;
    public int tiempoCreacion;
    public double cpuDerecho;
    public double proporcion;

    public Proceso(int id, int tiempoRestante, EstadoProceso estado, int prioridad, int boletos, String usuario, int tiempoActual) {
        // TODO: Inicializar
    }
}
