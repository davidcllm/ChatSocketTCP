import java.io.*;
import java.net.*;

/*
Cliente de chat TCP

Funcionamiento:
-Se conecta a localhost:5000 (el servidor).
-El hilo principal lee el taclado y envia el msj al servidor.
-EL hilo secundario MessageListener escucha otros mensajes del servidor y los imprime.

 */

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("║         CLIENTE DE CHAT TCP          ║");
        System.out.printf ("║  Conectando a %s:%-5d          ║%n", HOST, PORT);

        //Cierra el socket y la transimision de mensajes
        try (
            Socket socket  = new Socket(HOST, PORT);

            //Transmision de salida hacia el server
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            //Transmision de entrada desde el server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            //Transmision que lee la entrada del teclado
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        ) {
            System.out.println("Info -> Conexión exitosa al servidor. \n");

            //Hilo auxiliar/secundario
            //Daemon sirve para no bloquear la salida del programa
            Thread listener = new Thread(new MessageListener(in));
            listener.setDaemon(true);
            listener.start();

            //Hilo principal
            //Bucle
            String userInput;
            while ((userInput = teclado.readLine()) != null) {
                out.println(userInput); //Envia el msj al servidor

                if (userInput.equalsIgnoreCase("/salir")) {
                    System.out.println("Info -> Desconectando...");
                    break;
                }
            }


        }
        catch (ConnectException e) {
            System.err.println("Error -> No se pudo conectar al servidor. Asegúrese que esté en ejecución.");
        }
        catch (IOException e) {
            System.err.println("Error -> Problema de comunicación: " + e.getMessage());
        }

        System.out.println("Info -> Cliente terminado.");
    }

    static class MessageListener implements Runnable {
        private final BufferedReader serverIn;

        MessageListener(BufferedReader serverIn) {
            this.serverIn = serverIn;
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                // readLine() bloquea hasta que llega una línea o el servidor cierra
                while ((serverMessage = serverIn.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                // Ocurre cuando el socket se cierra (desconexión normal o error)
                System.out.println("\nInfo -> Conexión con el servidor cerrada.");
            }
        }
    }

}
