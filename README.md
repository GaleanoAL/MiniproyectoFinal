# Yu-Gi-Oh! — Mini Proyecto 4
### Programación Orientada a Eventos — Java 21

---

## Integrantes

| Nombre | Código |
|--------|--------|
| Alejandro Galeano Castro | 2477228 |
| Juan Manuel Polania | 2477452 |


---

## Descripción del juego

Simulación de un duelo de Yu-Gi-Oh! en Java 21 con arquitectura **MVC** completa. Dos jugadores se enfrentan con mazos de 25 cartas (monstruos, mágicas y trampas), comenzando con 8000 LP cada uno. El objetivo es reducir los LP del oponente a 0 o dejarlo sin cartas en el mazo.

El juego corre simultáneamente en **consola** y en **interfaz gráfica** gracias al patrón Observer: ambas vistas reciben las mismas notificaciones del controlador sin acoplamiento directo.

---

## Instrucciones de ejecución

### Requisitos
- Java JDK 21 o superior
- Terminal / IDE (VS Code, IntelliJ, Eclipse)

### Compilación desde terminal
```bash
# Desde la raíz del proyecto (donde está src/)
find src -name "*.java" > sources.txt
javac --release 21 -d out @sources.txt
```

### Ejecución
```bash
# La carpeta /cartas debe estar en el directorio de ejecución
cd out
java -cp . App
```

### Con IDE
Importar la carpeta raíz como proyecto Java, marcar `src/` como Sources Root, y ejecutar `App.java`.  
Asegurarse de que la carpeta `cartas/` esté en el directorio de trabajo del IDE.

---

## Estructuras de datos implementadas (RF1)

Se implementan **5 estructuras** con justificación real, no forzada:

| Estructura | Ubicación | Justificación |
|---|---|---|
| **Stack\<Carta\>** | `Jugador.java` (campo `mazo`) | El mazo funciona LIFO: siempre se roba la carta del tope, exactamente como barajar físicamente. |
| **LinkedList\<Carta\>** | `Jugador.java` (campo `mano`) | La mano requiere inserción al final y eliminación por índice con frecuencia. LinkedList evita el desplazamiento de ArrayList en eliminaciones intermedias. |
| **Queue\<String\>** (LinkedList) | `JuegoControlador.java` (campo `eventos`) | El log de eventos del turno es FIFO: los eventos se muestran en el orden en que ocurrieron. Se limita a 60 entradas eliminando las más antiguas. |
| **HashSet\<String\>** | `JuegoControlador.java` (campo `cartasUsadas`) | Registra nombres de cartas únicas activadas en la partida. Set garantiza unicidad y búsqueda O(1), más eficiente que List para "¿ya se usó esta carta?". |
| **HashMap\<String, Class\>** | `CartaFactory.java` (campo `registro`) | Mapea nombre de carta → clase Java. Búsqueda O(1) sin ningún if/switch. Cargado dinámicamente via Reflection desde `/cartas/*.txt`. |
| **TreeMap\<Integer, List\<Monstruo\>\>** | `Jugador.java` (campo `indicePorNivel`) | Árbol BST interno que mantiene los monstruos del campo ordenados por nivel. Permite consultar "¿qué monstruos de mayor nivel tengo?" en O(log n). |

---

## Patrones de diseño implementados (RF3-A)

### 1. Observer
**Archivos:** `VistaJuego.java`, `JuegoControlador.java`, `VentanaYuGiOh.java`, `VistaConsola.java`

El controlador mantiene una lista de `VistaJuego` suscritas. Cada vez que el estado cambia (LP, campo, mano, turno), invoca `notificarVistas()` que llama `actualizar()` en cada vista registrada. Las vistas no se conocen entre sí ni conocen al controlador más allá de la interfaz.

```java
// Suscripción:
controlador.agregarVista(consola);
controlador.agregarVista(ventana);

// Notificación automática en cada cambio:
private void notificarVistas() {
    vistas.forEach(v -> v.actualizar(j1, j2, turnoJ1));
}
```

