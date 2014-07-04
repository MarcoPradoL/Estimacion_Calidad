/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mx.cimat.marco.catalogoFormulasTest;

import junit.framework.TestCase;
import mx.cimat.marco.catalogoFormulas.Formula;

/**
 *
 * @author Marco
 */

public class FormulaTest extends TestCase{

   Formula  f = new Formula();
   int  id = 0;
   String resultado = "producto ( S ( ) )" ;

public void testLeerFormulas(){
    assertNotNull(f.leerFormulas());
}
   
public void testObtenerOperacion(){
    assertEquals(resultado, f.obtenerOperacion(id));
}

}

