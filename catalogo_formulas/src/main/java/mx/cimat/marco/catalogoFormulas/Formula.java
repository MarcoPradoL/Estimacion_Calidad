

package mx.cimat.marco.catalogoFormulas;

import java.io.File;
import java.io.IOException;         // |
import java.util.ArrayList;          // |\ Librerías
import java.util.Iterator;
import java.util.List;    // |/ JDOM
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import java.util.StringTokenizer;

/**
 *
 * @author Marco
 */
public class Formula {
    private int id;
    private String operacion;
    
    public ArrayList<Formula>  leerFormulas(){
        ArrayList<Formula> formulas = new ArrayList();
        //Se crea un SAXBuilder para poder parsear el archivo
        Formula f;
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File( "src/main/resources/formulas.xml" );
        try
        {
            //Se crea el documento a traves del archivo
            Document document = (Document) builder.build( xmlFile );

            //Se obtiene la raiz 'tables'
            Element rootNode = document.getRootElement();

            //Se obtiene la lista de hijos de la raiz 'tables'
            List list = rootNode.getChildren();

            //Se recorre la lista de hijos de 'tables'
            for ( int i = 0; i < list.size(); i++ ){
                f = new Formula();
                //Se obtiene el elemento 'tabla'
                Element formula = (Element) list.get(i);

                f.setId(Integer.parseInt(formula.getChildTextTrim("id")));
                f.setOperacion(formula.getChildTextTrim("operacion"));
                formulas.add(f);
                
            }
        }catch ( IOException io ) {
            System.out.println( io.getMessage() );
        }catch ( JDOMException jdomex ) {
            System.out.println( jdomex.getMessage() );
        }
        
        return formulas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }
    public String obtenerOperacion(int id){
        ArrayList<Formula> formulas;
        formulas = leerFormulas();
        return formulas.get(id).getOperacion();
    }

    
}
