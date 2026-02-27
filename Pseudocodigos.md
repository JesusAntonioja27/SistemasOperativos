# Pseudocódigos — Simulador de Planificador de Procesos
**Versión 3 — Incluye clases base + 10 algoritmos | Orientado a POO básica**

---

> **Cómo leer este documento:**
> Cada sección muestra el pseudocódigo de una clase o algoritmo.
> Los métodos usan la sintaxis `objeto.metodo()` para que se vea
> directamente cómo se conectan los objetos entre sí.
> Los 10 algoritmos reutilizan las mismas clases base.

---

# PARTE 1 — CLASES BASE
> Estas clases se crean **una sola vez** y todos los algoritmos las usan.

---

## EstadoProceso.java — Enum

```
// Un enum es una lista fija de valores con nombre.
// En Java: public enum EstadoProceso { LISTO, BLOQUEADO, EN_EJECUCION, TERMINADO }
// Lo usaremos como: proceso.estado = EstadoProceso.LISTO

ENUM EstadoProceso:
    LISTO
    BLOQUEADO
    EN_EJECUCION
    TERMINADO
```

---

## Proceso.java — Clase

> Representa a un proceso del sistema. Todos los algoritmos trabajan
> con objetos de este tipo.

```
CLASE Proceso:

    // --- Atributos ---
    int    id               // número único del proceso (1, 2, 3...)
    int    tiempoRestante   // cuántas unidades le falta ejecutar
    int    tiempoInicial    // copia del tiempo original (para estadísticas)
    Estado estado           // LISTO, BLOQUEADO, EN_EJECUCION o TERMINADO
    int    prioridad        // número del 1 al 10 (mayor = más prioritario)
    int    boletos          // cantidad de boletos para lotería (1 a 5)
    String usuario          // a qué usuario pertenece ("Usuario1", "Usuario2"...)
    int    vecesUsoCPU      // cuántas veces ha tomado el procesador
    int    tiempoUsoCPU     // tiempo total acumulado en el CPU
    int    tiempoCreacion   // en qué momento (t) fue creado el proceso
    double cpuDerecho       // cuánto CPU le corresponde (solo para Garantizada)
    double proporcion       // qué tanto ha tenido vs lo que le toca (solo Garantizada)

    // --- Constructor ---
    CONSTRUCTOR Proceso(id, tiempoRestante, estado, prioridad, boletos, usuario, tiempoActual):
        this.id             ← id
        this.tiempoRestante ← tiempoRestante
        this.tiempoInicial  ← tiempoRestante   // guardar copia
        this.estado         ← estado
        this.prioridad      ← prioridad
        this.boletos        ← boletos
        this.usuario        ← usuario
        this.vecesUsoCPU    ← 0
        this.tiempoUsoCPU   ← 0
        this.tiempoCreacion ← tiempoActual
        this.cpuDerecho     ← 0.0
        this.proporcion     ← 0.0
```

---

## PCB.java — Clase (Process Control Block)

> Es la "tabla" de todos los procesos. Todos los algoritmos la consultan
> para saber qué procesos existen y en qué estado están.

```
CLASE PCB:

    // --- Atributos ---
    Lista<Proceso> procesos   // la lista completa de todos los procesos

    // --- Constructor ---
    CONSTRUCTOR PCB(listaDeProcesos):
        this.procesos ← listaDeProcesos

    // --- Métodos ---

    METODO obtenerProcesos() → Lista<Proceso>:
        RETORNAR procesos

    METODO mostrarTabla():
        IMPRIMIR "╔══════════════════════════════════════════════════╗"
        IMPRIMIR "║            TABLA DE CONTROL DE PROCESOS          ║"
        IMPRIMIR "╠═══╦═════╦═════════╦══════╦═══════╦═══════╦══════╣"
        IMPRIMIR "║ID ║TRest║ Estado  ║Prior ║Boletos║Usuario║VecCPU║"
        IMPRIMIR "╠═══╬═════╬═════════╬══════╬═══════╬═══════╬══════╣"
        PARA CADA proceso EN procesos:
            IMPRIMIR fila con todos los campos del proceso
        IMPRIMIR "╚══════════════════════════════════════════════════╝"
```

---

## GestorProcesos.java — Clase

> Se encarga de crear todos los procesos al inicio con valores aleatorios.
> Solo se llama una vez antes de arrancar la simulación.

