# Chat TCP en Java – Guía de uso

## Archivos
| Archivo | Rol |
|---|---|
| `ChatServer.java` | Servidor multi-cliente con broadcast |
| `ChatClient.java` | Cliente con hilo de escucha independiente |

## Requisitos
- JDK 8 o superior (`java -version` para verificar)

## Compilación
```bash
javac -source 8 -target 8 ChatServer.java
javac -source 8 -target 8 ChatClient.java
```

## Ejecución

### Terminal 1 – Servidor
```bash
java ChatServer
```

### Terminal 2, 3, … – Clientes (uno por terminal)
```bash
java ChatClient
```

## Flujo de uso
1. El servidor inicia y queda esperando en el puerto **5000**.
2. El cliente se conecta y el servidor pide un **nombre**.
3. Escribe mensajes → todos los clientes conectados los reciben.
4. Escribe `/salir` para desconectarte limpiamente de los clientes.
5. Ctrl+C para salir del servidor

## Conceptos aplicados
| Concepto | Dónde |
|---|---|
| `ServerSocket` / `Socket` | Apertura de conexión TCP |
| `BufferedReader` / `PrintWriter` | Lectura y escritura de texto por el socket |
| `ExecutorService` (pool de hilos) | Un hilo por cliente en el servidor |
| Hilo daemon | `MessageListener` en el cliente |
| `synchronized` / `Collections.synchronizedList` | Acceso seguro a la lista de clientes |
| Broadcast | `ChatServer.broadcast()` difunde a todos |
| `try-with-resources` | Cierre automático de sockets y streams |
