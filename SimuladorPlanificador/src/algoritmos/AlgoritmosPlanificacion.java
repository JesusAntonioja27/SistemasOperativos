package algoritmos;

import clases.Simulacion;
import clases.GestorProcesos;
import clases.GestorInterrupciones;

public interface AlgoritmosPlanificacion {
    void ejecutar(Simulacion sim, GestorProcesos gp, GestorInterrupciones gi);
}