```
CLASE GestorProcesos:

    // --- Atributos ---
    PCB    pcb       // la tabla de procesos que este gestor va a construir
    Random random    // generador de números aleatorios de Java

    // --- Constructor ---
    CONSTRUCTOR GestorProcesos():
        this.random ← new Random()
        this.pcb    ← null

    // --- Métodos ---

    METODO inicializarProcesos(tiempoActual):
        numProcesos ← random.entre(1, 10)
        lista       ← nueva Lista vacía

        PARA i = 1 HASTA numProcesos:
            tiempo    ← random.entre(3, 10)
            estadoNum ← random.entre(1, 2)
            estado    ← (estadoNum = 1) ? LISTO : BLOQUEADO
            prioridad ← random.entre(1, 10)
            boletos   ← random.entre(1, 5)
            usuario   ← "Usuario" + random.entre(1, 3)

            nuevo ← new Proceso(i, tiempo, estado, prioridad, boletos, usuario, tiempoActual)
            lista.agregar(nuevo)

        pcb ← new PCB(lista)
        pcb.mostrarTabla()   // mostrar tabla inicial

    METODO getPcb() → PCB:
        RETORNAR pcb
```

---

## GestorInterrupciones.java — Clase

> Maneja el desbloqueo de procesos. Cuando un proceso está BLOQUEADO,
> se intenta desbloquearlo hasta 3 veces. Si no se logra → inanición.

```
CLASE GestorInterrupciones:

    // --- Atributos ---
    Random random

    // --- Constructor ---
    CONSTRUCTOR GestorInterrupciones():
        this.random ← new Random()

    // --- Métodos ---

    METODO intentarDesbloquear(proceso) → booleano:
        intentos ← 0

        MIENTRAS intentos < 3:
            IMPRIMIR "  Intento " + (intentos+1) + " de desbloquear P" + proceso.id
            resultado ← random.entre(0, 1)

            SI resultado = 1:
                proceso.estado ← LISTO
                IMPRIMIR "  ✓ Proceso P" + proceso.id + " desbloqueado"
                RETORNAR verdadero

            IMPRIMIR "  ✗ P" + proceso.id + " sigue bloqueado"
            intentos ← intentos + 1

        // 3 intentos fallidos → muerte por inanición
        IMPRIMIR ">>> Muerte del proceso P" + proceso.id + " por inanición <<<"
        proceso.estado ← TERMINADO
        RETORNAR falso
```

---

## Simulacion.java — Clase

> Guarda los valores globales de la simulación.
> Todos los algoritmos la consultan para saber el tiempo actual,
> el quantum y cuándo parar.

```
CLASE Simulacion:

    // --- Atributos ---
    int tiempoMonitoreo   // cuánto dura la simulación (aleatorio 20-35)
    int quantum           // tiempo de CPU por turno en modo apropiativo (aleatorio 2-5)
    int tiempoActual      // reloj de la simulación, empieza en 0
    int cambiosProceso    // contador de cuántas veces cambió el proceso en CPU

    // --- Constructor ---
    CONSTRUCTOR Simulacion():
        Random r         ← new Random()
        tiempoMonitoreo  ← r.entre(20, 35)
        quantum          ← r.entre(2, 5)
        tiempoActual     ← 0
        cambiosProceso   ← 0
        IMPRIMIR "Tiempo de monitoreo: " + tiempoMonitoreo
        IMPRIMIR "Quantum            : " + quantum

    // --- Métodos ---

    METODO incrementarTiempo():
        tiempoActual ← tiempoActual + 1

    METODO registrarCambioContexto():
        cambiosProceso ← cambiosProceso + 1
```

---

## ReporteFinal.java — Clase

> Al terminar la simulación, recorre la lista de procesos
> y los clasifica para mostrar el resumen final.

```
CLASE ReporteFinal:

    // Sin atributos, solo métodos

    METODO generarReporte(procesos, totalCambios):
        terminados       ← procesos donde (estado = TERMINADO Y vecesUsoCPU > 0)
        nuncaEjecutados  ← procesos donde (vecesUsoCPU = 0)
        aunEnEjecucion   ← procesos donde (estado ≠ TERMINADO Y vecesUsoCPU > 0)

        IMPRIMIR "======================================="
        IMPRIMIR "           REPORTE FINAL              "
        IMPRIMIR "======================================="
        IMPRIMIR "Procesos finalizados      : " + IDs de terminados
        IMPRIMIR "Procesos nunca ejecutados : " + IDs de nuncaEjecutados
        IMPRIMIR "Procesos aún en ejecución : " + IDs de aunEnEjecucion
        IMPRIMIR "Total cambios de proceso  : " + totalCambios
        IMPRIMIR "======================================="
```

---

## AlgoritmosPlanificacion.java — Interfaz

> Esta interfaz es el "contrato" que deben cumplir los 10 algoritmos.
> Todos tienen que implementar el método `ejecutar`.
> Así el `Planificador.java` puede llamar a cualquier algoritmo
> sin importar cuál sea, porque todos tienen el mismo método.