### 2. Memento
**Archivos:** `JuegoMemento.java`, `JuegoControlador.java`

Antes de ejecutar cada acción de juego, el controlador toma una instantánea (`crearMemento()`) del estado completo: LP, mano, campo y turno de ambos jugadores. Si el usuario elige "Deshacer", se restaura la instantánea (`restaurarMemento()`). Esta misma mecánica se integra con la persistencia en disco (RF2).

```java
// Antes de ejecutar una acción:
ultimoEstado = crearMemento();

// Al deshacer:
restaurarMemento(ultimoEstado);
```

### 3. Command
**Archivos:** `Comando.java`, `ComandoJugarCarta.java`, `JuegoControlador.java`

Cada acción del jugador se encapsula en un objeto `Comando` con `execute()`, `undo()` y `descripcion()`. El controlador mantiene una `Stack<Comando>` (`historialCmd`). El botón "Deshacer" saca el último comando y lo revierte combinado con el Memento.

```java
ComandoJugarCarta cmd = new ComandoJugarCarta(actual, index);
cmd.execute();
historialCmd.push(cmd);
// ...
Comando ultimo = historialCmd.pop();
ultimo.undo(); // + restaurarMemento para estado completo
```

### 4. Factory
**Archivos:** `CartaFactory.java`, `RegistroCartas.java`

`CartaFactory` recibe el nombre de una carta y devuelve una instancia sin ningún `if/switch`. `RegistroCartas.crearFactory()` lee todos los `.txt` de `/cartas/` y registra las clases via **Reflection** (ver RF3-B).

### 5. Strategy
**Archivos:** `Activable.java`, todas las cartas mágicas y trampas

Cada carta implementa `Activable.activar(jugador, oponente)` con su propio algoritmo. El controlador llama `a.activar(...)` sin saber qué carta concreta es. Agregar un nuevo efecto = nueva clase, sin tocar el controlador.

### 6. Singleton
**Archivo:** `GestorPartidas.java`

Un único `GestorPartidas.getInstance()` gestiona toda la E/S de archivos de texto. Garantiza que no haya escrituras simultáneas y que el archivo de resultados sea accedido desde un punto central.

---

## Reflection API (RF3-B)

**Archivo:** `RegistroCartas.java`

```java
// Lectura del archivo .txt:
Properties props = new Properties();
props.load(new FileReader(archivo));
String nombreClase = props.getProperty("clase"); // ej: "modelo.carta.PotOfGreed"

// Reflection: carga dinámica sin new ni switch
Class<? extends Carta> clase = (Class<? extends Carta>) Class.forName(nombreClase);
Carta instancia = clase.getDeclaredConstructor().newInstance();
```

Para agregar una nueva carta al juego basta con crear su `.java` y su `.txt` en `/cartas/`. No se requiere recompilar ni modificar ningún archivo existente.

---

## Persistencia (RF2)

### Guardar partida
En cualquier momento del duelo se puede guardar en `partida_guardada.txt`, un archivo de texto plano con formato `clave=valor` legible en bloc de notas.

### Cargar partida
Desde el menú principal o durante el duelo. Si el archivo de disco existe, restaura desde ahí; si no, usa el Memento en RAM.

### Historial de resultados
Al terminar cada duelo se escribe una línea en `resultados.txt`:
```
2026-06-01 14:23:11 | JP1 vs JP2 | Ganador: JP1 | Turnos: 12 | LP: 3200 - 0
```

### Estadísticas
El menú principal muestra victorias por duelista y la partida más larga, leídas desde `resultados.txt`.

---

## Estructura del proyecto

