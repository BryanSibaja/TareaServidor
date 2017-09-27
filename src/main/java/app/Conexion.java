package app;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
			String respuesta = generarEncabezado(contenido.length(), "text/html", "501 Not Implemented");
			enviar(respuesta.getBytes());
			enviar(contenido.getBytes());
			return;
		}

		Path archivo = Paths.get(directorio + solicitud.getRecurso());
		byte[] datos = null;
		try {
			datos = Files.readAllBytes(archivo);
		} catch (IOException ex) {
			String dato = "<h1>501 Not Found</h1>";
			String respuesta = generarEncabezado(dato.length(), "text/html", "404 Not Found");
			enviar(respuesta.getBytes());
			enviar(dato.getBytes());
			return;
		}

		if (solicitud.getRecurso().equals("mimetype aceptado")) {
			// Error 406 no aceptado
		}

		if (solicitud.getAccion().equals("GET") || solicitud.getAccion().equals("POST")) {
			String respuesta = generarEncabezado(datos.length, tipo, "200 Ok");
			enviar(respuesta.getBytes());
			enviar(datos);
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
		String response = "HTTP/1.1 " + estado + "\r\n" + "Content-Length: " + len + "\r\n" + "Content-Type: " + tipo
				+ "\r\n\r\n";
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