```
INTERFAZ AlgoritmosPlanificacion:

    // Método que TODOS los algoritmos deben implementar obligatoriamente
    METODO ejecutar(sim, gestorProcesos, gestorInterrupciones)
    //               ↑         ↑                  ↑
    //          estado      lista de           desbloqueo
    //          global      procesos           e inanición
```

---

## Planificador.java — Clase

> Actúa como intermediario entre el menú (Main) y el algoritmo elegido.
> Gracias a la interfaz, no importa qué algoritmo sea: siempre se llama
> igual. Esto es el **Patrón Estrategia** de POO.

```
CLASE Planificador:

    // --- Atributos ---
    AlgoritmosPlanificacion algoritmoActual   // el algoritmo elegido por el usuario

    // --- Métodos ---

    METODO setAlgoritmo(algoritmo):
        this.algoritmoActual ← algoritmo
        // Ejemplo de uso desde Main:
        // planificador.setAlgoritmo(new RoundRobinAprop())

    METODO iniciar(sim, gestorProcesos, gestorInterrupciones):
        algoritmoActual.ejecutar(sim, gestorProcesos, gestorInterrupciones)
        // El Planificador no sabe NI LE IMPORTA qué algoritmo es,
        // solo lo llama. Eso es polimorfismo.
```

---

## Main.java — Clase

> Punto de entrada del programa. Muestra el menú, instancia todos los
> objetos y arranca la simulación.

```
CLASE Main:

    METODO main(args):

        // 1. Crear los objetos globales
        scanner    ← new Scanner(System.in)
        sim        ← new Simulacion()               // genera tiempoMonitoreo y quantum
        gestorP    ← new GestorProcesos()
        gestorI    ← new GestorInterrupciones()
        planif     ← new Planificador()
        reporte    ← new ReporteFinal()

        // 2. Inicializar procesos (crea el PCB con procesos aleatorios)
        gestorP.inicializarProcesos(sim.tiempoActual)

        // 3. Mostrar menú de algoritmos
        IMPRIMIR "Selecciona el algoritmo de planificación:"
        IMPRIMIR " 1. Round Robin Apropiativo"
        IMPRIMIR " 2. Round Robin No Apropiativo"
        IMPRIMIR " 3. Prioridades Apropiativo"
        IMPRIMIR " 4. Prioridades No Apropiativo"
        IMPRIMIR " 5. Múltiples Colas de Prioridad"
        IMPRIMIR " 6. Proceso Más Corto Primero (SJF)"
        IMPRIMIR " 7. Planificación Garantizada"
        IMPRIMIR " 8. Lotería Apropiativo"
        IMPRIMIR " 9. Lotería No Apropiativo"
        IMPRIMIR "10. Participación Equitativa"

        opcion ← scanner.nextInt()

        // 4. Asignar el algoritmo elegido al planificador
        SEGUN opcion:
            1:  planif.setAlgoritmo(new RoundRobinAprop())
            2:  planif.setAlgoritmo(new RoundRobinNoAprop())
            3:  planif.setAlgoritmo(new PrioridadesAprop())
            4:  planif.setAlgoritmo(new PrioridadesNoAprop())
            5:  planif.setAlgoritmo(new MultiplesColas())
            6:  planif.setAlgoritmo(new ProcesoMasCorto())
            7:  planif.setAlgoritmo(new PlanificacionGarantizada())
            8:  planif.setAlgoritmo(new LoteriaAprop())
            9:  planif.setAlgoritmo(new LoteriaNoAprop())
            10: planif.setAlgoritmo(new ParticipacionEquitativa())

        // 5. Ejecutar la simulación
        planif.iniciar(sim, gestorP, gestorI)

        // 6. Mostrar reporte final
        reporte.generarReporte(gestorP.getPcb().obtenerProcesos(), sim.cambiosProceso)
```

---

# PARTE 2 — LOS 10 ALGORITMOS

> Cada algoritmo implementa la interfaz `AlgoritmosPlanificacion`,
> por lo tanto todos tienen el método `ejecutar(sim, gp, gi)`.
> Dentro de ese método usan los objetos de la Parte 1.
>
> **Convenciones:**
> - `gp.getPcb().obtenerProcesos()` → obtener la lista de procesos
> - `gi.intentarDesbloquear(p)` → intentar desbloquear el proceso `p`
> - `sim.tiempoActual` → reloj actual
> - `sim.tiempoMonitoreo` → cuándo parar
> - `sim.quantum` → tiempo máximo por turno (solo aprop.)
> - `sim.registrarCambioContexto()` → contar un cambio de proceso

---

## Algoritmo 1 — RoundRobinAprop.java