```
MINI4_D/
├── src/
│   ├── App.java
│   ├── controlador/
│   │   └── JuegoControlador.java       ← Observer + Memento + Command + Singleton
│   ├── modelo/
│   │   ├── carta/
│   │   │   ├── Carta.java              ← Base abstracta
│   │   │   ├── Monstruo.java
│   │   │   ├── Magica.java             ← Abstract + Strategy
│   │   │   ├── Trampa.java             ← Abstract + Strategy
│   │   │   ├── PotOfGreed.java
│   │   │   ├── Raigeki.java
│   │   │   ├── DarkHole.java
│   │   │   ├── Hinotama.java
│   │   │   ├── ChangeOfHeart.java
│   │   │   ├── StandarOfCourage.java
│   │   │   ├── TyphoonOfMagicalSpace.java
│   │   │   ├── AcesCoup.java
│   │   │   ├── BoostAtk.java
│   │   │   ├── AceleronMiauravilloso.java
│   │   │   ├── MirrorForce.java        ← Trampa
│   │   │   └── SakuretsuArmor.java     ← Trampa
│   │   ├── efecto/
│   │   │   └── Activable.java          ← Interfaz Strategy
│   │   ├── comando/
│   │   │   ├── Comando.java            ← Interfaz Command
│   │   │   └── ComandoJugarCarta.java
│   │   ├── factory/
│   │   │   ├── CartaFactory.java       ← Factory + HashMap (RF1)
│   │   │   └── RegistroCartas.java     ← Reflection API
│   │   ├── juego/
│   │   │   └── Jugador.java            ← Stack + LinkedList + TreeMap (RF1)
│   │   ├── memento/
│   │   │   └── JuegoMemento.java       ← Memento pattern
│   │   └── persistencia/
│   │       └── GestorPartidas.java     ← Singleton + E/S texto (RF2)
│   └── vista/
│       ├── VistaJuego.java             ← Interfaz Observer
│       ├── VistaConsola.java           ← Vista terminal
│       └── VentanaYuGiOh.java          ← Vista GUI Swing
├── cartas/
│   ├── PotOfGreed.txt
│   ├── Raigeki.txt
│   ├── DarkHole.txt
│   ├── Hinotama.txt
│   ├── ChangeOfHeart.txt
│   ├── StandarOfCourage.txt
│   ├── TyphoonOfMagicalSpace.txt
│   ├── MirrorForce.txt
│   ├── SakuretsuArmor.txt
│   └── AcesCoup.txt
└── resultados.txt
```

---

## Mecánica del juego

### Inicio
- Se ingresan los nombres de los jugadores
- Se generan mazos de 25 cartas mezcladas
- Cada jugador roba 5 cartas iniciales
- El primer turno se decide aleatoriamente

### Acciones por turno
| Acción | Descripción |
|--------|-------------|
| **Robar carta** | Automático al inicio del turno |
| **Jugar carta** | Una sola carta por turno (monstruo, magia o trampa) |
| **Atacar** | Un ataque por turno (prohibido en el primer turno) |
| **Cambiar modo** | ATK ↔ DEF, una vez por monstruo por turno |
| **Deshacer** | Revierte la última carta jugada (Command + Memento) |
| **Guardar / Cargar** | Persistencia en disco en cualquier momento |

### Sistema de sacrificios
- Nivel 1–3: sin sacrificio
- Nivel 4–6: 1 sacrificio
- Nivel 7+: 2 sacrificios

### Condiciones de victoria
- LP del oponente llegan a 0
- El oponente intenta robar con el mazo vacío

---

## Notas sobre uso de IA

Se usó asistencia de IA para resolver dudas conceptuales sobre la Reflection API de Java (`Class.forName`, `getDeclaredConstructor`, `newInstance`) y sobre la estructura del patrón Command. El código fue escrito y adaptado por el equipo, tambien se uso de apoyo de IA para poder hacer una inesvtigación profunda en  RF3 — Tema de investigación: Patrones de Diseño + Reflection, realizando constantemente prueba y error con el codigo.


---

## Tablero KanbanFlow

[Enlace al tablero KanbanFlow](https://kanbanflow.com/board/Y6QBhV4) 

---
