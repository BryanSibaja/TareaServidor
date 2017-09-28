package app;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Encapsula una solicitud HTTP
 *
 * @author bryan
 */
public class HttpSolicitud {

  private String accion, recurso, version;
  private final Map<String, String> encabezados;
  private final boolean correcta;

  /**
   * Contruye una solicitud usando una cadena de entrada
   *
   * @param solicitud
   */
  public HttpSolicitud(String solicitud) {
    encabezados = new HashMap<>();
    correcta = parse(solicitud);
  }

  private boolean parse(String solicitud) {
    Deque<String> lineas;
    lineas = new ArrayDeque<>(Arrays.asList(solicitud.split("\r\n")));
    String primera = lineas.poll();
    //Pattern regex = Pattern.compile("(GET|HEAD|POST)\\s/(\\w+/)*(\\w+" + "\\.\\w+)?\\s(HTTP)/\\d\\.\\d");
    Pattern regex = Pattern.compile("(GET|HEAD|POST)\\s/(\\w+/)*(.+" + "\\.\\w+)?\\s(HTTP)/\\d\\.\\d");
    if (!regex.matcher(primera).matches()) {
      return false;
    }
    String[] result = primera.split(" ");
    regex = Pattern.compile("[\\w-]+:\\s.+");
    String linea;
    while ((linea = lineas.poll()) != null) {
      if (!regex.matcher(linea).matches()) {
        return false;
      }
      String[] headers = linea.split(": ");
      encabezados.put(headers[0], headers[1]);
    }
    accion = result[0];
    recurso = result[1];
    version = result[2];
    return true;
  }

  /**
   * Representación en texto de una solicitud
   *
   * @return
   */
  @Override
  public String toString() {
    String resultado = "Accion " + accion + "\n" + "Recurso " + recurso + "\n" + "Version " + version + "\n"
        + encabezados.toString();
    return resultado;
  }

  public String getEncabezado(String encabezado) {
    return encabezados.get(encabezado);
  }

  public String getAccion() {
    return accion;
  }

  public String getRecurso() {
    return recurso;
  }

  public String getVersion() {
    return version;
  }

  /**
   * Retorna falso si la solicitud es invalida error en el parse
   *
   * @return el estado del parse
   */
  public boolean esCorrecta() {
    return correcta;
  }
}