```
CLASE RoundRobinAprop IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos ← gp.getPcb().obtenerProcesos()

        // Crear la cola: solo los procesos que están LISTOS al inicio
        cola ← nueva Cola vacía
        PARA CADA p EN procesos:
            SI p.estado = LISTO: cola.agregar(p)

        IMPRIMIR "=== Round Robin APROPIATIVO | quantum=" + sim.quantum + " ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            SI cola está vacía: ROMPER

            proceso ← cola.sacarPrimero()

            // Si está bloqueado, intentar desbloquearlo
            SI proceso.estado = BLOQUEADO:
                desbloqueado ← gi.intentarDesbloquear(proceso)
                SI NO desbloqueado: CONTINUAR   // saltar, ya está TERMINADO
                // Si se desbloqueó, continúa abajo

            // Calcular cuánto ejecutar: lo que quede o el quantum, lo que sea menor
            tiempoEjec ← minimo(sim.quantum, proceso.tiempoRestante)

            proceso.estado ← EN_EJECUCION
            IMPRIMIR "[t=" + sim.tiempoActual + "] Ejecutando P" + proceso.id +
                     " | ejecuta=" + tiempoEjec + " | restante antes=" + proceso.tiempoRestante

            // Simular la ejecución unidad por unidad
            REPETIR tiempoEjec veces:
                proceso.tiempoRestante ← proceso.tiempoRestante - 1
                proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
            sim.registrarCambioContexto()

            // ¿Terminó?
            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO en t=" + sim.tiempoActual
            SINO:
                // Quantum agotado: va al final de la cola (esto es lo APROPIATIVO)
                proceso.estado ← LISTO
                cola.agregarAlFinal(proceso)
                IMPRIMIR "  → Quantum agotado, P" + proceso.id + " regresa a la cola"

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 2 — RoundRobinNoAprop.java

```
CLASE RoundRobinNoAprop IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos ← gp.getPcb().obtenerProcesos()
        cola     ← nueva Cola con todos los LISTOS

        IMPRIMIR "=== Round Robin NO APROPIATIVO ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            SI cola está vacía: ROMPER

            proceso ← cola.sacarPrimero()

            SI proceso.estado = BLOQUEADO:
                desbloqueado ← gi.intentarDesbloquear(proceso)
                SI NO desbloqueado: CONTINUAR

            proceso.estado ← EN_EJECUCION
            IMPRIMIR "[t=" + sim.tiempoActual + "] Ejecutando P" + proceso.id +
                     " hasta finalizar | restante=" + proceso.tiempoRestante

            // Sin quantum: corre TODO el tiempo que le queda (NO APROPIATIVO)
            MIENTRAS proceso.tiempoRestante > 0
            Y        sim.tiempoActual < sim.tiempoMonitoreo:
                proceso.tiempoRestante ← proceso.tiempoRestante - 1
                proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
            sim.registrarCambioContexto()

            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO"
            SINO:
                proceso.estado ← LISTO
                // No vuelve a la cola: el tiempo de monitoreo se agotó

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 3 — PrioridadesAprop.java

```
CLASE PrioridadesAprop IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos ← gp.getPcb().obtenerProcesos()

        IMPRIMIR "=== Prioridades APROPIATIVO | quantum=" + sim.quantum + " ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            // En cada ciclo buscar el proceso LISTO con MAYOR prioridad
            candidatos ← procesos donde estado = LISTO
            SI candidatos vacío: ROMPER

            proceso ← el de MAYOR prioridad en candidatos
            //         (si hay empate, el de menor ID)

            SI proceso.estado = BLOQUEADO:
                gi.intentarDesbloquear(proceso)
                CONTINUAR

            tiempoEjec     ← minimo(sim.quantum, proceso.tiempoRestante)
            proceso.estado ← EN_EJECUCION

            IMPRIMIR "[t=" + sim.tiempoActual + "] P" + proceso.id +
                     " (prioridad=" + proceso.prioridad + ") ejecuta " + tiempoEjec

            REPETIR tiempoEjec veces:
                proceso.tiempoRestante ← proceso.tiempoRestante - 1
                proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
            sim.registrarCambioContexto()

            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO"
            SINO:
                // Vuelve a LISTO: en el siguiente ciclo se re-evalúa
                // Si llegó uno de mayor prioridad, ese ganará (APROPIATIVO)
                proceso.estado ← LISTO

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 4 — PrioridadesNoAprop.java

```
CLASE PrioridadesNoAprop IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos      ← gp.getPcb().obtenerProcesos()
        esperaPorID   ← nuevo Mapa<Int, Int>   // proceso.id → cuántos ciclos lleva esperando
        PARA CADA p EN procesos: esperaPorID[p.id] ← 0

        IMPRIMIR "=== Prioridades NO APROPIATIVO | Aging activado ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            candidatos ← procesos donde estado = LISTO
            SI candidatos vacío: ROMPER

            // AGING: aumentar prioridad de los que llevan 3 ciclos sin CPU
            PARA CADA p EN candidatos:
                esperaPorID[p.id] ← esperaPorID[p.id] + 1
                SI esperaPorID[p.id] MOD 3 = 0:
                    p.prioridad ← p.prioridad + 1
                    IMPRIMIR "  [Aging] P" + p.id + " nueva prioridad=" + p.prioridad

            proceso ← el de MAYOR prioridad en candidatos
            esperaPorID[proceso.id] ← 0   // resetear su contador de espera

            SI proceso.estado = BLOQUEADO:
                gi.intentarDesbloquear(proceso)
                CONTINUAR

            proceso.estado ← EN_EJECUCION
            IMPRIMIR "[t=" + sim.tiempoActual + "] P" + proceso.id +
                     " (prioridad=" + proceso.prioridad + ") corre hasta finalizar"

            // NO APROPIATIVO: corre todo sin interrupciones
            MIENTRAS proceso.tiempoRestante > 0
            Y        sim.tiempoActual < sim.tiempoMonitoreo:
                proceso.tiempoRestante ← proceso.tiempoRestante - 1
                proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
            sim.registrarCambioContexto()

            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO"
            SINO:
                proceso.estado ← LISTO

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 5 — MultiplesColas.java

