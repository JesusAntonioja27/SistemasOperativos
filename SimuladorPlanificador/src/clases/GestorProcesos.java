package clases;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * Encargada de poblar y retornar la tabla inyectada inicial (`PCB`).
 * Contiene el generador algorítmico principal (java.util.Random) que distribuye
 * las
 * probabilidades base para los atributos: Tiempos, Estados y Prioridades de
 * forma dinámica.
 * <p>
 * Solo se llama una vez antes de invocar explícitamente el algoritmo de
 * simulación.
 * </p>
 */
public class GestorProcesos {

    /**
     * Se almacena aquí el bloque ya estructurado de procesos una vez completada la
     * inyección.
     */
    private PCB pcb;

    /** Componente utilitario de semillas aleatorio de Java. */
    private Random random;

    /**
     * Constructor por defecto. Inicializa en vacío el generador de semillas.
     */
    public GestorProcesos() {
        this.random = new Random();
        this.pcb = null;
    }

    /**
     * Disparador masivo de procesos iniciales.
     * Deberá ser invocado con el tiempo inicial de la simulación (t=0).
     *
     * @param tiempoActual El contador del reloj inicial donde nacen los procesos.
     */
    public void inicializarProcesos(int tiempoActual) {

        // Creamos entre 5 y 10 procesos al azar, asegurando suficiente masa de prueba.
        // Se puede modificar fácilmente si se desea un entorno de alta concurrencia.
        int numProcesos = random.nextInt(6) + 5; // [0-5] + 5 = 5 hasta 10
        List<Proceso> lista = new ArrayList<>();

        for (int i = 1; i <= numProcesos; i++) {
            // Tiempos restantes variables [3 a 10]. Sirve bien para probar "Mas Corto
            // Primero"
            int tiempo = random.nextInt(8) + 3;

            // Probabilidad estado: 70% LISTO, 30% BLOQUEADO
            // Se usó nextInt(10): valores 0,1,2 = BLOQUEADO (3/10), resto = LISTO (7/10)
            // Para subir el % de bloqueados, basta con cambiar el 3 a un numero mayor.
            int chanceBloqueado = random.nextInt(10);
            EstadoProceso estado = (chanceBloqueado < 3) ? EstadoProceso.BLOQUEADO : EstadoProceso.LISTO;

            // Prioridad [1 a 10]
            int prioridad = random.nextInt(10) + 1;

            // Boletos de lotería [1 a 5]
            int boletos = random.nextInt(5) + 1;

            // Identificador en base al mod del usuario [Usuario1 Usuario2 o Usuario3]
            String usuario = "Usuario" + (random.nextInt(3) + 1);

            // Inyección al modelo Base
            Proceso nuevo = new Proceso(i, tiempo, estado, prioridad, boletos, usuario, tiempoActual);
            lista.add(nuevo);
        }

        // Empaquetamiento final
        this.pcb = new PCB(lista);

        System.out.println("\n--- [INICIO] Se han creado " + numProcesos + " procesos iniciales. ---");
        // Impresión inicial de validación visual
        this.pcb.mostrarTabla();
    }

    /**
     * Devuelve la estructura compacta llena de datos poblados.
     * Es ideal inyectárselo a los simuladores y algoritmos.
     * 
     * @return El control de bloque de procesos de forma centralizada.
     */
    public PCB getPcb() {
        return pcb;
    }
}
