import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/*
Servidor de chat TCP que permite multiples clientes a la vez.

Funcionamiento:
-ServerSocket que escucha al puerto 5000.
-Por cada cliente admitido se crea un hilo con Client Handlet.
-El servidor transmite (broadcast) el mensaje recibido a todos los clientes conetados.

 */

public class ChatServer {
    //Se establece el puerto de escucha
    private static final int PORT = 5000;

    //Se crea una lista de thread-safe de todos los escritores activos (se crea uno por cliente)
    private static final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("║        SERVIDOR DE CHAT TCP          ║");
        System.out.printf ("║  Escuchando en el puerto %-4d        ║%n", PORT);
        System.out.println("║  Esperando clientes...               ║");


        //ExecutorService gestiona un conjunto de hilos (uno por cliente)
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //Bucle infinito
            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.printf("Servidor -> Cliente conectado desde %s%n", clientSocket.getInetAddress().getHostAddress());

                //genera un hilo para el cliente
                pool.execute(new ClientHandler(clientSocket));
            }

        }
        catch (IOException e) {
            System.err.println("Error -> No se pudo iniciar el servidor: " + e.getMessage());
        }

    }

    static void broadcast(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
        System.out.println("Broadcast -> " + message);
    }

    //Se registra el print writer de un cliente al conectarse
    static void addClient(PrintWriter writer) {
        clientWriters.add(writer);
    }

    //Elimina el print writer de un cliente al desconectarse
    static void removeClient(PrintWriter writer) {
        clientWriters.remove(writer);
    }

    //Hilo que gestiona la comunicacion con un cliente en especifico
    //Lee mensajes entrantes
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private String clientName = "Desconocido";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            PrintWriter out = null;

            try {
                //Convierte bytes a caracteres UTF8
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                //cada println envia el mensaje de inmediato
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                //Se registra el cliente en la lista global
                addClient(out);

                //Se recibe un msj inicial, por ejemplo, el nombre del cliente
                out.println("Bienvenido al chat de mensajes ¿Cuál es tu nombre?: ");
                clientName = in.readLine();

                if (clientName == null || clientName.isEmpty()) clientName = "Anónimo";

                broadcast("*** " + clientName + " se ha unido al chat ***");

                //Bucle de lectura de mensajes
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/salir")) { //Mensaje para salir del chat
                        break;
                    }

                    broadcast(clientName + " -> " + message + "\n");
                }
            }
            catch (IOException e) {
                System.err.printf("Error -> Problema con cliente %s: %s%n", clientName, e.getMessage());
            }
            finally {
                //Limpieza
                if (out != null) removeClient(out);
                broadcast("*** " + clientName + " ha abandonado el chat ***");
                try { socket.close(); } catch (IOException ignored) {}
                System.out.printf("Servidor -> Cliente %s desconectado.%n", clientName);
            }
        }
    }

}