```
CLASE MultiplesColas IMPLEMENTA AlgoritmosPlanificacion:

    // Constante: cuántos niveles de cola hay
    int NUM_NIVELES = 4

    METODO ejecutar(sim, gp, gi):

        procesos ← gp.getPcb().obtenerProcesos()

        // Crear 4 listas (una por nivel de prioridad)
        colas ← array de 4 listas vacías

        // Distribuir procesos LISTOS en su cola inicial según prioridad
        PARA CADA p EN procesos:
            SI p.estado = LISTO:
                nivel ← calcularNivel(p.prioridad)
                colas[nivel].agregar(p)

        IMPRIMIR "=== Múltiples Colas de Prioridad ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            // Buscar el primer proceso en la cola de mayor prioridad (nivel 0 primero)
            proceso     ← null
            nivelActual ← -1
            PARA nivel = 0 HASTA 3:
                listos ← colas[nivel] donde estado = LISTO
                SI listos NO vacío:
                    proceso     ← listos.primero()
                    nivelActual ← nivel
                    ROMPER

            SI proceso = null: ROMPER

            SI proceso.estado = BLOQUEADO:
                desbloqueado ← gi.intentarDesbloquear(proceso)
                SI NO desbloqueado:
                    colas[nivelActual].eliminar(proceso)
                    CONTINUAR

            // Tiempo asignado = prioridad × (vecesUsoCPU + 1)   ← fórmula del proyecto
            tiempoAsignado ← proceso.prioridad × (proceso.vecesUsoCPU + 1)
            tiempoEjec     ← minimo(tiempoAsignado, proceso.tiempoRestante)
            proceso.estado ← EN_EJECUCION

            IMPRIMIR "[t=" + sim.tiempoActual + "] P" + proceso.id +
                     " (cola=" + nivelActual + " | prioridad=" + proceso.prioridad +
                     " | veces=" + proceso.vecesUsoCPU + ") → asignado=" + tiempoAsignado

            REPETIR tiempoEjec veces:
                proceso.tiempoRestante ← proceso.tiempoRestante - 1
                proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
            sim.registrarCambioContexto()
            colas[nivelActual].eliminar(proceso)

            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO"
            SINO:
                // Bajarlo un nivel (como en Tanenbaum: al usar su tiempo asignado, baja)
                proceso.estado ← LISTO
                nuevoNivel     ← minimo(nivelActual + 1, 3)
                colas[nuevoNivel].agregar(proceso)
                IMPRIMIR "  → P" + proceso.id + " baja a cola " + nuevoNivel

            gp.getPcb().mostrarTabla()

    // Asignar nivel según rango de prioridad (1-10 dividido en 4 grupos)
    METODO calcularNivel(prioridad) → int:
        SI prioridad >= 9: RETORNAR 0   // mayor prioridad → cola 0
        SI prioridad >= 7: RETORNAR 1
        SI prioridad >= 4: RETORNAR 2
        RETORNAR 3                       // menor prioridad → cola 3
```

---

## Algoritmo 6 — ProcesoMasCorto.java (SJF No Apropiativo)

