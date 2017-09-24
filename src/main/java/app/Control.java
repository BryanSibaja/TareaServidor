package app;

/**
 * Main class
 *
 * @author Bryan Sibaja
 * @version 2017.09.11
 */
public class Control {

  /**
   * @param args argumentos de linea de comando
   */
  public static void main(String[] args) {
    Servidor servidor = new Servidor(4567);
    servidor.iniciar();
  }
}
