package xm.soft;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.JFrame;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WServiceInfoProvider extends JFrame {
	private final int API_NAME = 0;
	private final int API_AVAILABILITY = 1;
	private final int API_PERFORMANCE = 2;
	private final int API_STATUS = 3;
	private final JFXPanel mPanelFX = new JFXPanel();
	private WebEngine mEngine;
	private SyncStack<ArrayList<ServiceInfo>> mStack = new SyncStack<>();
	
	/*
	 * Prueba del componente, es importante que los datos esten en la misma 
	 * pagina, no en un iframe de la pagina, esto sucede en el de mercado libre
	 * y el de twitter, es posible solventar este problema llamando recursivamente 
	 * al metodo para que procese la direccion de esos iframe, es por eso que
	 * recomiendo que sea en un proceso en segundo plano en la aplicacion, estas
	 * son las paginas de donde estan realmente los datos:
	 * http://status.wikimedia.org/
	 * http://status.mozilla.com/
	 * https://dev.twitter.com/status => https://status.io.watchmouse.com/7617
	 * http://developers.mercadolibre.com/api-health-view/
	 * http://status.cloudmonitor.ca.com/
	 * http://code.movideo.com/Media_API_Status => http://api.status.movideo.com/
	 */
	public static void main(String args[]){
		WServiceInfoProvider provider = new WServiceInfoProvider();
		provider.setVisible(true); //no es necesario, pero para ver que cargue la pagina es bueno
		String url = "http://status.io.watchmouse.com/7617";
		//Conseguimos la informacion en un plazo a lo mas de 20 segundos,
		//Este parametro debe depender de la velocidad de conexion y de procesamiento.
		ArrayList<ServiceInfo> services = provider.getServices(url, 20000);
		for (ServiceInfo service: services){
			System.out.println(service);
		}
		
		provider.dispose();
	}
	public WServiceInfoProvider() {
		super();
		initComponents();
	}

	private void initComponents() {
		createScene();
		getContentPane().add(mPanelFX);
		setPreferredSize(new Dimension(800, 600));
		setSize(800,600);
	}

	private void createScene() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				WebView view = new WebView();
				mEngine = view.getEngine();

				mPanelFX.setScene(new Scene(view));
			}
		});
	}
	
	private ArrayList<ServiceInfo> getServicesInfo() {

		ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
		Document document = mEngine.getDocument();
		if (document == null){
			return services;
		}
		NodeList tablas = document.getElementsByTagName("table");
		if (tablas == null){
			return services;
		}
		// Recorremos todas las tablas para poder identificar las tablas que
		// tienen la información que necesitamos
		for (int i = 0; i < tablas.getLength(); i++) {
			Node tabla = tablas.item(i);
			// Este mapa contendrá las llaves "name", "performance",
			// "availability" y "status"
			TreeMap<String, Integer> headers = null;
			// Se obtiene el primer renglon de la tabla, es donde determinamos
			// que campos hay
			Node fila = getFirstChildElementNode(tabla);
			if (fila.getNodeName().equalsIgnoreCase("thead")) {
				// Extraemos los encabezados de la primera linea del thead
				Node filaHeader = getFirstChildElementNode(fila);
				headers = extractHeaders(filaHeader);
				// Ahora apuntamos a los datos, que están en el "tbody"
				fila = getNextElementNode(fila);
				fila = getFirstChildElementNode(fila);
			} else {
				// Los encabezados se extraen del "tbody"
				fila = getFirstChildElementNode(fila);
				headers = extractHeaders(fila);
				// Ahora apuntamos a los verdaderos datos, que están en el
				// "tbody"
				fila = getNextElementNode(fila);
			}
			if (headers.size() < 3) {
				continue;
			}
			// Se extraen los valores de las filas
			while (fila != null) {
				int index = 0;
				ServiceInfo service = new ServiceInfo();
				Node columna = getFirstChildElementNode(fila);
				while (columna != null) {
					if (headers.containsKey("name")) {
						int start = headers.get("name");
						int width = headers.get("name_colspan");
						if (index >= start && index < start + width) {
							String name = columna.getTextContent();
							name = trim(name);
							if (name.length() > 0) {
								service.setName(name);
							}
						}
					}
					if (headers.containsKey("performance")
							&& index == headers.get("performance")) {
						double value = parseDouble(columna.getTextContent());

						service.setPerformance((int) value);
					} else if (headers.containsKey("availability")
							&& index == headers.get("availability")) {
						double value = parseDouble(columna.getTextContent());
						service.setAvailability(value);
					} else if (headers.containsKey("status")
							&& index == headers.get("status")) {

					}
					index++;
					columna = getNextElementNode(columna);
				}
				fila = getNextElementNode(fila);
				services.add(service);
			}
			if (headers.size() >= 3) {
				break;
			}
		}
		return services;
	}

	public ArrayList<ServiceInfo> getServices(String strUrl, long delay) {
		final String url = strUrl;
		/*
		 * Cargamos la pagina web y esperamos la cantidad indicada de
		 * milisegundos
		 */
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				mEngine.load(url);
			}
		});
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				mStack.push(getServicesInfo());
			}
		});
		return mStack.pop();
	}

	/**
	 * Metodo usado para buscar content dentro de cada uno de los elementos de
	 * values
	 */
	private int search(String[] values, String content) {
		for (int i = 0; i < values.length; i++) {
			if (content.indexOf(values[i]) >= 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Metodo que extrae los encabezados de una fila, e indica el tipo de
	 * contenido que almacena cada una de las columnas.
	 * 
	 * @param fila
	 *            la primer fila de una tabla que contiene informacion del
	 *            contenido que hay en cada una de las demas filas
	 * @return un TreeMap que contiene el tipo de columna como llaves y como
	 *         valores contiene la posicion de las columnas donde estan ubicadas
	 *         las llaves. Asi pues, si la columna 0 corresponde al tipo
	 *         API_NAME, la vista de esto se puede ver como "name => 0", ademas
	 *         se agregan como llaves la cantidad de columnas que abarca cada
	 *         tipo de servicio. Estas estan dadas por los nombres
	 *         "name_colspan", "availability_colspan" ...
	 */
	private TreeMap<String, Integer> extractHeaders(org.w3c.dom.Node fila) {
		TreeMap<String, Integer> results = new TreeMap<String, Integer>();
		Node columna = getFirstChildElementNode(fila);
		int index = 0;
		while (columna != null) {
			String campo = null;
			int type = getType(columna.getTextContent());
			switch (type) {
			case 0:
				campo = "name";
				break;
			case 1:
				campo = "availability";
				break;
			case 2:
				campo = "performance";
				break;
			case 3:
				campo = "status";
				break;
			default:
				campo = "default";
			}
			results.put(campo, index);
			NamedNodeMap attribs = columna.getAttributes();
			Node span = attribs.getNamedItem("colspan");
			int width = span == null ? 1 : Integer.parseInt(span
					.getTextContent());
			index += width;
			results.put(campo + "_colspan", width);
			columna = getNextElementNode(columna);
		}
		return results;
	}

	/**
	 * Metodo que obtiene el nodo hermano siguiente de otro nodo, el tipo de
	 * nodo será ELEMENT_NODE.
	 * 
	 * @param current
	 *            el nodo del cual se buscará su hermano siguiente del tipo
	 *            ELEMENT_NODE
	 * @return un nodo de tipo ELEMENT_NODE o null
	 */
	private Node getNextElementNode(Node current) {
		do {
			current = current.getNextSibling();
		} while (current != null && current.getNodeType() != Node.ELEMENT_NODE);
		return current;
	}

	/**
	 * Metodo que devuelve el primer nodo hijo de tipo ELEMENT_NODE de un nodo
	 * padre. Esta operacion tiene que ver con el arbol DOM del engine web
	 * 
	 * @param parent
	 *            el nodo padre del que se extraerá el nodo
	 * @return el nodo ELEMENT_NODE que se encuentra al inicio del nodo padre
	 */
	private Node getFirstChildElementNode(Node parent) {
		Node child = parent.getFirstChild();
		while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
			child = getNextElementNode(child);
		}
		return child;
	}

	/**
	 * Metodo que indica el tipo de contenido que guarda una columna, esto en
	 * base al contenido de content.
	 * 
	 * @param content
	 *            el contenido que se analizará
	 * @return un entero que indica el tipo de contenido que la columna content
	 *         almacena
	 */
	private int getType(String content) {
		content = content.toLowerCase();
		String apiName[] = { "api name", "web service", "service", "website",
				"resource" };
		String apiAvailability[] = { "availability", "uptime" };
		String apiPerformance[] = { "current performance", "performance" };
		String apiStatus[] = { "status" };
		if (search(apiName, content) >= 0) {
			return API_NAME;
		}
		if (search(apiAvailability, content) >= 0) {
			return API_AVAILABILITY;
		}
		if (search(apiPerformance, content) >= 0) {
			return API_PERFORMANCE;
		}
		if (search(apiStatus, content) >= 0) {
			return API_STATUS;
		}
		return -1;
	}

	/**
	 * Método encargado de convertir una cadena de texto en un numero double,
	 * este es usado en lugar del metodo habitual puesto que este no arroja
	 * excepciones cuando se encuentra con caracteres no numericos
	 * 
	 * @param text
	 *            la cadena que se convertirá a numero
	 * @return un numero double con los unicos valores validos de text
	 */
	private double parseDouble(String text) {
		double value = 0.0;
		int nDecimals = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '.') {
				nDecimals++;
				;
			} else {
				int digit = text.charAt(i) - '0';
				if (digit >= 0 && digit < 10) {
					if (nDecimals > 0) {
						value += digit / (Math.pow(10, nDecimals));
						nDecimals++;
					} else {
						value *= 10;
						value += digit;
					}
				}
			}
		}
		return value;
	}
	
	/**
	 * Metodo que elimina los espacios en blanco al inicio de una cadena
	 * 
	 * @param content
	 *            es la cadena a la que se le extraerán los espacios
	 * @return un string que contiene los datos sin espacios
	 */
	private String trim(String content) {
		StringBuilder cadena = new StringBuilder();
		for (int i = 0; i < content.length(); i++) {
			if (!Character.isSpaceChar(content.charAt(i))) {
				cadena.append(content.charAt(i));
			}
		}
		return new String(cadena);
	}
	
	
}