```
CLASE ProcesoMasCorto IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos ← gp.getPcb().obtenerProcesos()

        IMPRIMIR "=== Proceso Más Corto Primero (SJF) — NO APROPIATIVO ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            candidatos ← procesos donde estado = LISTO
            SI candidatos vacío: ROMPER

            // Seleccionar el que tenga MENOR tiempoRestante
            // (en empate: el de menor ID)
            proceso ← el de MENOR tiempoRestante en candidatos

            SI proceso.estado = BLOQUEADO:
                gi.intentarDesbloquear(proceso)
                CONTINUAR

            proceso.estado ← EN_EJECUCION
            IMPRIMIR "[t=" + sim.tiempoActual + "] SJF: P" + proceso.id +
                     " (el más corto, restante=" + proceso.tiempoRestante + ") corre completo"

            // NO APROPIATIVO: corre todo de una vez
            MIENTRAS proceso.tiempoRestante > 0
            Y        sim.tiempoActual < sim.tiempoMonitoreo:
                proceso.tiempoRestante ← proceso.tiempoRestante - 1
                proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
            sim.registrarCambioContexto()

            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO en t=" + sim.tiempoActual
            SINO:
                proceso.estado ← LISTO

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 7 — PlanificacionGarantizada.java

```
// Idea (Tanenbaum pág. 159):
// Cada proceso tiene derecho a 1/n del CPU.
// Calculamos una "proporción" = lo que usó / lo que le tocaba.
// Siempre ejecutamos el que tenga la MENOR proporción (el más desfavorecido).

CLASE PlanificacionGarantizada IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos        ← gp.getPcb().obtenerProcesos()
        procesoAnterior ← null

        IMPRIMIR "=== Planificación Garantizada ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            activos    ← procesos donde estado ≠ TERMINADO
            SI activos vacío: ROMPER
            n          ← activos.tamaño()

            candidatos ← activos donde estado = LISTO
            SI candidatos vacío: ROMPER

            // Calcular proporción de cada candidato
            PARA CADA p EN candidatos:
                tiempoTranscurrido ← sim.tiempoActual - p.tiempoCreacion + 1
                p.cpuDerecho       ← tiempoTranscurrido / n   // lo que le DEBERÍA tocar
                SI p.cpuDerecho > 0:
                    p.proporcion ← p.tiempoUsoCPU / p.cpuDerecho  // lo que realmente tuvo
                SINO:
                    p.proporcion ← 0.0

            // Ejecutar el que tiene MENOR proporción (el más perjudicado)
            proceso ← el de MENOR proporcion en candidatos

            SI proceso.estado = BLOQUEADO:
                gi.intentarDesbloquear(proceso)
                CONTINUAR

            proceso.estado ← EN_EJECUCION
            IMPRIMIR "[t=" + sim.tiempoActual + "] P" + proceso.id +
                     " | cpuDerecho=" + proceso.cpuDerecho +
                     " | proporción=" + proceso.proporcion

            // Ejecutar 1 unidad para recalcular proporciones en cada paso
            proceso.tiempoRestante ← proceso.tiempoRestante - 1
            proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
            sim.incrementarTiempo()

            // Solo contar cambio si cambió el proceso en ejecución
            SI proceso ≠ procesoAnterior:
                proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
                sim.registrarCambioContexto()
            procesoAnterior ← proceso

            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO"
            SINO:
                proceso.estado ← LISTO

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 8 — LoteriaAprop.java

