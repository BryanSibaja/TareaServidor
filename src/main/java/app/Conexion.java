package app;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.Socket;

/**
 * Conexión que es manejada en un hilo
 *
 * @author Bryan Sibaja
 * @version 2017.09.11
 */
public class Conexion implements Runnable {

  private final Socket socket;
  private final String directorio;

  /**
   * Constructor
   *
   * @param socket El socket de la conexión
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

	if (!solicitud.esCorrecta()) {
	  contenido = "<h1>501 Not Implemented</h1>";
	  String respuesta = generarRespuesta(contenido, "text/html",
			  "501 Not Implemented");
	  enviarRespuesta(respuesta);
	  return;
	}
	
	if (solicitud.getRecurso().equals("mimetype aceptado")) {
	  // Error 406 no aceptado
	}
	
	if (solicitud.getAccion().equals("GET") || 
			solicitud.getAccion().equals("POST")) {
	  contenido = obtenerRecurso(solicitud.getRecurso());
	  String respuesta = generarRespuesta(contenido, "text/html", "200 Ok");
	  enviarRespuesta(respuesta);
	}
  }

  private String obtenerRecurso(String recurso) {
	StringBuilder contenido = new StringBuilder();
	try (Reader in = new FileReader(directorio + recurso)) {
	  int r;
	  while ((r = in.read()) != -1) {
		contenido.append((char)r);
	  }
	} catch (IOException ex) {
	  String dato = "<h1>501 Not Found</h1>";
	  String respuesta = generarRespuesta(dato, "text/html", "404 Not Found");
	  enviarRespuesta(respuesta);
	}
	return contenido.toString();
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

  private String generarRespuesta(String contenido, String tipo, String estado) {
	String response = "HTTP/1.1 " + estado + "\r\n"
			+ "Content-Length: " + contenido.length() + "\r\n"
			+ "Content-Type: " + tipo + "\r\n\r\n"
			+ contenido;
	return response;
  }

  private void enviarRespuesta(String respuesta) {
	// generar respuesta?
	try {
	  socket.getOutputStream().write(respuesta.getBytes());
	} catch (IOException ex) {
	  System.err.println("Error al escribir en el socket");
	}
  }
}
