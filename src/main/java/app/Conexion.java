package app;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Conexión que es manejada en un hilo
 *
 * @author Bryan Sibaja
 * @version 2017.09.11
 */
public class Conexion implements Runnable {

	private final Socket socket;

	/**
	 * Constructor
	 *
	 * @param socket El socket de la conexión
	 */
	public Conexion(Socket socket) {
		System.out.println("Nueva conexión");
		this.socket = socket;
	}

	/**
	 * Ejecuta el hilo que atiende la conexión
	 */
	@Override
	public void run() {

		HttpSolicitud solicitud = procesarEntrada();

		if (!solicitud.esCorrecta()) {
			// Error 501 no implementado
			String respuesta = "HTTP/1.1 501 Not Implemented\r\n" + "Content-Length: 28\r\n"
					+ "Content-Type: text/html\r\n\r\n" + "<h1>501 Not Implemented</h1>";
			enviarRespuesta(respuesta);
			return;
		}
		if (!solicitud.getRecurso().equals("recurso existente")) {
			// Error 404 no existe el recurso
			String respuesta = "HTTP/1.1 404 Not Found\r\n" + "Content-Length: 22\r\n" + "Content-Type: text/html\r\n\r\n"
					+ "<h1>501 Not Found</h1>";
			enviarRespuesta(respuesta);
			return;
		}
		if (solicitud.getAccion().equals("POST")) {
			// 200 OK
			String respuesta = "Content-Type: text/html\r\n\r\n" + "HTTP/1.1 200 Ok\r\n" + "Content-Length: 20\r\n"
					+ "<h1>500 Not Ok</h1>";
			enviarRespuesta(respuesta);
			return;
		}
		if (solicitud.getRecurso().equals("mimetype aceptado")) {
			// Error 406 no aceptado
		}
		// 200 oK
		if (solicitud.getAccion().equals("GET")) {
			// mandar recurso
			String response = "HTTP/1.1 200 Ok\r\n" + "Content-Length: 15\r\n" + "Content-Type: text/html\r\n\r\n"
					+ "<h1>500 OK</h1>";
			try {
				socket.getOutputStream().write(response.getBytes());
			} catch (IOException ex) {
				System.err.println("Error al escribir en el socket");
			}
			return;
		}
		// mandar encabezados
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

	private void enviarRespuesta(String respuesta) {
		// generar respuesta?
		try {
			socket.getOutputStream().write(respuesta.getBytes());
		} catch (IOException ex) {
			System.err.println("Error al escribir en el socket");
		}
	}
}