```
CLASE LoteriaAprop IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos ← gp.getPcb().obtenerProcesos()
        random   ← new Random()

        IMPRIMIR "=== Lotería APROPIATIVO | quantum=" + sim.quantum + " ==="
        IMPRIMIR "Boletos asignados:"
        PARA CADA p EN procesos:
            IMPRIMIR "  P" + p.id + " → " + p.boletos + " boletos"

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            candidatos ← procesos donde estado = LISTO
            SI candidatos vacío: ROMPER

            // Sumar todos los boletos disponibles
            totalBoletos  ← suma de p.boletos para todos en candidatos
            boletoBuscado ← random.entre(1, totalBoletos)

            IMPRIMIR "[t=" + sim.tiempoActual + "] Sorteo: boleto " +
                     boletoBuscado + " de " + totalBoletos

            // Recorrer la lista acumulando boletos hasta encontrar al ganador
            acumulado ← 0
            ganador   ← null
            PARA CADA p EN candidatos:
                acumulado ← acumulado + p.boletos
                SI boletoBuscado ≤ acumulado:
                    ganador ← p
                    ROMPER

            SI ganador.estado = BLOQUEADO:
                gi.intentarDesbloquear(ganador)
                CONTINUAR

            tiempoEjec     ← minimo(sim.quantum, ganador.tiempoRestante)
            ganador.estado ← EN_EJECUCION
            IMPRIMIR "  Ganador: P" + ganador.id + " (" + ganador.boletos +
                     " boletos) → ejecuta " + tiempoEjec + " uds"

            REPETIR tiempoEjec veces:
                ganador.tiempoRestante ← ganador.tiempoRestante - 1
                ganador.tiempoUsoCPU   ← ganador.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            ganador.vecesUsoCPU ← ganador.vecesUsoCPU + 1
            sim.registrarCambioContexto()

            SI ganador.tiempoRestante = 0:
                ganador.estado ← TERMINADO
                IMPRIMIR "  → P" + ganador.id + " TERMINADO"
            SINO:
                ganador.estado ← LISTO   // vuelve al pool para el siguiente sorteo

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 9 — LoteriaNoAprop.java

```
CLASE LoteriaNoAprop IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos ← gp.getPcb().obtenerProcesos()
        random   ← new Random()

        IMPRIMIR "=== Lotería NO APROPIATIVO ==="

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            candidatos   ← procesos donde estado = LISTO
            SI candidatos vacío: ROMPER

            totalBoletos  ← suma de p.boletos para todos en candidatos
            boletoBuscado ← random.entre(1, totalBoletos)

            acumulado ← 0
            ganador   ← null
            PARA CADA p EN candidatos:
                acumulado ← acumulado + p.boletos
                SI boletoBuscado ≤ acumulado:
                    ganador ← p
                    ROMPER

            SI ganador.estado = BLOQUEADO:
                gi.intentarDesbloquear(ganador)
                CONTINUAR

            ganador.estado ← EN_EJECUCION
            IMPRIMIR "[t=" + sim.tiempoActual + "] Ganador: P" + ganador.id +
                     " (" + ganador.boletos + " boletos) → corre hasta finalizar"

            // NO APROPIATIVO: corre todo sin interrupción
            MIENTRAS ganador.tiempoRestante > 0
            Y        sim.tiempoActual < sim.tiempoMonitoreo:
                ganador.tiempoRestante ← ganador.tiempoRestante - 1
                ganador.tiempoUsoCPU   ← ganador.tiempoUsoCPU + 1
                sim.incrementarTiempo()

            ganador.vecesUsoCPU ← ganador.vecesUsoCPU + 1
            sim.registrarCambioContexto()

            SI ganador.tiempoRestante = 0:
                ganador.estado ← TERMINADO
                IMPRIMIR "  → P" + ganador.id + " TERMINADO"
            SINO:
                ganador.estado ← LISTO

            gp.getPcb().mostrarTabla()
```

---

## Algoritmo 10 — ParticipacionEquitativa.java

```
// Idea (Tanenbaum pág. 160):
// Se garantiza que cada USUARIO reciba la misma proporción de CPU,
// sin importar cuántos procesos tenga.
// Se hace round-robin entre USUARIOS, y dentro de cada usuario
// también round-robin entre sus procesos.
//
// Ejemplo: Usuario1 tiene P1,P2,P3 y Usuario2 tiene P4
// Secuencia justa: P1 → P4 → P2 → P4 → P3 → P4 → P1 → ...

