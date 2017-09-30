package app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

/**
 * Conexión que es manejada en un hilo
 *
 * @author Bryan Sibaja
 * @version 2017.09.11
 */
public class Conexion implements Runnable {

  private final Socket socket;
  private final String directorio;
  private final String ERROR_404 = "404 Not Found";
  private final String ERROR_406 = "406 Not acceptable";
  private final String ERROR_501 = "501 Not Implemented";
  private final String HTML = "text/html";
  private final String OK = "200 OK";

  /**
   * Constructor
   *
   * @param socket El socket de la conexión
   * @param directorio Directorio para acceder recursos
   */
  public Conexion(Socket socket, String directorio) {
    System.out.println("Nueva conexión");
    this.socket = socket;
    this.directorio = directorio;
  }

  /**
   * Ejecuta el hilo que atiende la conexión
   */
  @Override
  public void run() {
    HttpSolicitud solicitud = procesarEntrada();
    String contenido = new String();
    String tipo = new String();

    if (!solicitud.esCorrecta()) {
      contenido = "<h1>501 Not Implemented</h1>";
      String respuesta = generarEncabezado(contenido.length(), HTML, ERROR_501);
      enviar(respuesta.getBytes());
      enviar(contenido.getBytes());
      return;
    }

    byte[] datos = null;
    if(solicitud.getRecurso().equals("/")){
      datos = "<h1>Miniservidor de Bryan<h1>".getBytes();
    } else {
      Path archivo = Paths.get(directorio + solicitud.getRecurso());
      try {
        datos = Files.readAllBytes(archivo);
        tipo = Files.probeContentType(archivo);
      } catch (IOException ex) {
        String dato = "<h1>501 Not Found</h1>";
        String respuesta = generarEncabezado(dato.length(), HTML, ERROR_404);
        enviar(respuesta.getBytes());
        enviar(dato.getBytes());
        return;
      }
    }

    if (!(solicitud.getEncabezado("Accept").equals("*/*") || solicitud.getEncabezado("Accept").equals(tipo))) {
      contenido = "<h1>406 Not acceptable</h1>";
      String respuesta = generarEncabezado(contenido.length(), HTML, ERROR_406);
      enviar(respuesta.getBytes());
      enviar(contenido.getBytes());
      return;
    }

    if (solicitud.getAccion().equals("GET") || solicitud.getAccion().equals("POST")) {
      String respuesta = generarEncabezado(datos.length, tipo, OK);
      enviar(respuesta.getBytes());
      enviar(datos);
    }

    if (solicitud.getAccion().equals("HEAD")) {
      String respuesta = generarEncabezado(datos.length, tipo, OK);
      enviar(respuesta.getBytes());
      enviar(contenido.getBytes());
    }

    String entrada = solicitud.getAccion() + "\t" + solicitud.getEncabezado("Host") + "\t" + solicitud.getRecurso();
    try (PrintWriter out = new PrintWriter(new FileOutputStream("bitacora.txt", true))) {
      out.println(entrada);
    } catch (IOException ex) {
      System.err.println("Error al escrivir Bitacora");
    }
    try {
      socket.close();
    } catch (IOException ex) {
      System.err.println("Error de cerrado");
    }
  }

  private HttpSolicitud procesarEntrada() {
    StringBuilder buffer = new StringBuilder();
    StringBuilder line = new StringBuilder();
    try {
      InputStream in = socket.getInputStream();
      while (!line.toString().equals("\r\n")) {
        char dato;
        buffer.append(line);
        line.setLength(0);
        do {
          dato = (char) in.read();
          line.append(dato);
        } while (dato != '\n');
      }
    } catch (IOException ex) {
      System.err.println("Error al leer solicitud");
    }
    return new HttpSolicitud(buffer.toString());
  }

  private String generarEncabezado(int len, String tipo, String estado) {
    String response = "HTTP/1.1 " + estado + "\r\n" + "Date: " + LocalDate.now().toString() + "\r\n"
        + "Content-Length: " + len + "\r\n" + "Content-Type: " + tipo + "\r\n\r\n";
    return response;
  }

  private void enviar(byte[] respuesta) {
    try {
      socket.getOutputStream().write(respuesta);
    } catch (IOException ex) {
      System.err.println("Error al escribir en el socket");
    }
  }
}