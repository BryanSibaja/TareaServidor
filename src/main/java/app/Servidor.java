package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servidor miniatura
 *
 * @author Bryan Sibaja
 * @version 2017.09.11
 */
public class Servidor {

  private final int puerto;

  /**
   * Constructor
   *
   * @param puerto parametro de tipo entero con el número de puerto
   */
  public Servidor(int puerto) {
    this.puerto = puerto;
  }

  /**
   * Comienza la ejecución del servidor
   */
  public void iniciar() {
    System.out.println("Iniciando Servidor en el puerto: " + puerto);
    try {
      ServerSocket servidor;
      servidor = new ServerSocket(puerto);
      while (true) {
        Socket socket = servidor.accept();
        new Thread(new Conexion(socket)).start();
      }
    } catch (IOException ex) {
      Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