CLASE ParticipacionEquitativa IMPLEMENTA AlgoritmosPlanificacion:

    METODO ejecutar(sim, gp, gi):

        procesos        ← gp.getPcb().obtenerProcesos()
        procesoAnterior ← null

        // Agrupar procesos por nombre de usuario en un Mapa
        // Ejemplo: {"Usuario1": [P1, P2, P3], "Usuario2": [P4]}
        mapaUsuarios ← agrupar procesos por p.usuario

        // Índice para round-robin entre usuarios
        listaUsuarios  ← lista de nombres de usuarios
        indiceUsuario  ← 0

        // Índice para round-robin dentro de cada usuario
        indiceProcesoDeUsuario ← Mapa<String, Int> inicializado en 0

        IMPRIMIR "=== Participación Equitativa ==="
        IMPRIMIR "Usuarios: " + listaUsuarios

        MIENTRAS sim.tiempoActual < sim.tiempoMonitoreo:

            // Saltar usuarios que ya no tienen procesos activos
            intentos ← 0
            MIENTRAS intentos < listaUsuarios.tamaño():
                nombreUsuario ← listaUsuarios[indiceUsuario]
                procesosDeUsuario ← mapaUsuarios[nombreUsuario]
                    donde estado ≠ TERMINADO
                SI procesosDeUsuario NO vacío: ROMPER
                // Este usuario ya terminó, avanzar al siguiente
                indiceUsuario ← (indiceUsuario + 1) MOD listaUsuarios.tamaño()
                intentos ← intentos + 1

            SI intentos = listaUsuarios.tamaño(): ROMPER   // todos terminaron

            // Seleccionar el siguiente proceso del usuario actual
            nombreUsuario    ← listaUsuarios[indiceUsuario]
            procesosActivos  ← mapaUsuarios[nombreUsuario] donde estado = LISTO
            SI procesosActivos vacío:
                indiceUsuario ← (indiceUsuario + 1) MOD listaUsuarios.tamaño()
                CONTINUAR

            // Round-robin dentro del usuario
            idx     ← indiceProcesoDeUsuario[nombreUsuario] MOD procesosActivos.tamaño()
            proceso ← procesosActivos[idx]
            indiceProcesoDeUsuario[nombreUsuario] ← idx + 1

            SI proceso.estado = BLOQUEADO:
                gi.intentarDesbloquear(proceso)
                indiceUsuario ← (indiceUsuario + 1) MOD listaUsuarios.tamaño()
                CONTINUAR

            proceso.estado ← EN_EJECUCION
            IMPRIMIR "[t=" + sim.tiempoActual + "] " + nombreUsuario +
                     " → P" + proceso.id

            // Ejecutar 1 unidad para mantener el turno equitativo entre usuarios
            proceso.tiempoRestante ← proceso.tiempoRestante - 1
            proceso.tiempoUsoCPU   ← proceso.tiempoUsoCPU + 1
            sim.incrementarTiempo()

            SI proceso ≠ procesoAnterior:
                proceso.vecesUsoCPU ← proceso.vecesUsoCPU + 1
                sim.registrarCambioContexto()
            procesoAnterior ← proceso

            SI proceso.tiempoRestante = 0:
                proceso.estado ← TERMINADO
                IMPRIMIR "  → P" + proceso.id + " TERMINADO"
            SINO:
                proceso.estado ← LISTO

            // Avanzar al siguiente usuario para el próximo ciclo
            indiceUsuario ← (indiceUsuario + 1) MOD listaUsuarios.tamaño()

            gp.getPcb().mostrarTabla()
```

---

# PARTE 3 — RESUMEN DE CONEXIONES ENTRE CLASES

```
Main
 ├── crea: Simulacion, GestorProcesos, GestorInterrupciones, Planificador, ReporteFinal
 ├── llama: gestorP.inicializarProcesos()        → construye el PCB con procesos aleatorios
 ├── llama: planif.setAlgoritmo(new XYZ())       → inyecta el algoritmo elegido
 ├── llama: planif.iniciar(sim, gestorP, gestorI)→ ejecuta la simulación
 └── llama: reporte.generarReporte(...)          → muestra el resumen final

Planificador
 └── llama: algoritmoActual.ejecutar(sim, gp, gi)

Cada algoritmo (RoundRobinAprop, LoteriaAprop, etc.)
 ├── lee: gp.getPcb().obtenerProcesos()         → lista de procesos
 ├── llama: gi.intentarDesbloquear(proceso)     → maneja bloqueos
 ├── modifica: proceso.tiempoRestante, .estado, .tiempoUsoCPU, .vecesUsoCPU
 ├── llama: sim.incrementarTiempo()             → avanza el reloj
 ├── llama: sim.registrarCambioContexto()       → cuenta el cambio
 └── llama: gp.getPcb().mostrarTabla()          → imprime el estado actual

ReporteFinal
 └── lee: gp.getPcb().obtenerProcesos() + sim.cambiosProceso

```

---

# PARTE 4 — TABLA DE LOS 10 ALGORITMOS

| # | Clase Java | Variante | Usa quantum | Diferencia clave |
|---|-----------|----------|:-----------:|-----------------|
| 1 | RoundRobinAprop | Apropiativo | ✓ | Proceso va al final de cola al agotar quantum |
| 2 | RoundRobinNoAprop | No Apropiativo | ✗ | Proceso corre completo antes de liberar CPU |
| 3 | PrioridadesAprop | Apropiativo | ✓ | Mayor prioridad gana; re-evalúa cada quantum |
| 4 | PrioridadesNoAprop | No Apropiativo | ✗ | Mayor prioridad corre completo + Aging |
| 5 | MultiplesColas | Una versión | ✓ | Tiempo = prioridad × vecesUso; baja de cola si lo agota |
| 6 | ProcesoMasCorto | No Apropiativo | ✗ | Menor tiempoRestante gana y corre completo |
| 7 | PlanificacionGarantizada | Una versión | ✗ | Ejecuta el de menor proporción (uso/derecho) |
| 8 | LoteriaAprop | Apropiativo | ✓ | Sorteo de boleto cada quantum |
| 9 | LoteriaNoAprop | No Apropiativo | ✗ | Sorteo de boleto; ganador corre completo |
| 10 | ParticipacionEquitativa | Una versión | ✗ | Round-robin entre usuarios, luego entre sus procesos |