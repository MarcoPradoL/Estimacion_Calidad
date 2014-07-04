/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mx.cimat.marco.AtributoCalidad;

import org.neo4j.graphdb.Node;

/**
 *
 * @author Marco
 */
public class AtributoCalidad{
   
  private Node nodo;
  public AtributoCalidad(Node nodo) {
    this.nodo = nodo;
  }

    public String getNombre() {
        return (String) nodo.getProperty("nombre");
    }

    public void setNombre(String nombre) {
        nodo.setProperty("nombre", nombre);
    }

    public double getValor() {
        return (double) nodo.getProperty("valor");
    }

    public void setValor(double valor) {
        nodo.setProperty("valor", valor);
    }

    public Atributo getAtributo() {
        return (Atributo) nodo.getProperty("atributo");
    }

    public void setAtributo(Atributo atributo) {
         nodo.setProperty("atributo", atributo);
    }

    public int getRepeticiones() {
        return (int) nodo.getProperty("repeticiones");
    }

    public void setRepeticiones(int repeticiones) {
       nodo.setProperty("repeticiones", repeticiones);
    }

    public double getProbabilidad() {
       return (double) nodo.getProperty("probabilidad");
    }

    public void setProbabilidad(double probabilidad) {
         nodo.setProperty("probabilidad", probabilidad);
    }

    public Node getNodo() {
        return nodo;
    }

    public void setNodo(Node nodo) {
        this.nodo = nodo;
    }
    
}
