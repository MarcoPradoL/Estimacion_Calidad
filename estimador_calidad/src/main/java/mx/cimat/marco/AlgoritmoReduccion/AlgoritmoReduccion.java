/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mx.cimat.marco.AlgoritmoReduccion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import mx.cimat.marco.AtributoCalidad.AtributoCalidad;
import mx.cimat.marco.AtributoCalidad.Atributo;
import mx.cimat.marco.AtributoCalidad.PatronesRels;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
/**
 *
 * @author Marco
 */
public class AlgoritmoReduccion {
    private ArrayList<AtributoCalidad> atributos;

    public AlgoritmoReduccion(ArrayList<AtributoCalidad> atributos) {
        this.atributos = atributos;
    }
    
    
    
    public List<AtributoCalidad> reducirGrafo (ArrayList<AtributoCalidad> atributos){
        Integer idServicio;
        List<Integer> servicios;
        idServicio = 0;
        AtributoCalidad root= atributos.get(0);
        ArrayList<Relationship> relaciones; 
        while (atributos.size() > 1) {
            
            relaciones = (ArrayList<Relationship>) root.getNodo().getRelationships(Direction.OUTGOING);
            if (relaciones.size() == 1){
                if (root.getNodo().hasRelationship(PatronesRels.SEQ)) {
                    atributos.remove(root);
                    root = patronSecuencial(root);
                    
                }
                if (root.getNodo().hasRelationship(PatronesRels.LOOP) 
                        && (root.getNodo().getSingleRelationship(PatronesRels.LOOP, Direction.OUTGOING)).getEndNode().equals(root) ) {
                    root = patronLoop(root);
                }
            }
            else{
                if (root.getNodo().hasRelationship(PatronesRels.AND_SPLIT)){
                    patronParalelo(root);
                }
                if (root.getNodo().hasRelationship(PatronesRels.OR_SPLIT)){
                    patronSelectivo(root);
                }
            }
        }
    
    return atributos;
    }
    
    public List<Integer> obtenerServicios (Integer[][] matriz, Integer idServicio){
        List<Integer> servicios;
        
        servicios = new ArrayList();
        
        for (int i = 0; i < matriz.length; i++) {
            if ( matriz[idServicio][i] != 0 ){
                servicios.add(i);
            }
        }
  
        return servicios;
    }
    public List<Integer> obtenerDependencias (Integer[][] matriz, Integer idServicio){
        List<Integer> dependencias;
        
        dependencias = new ArrayList();
        
        for (int i = 0; i < matriz.length; i++) {
            if ( matriz[i][idServicio] != 0 ){
                dependencias.add(i);
            }
        }
  
        return dependencias;
    }
    

    public AtributoCalidad patronSecuencial(AtributoCalidad root){
        AtributoCalidad siguiente =new AtributoCalidad(root.getNodo().
                getSingleRelationship(PatronesRels.SEQ, Direction.OUTGOING).getEndNode());
        //aqui debera mandar llamar la operacion secuencial desde el repocitorio de formulas.
        double resultado;
        root.getNodo().getSingleRelationship(PatronesRels.SEQ, Direction.OUTGOING).delete();
        root.getNodo().delete();
        return siguiente;
    }
    
    public Integer[][] reducirMatriz (Integer[][] matriz, Integer idServicio, int tipo){
        boolean bandera= false;
        List<Integer> servicio;
        List<Integer> dependencia;
        Integer [][] original = matriz;
        Integer [][] modificadaC= null;
        Integer [][] modificadaF= null; 
        if (tipo == 1){
            //eliminar columna
             modificadaC = new Integer [original.length][original.length-1];
            modificadaF = new Integer [original.length-1][original.length-1];
            for (int i = 0; i < modificadaC.length; i++) {
                for (int j = 0; j < modificadaC[i].length; j++) {
                    if (j!=idServicio){
                        if (bandera)
                            modificadaC[i][j]= original[i][j+1];
                        else
                            modificadaC[i][j]= original[i][j];
                    }
                    else{
                        bandera = true;
                        modificadaC[i][j]= original[i][j+1];
                    }
                }
                bandera = false;
            }
          
            //eliminar fila 
            for(int i = 0; i < idServicio; i++)
                modificadaF[i] = modificadaC[i];
            for(int i = idServicio; i < modificadaF.length; i++)
                modificadaF[i] = modificadaC[i+1];
            // asignar nueva dependencia con AND o OR
            servicio = obtenerServicios(original, idServicio);
            dependencia = obtenerDependencias(original, idServicio);
            
            if(!dependencia.isEmpty()){
                if (original[idServicio][servicio.get(0)] !=2 && original[idServicio][servicio.get(0)]!= 3)
                modificadaF[dependencia.get(0)][idServicio] = original[dependencia.get(0)][idServicio];
            }
            
            
        }
        
        if (tipo == 2) {
            servicio = obtenerServicios(matriz, idServicio);
            int idServicioParalalo = servicio.get(0);
            modificadaC = new Integer [original.length][original.length-1];
            modificadaF = new Integer [original.length-1][original.length-1];
            for (int i = 0; i < modificadaC.length; i++) {
                for (int j = 0; j < modificadaC[i].length; j++) {
                    if (j!=idServicioParalalo ){
                        if (bandera)
                            modificadaC[i][j]= original[i][j+1];
                        else
                            modificadaC[i][j]= original[i][j];
                    }
                    else{
                        bandera = true;
                        modificadaC[i][j]= original[i][j+1];
                    }
                }
                bandera = false;
            }
          
            //eliminar fila 
            for(int i = 0; i < idServicioParalalo; i++)
                modificadaF[i] = modificadaC[i];
            for(int i = idServicioParalalo; i < modificadaF.length; i++)
                modificadaF[i] = modificadaC[i+1];
            
             modificadaF[idServicio][servicio.get(0)] = 1;
        }
        
        return modificadaF;
    }

    private void patronParalelo(AtributoCalidad root) {
        ArrayList<AtributoCalidad>dependencias;
        
        dependencias = new ArrayList<>();
        ArrayList<Relationship> relaciones; 
        ArrayList<Relationship> relacionesInt; 
        relaciones = (ArrayList<Relationship>) root.getNodo().getRelationships(Direction.OUTGOING);
        for (Iterator<Relationship> it = relaciones.iterator(); it.hasNext();) {
            Relationship relationship = it.next();
            AtributoCalidad at = new AtributoCalidad(relationship.getEndNode());
            relacionesInt = (ArrayList<Relationship>) at.getNodo().getRelationships(Direction.OUTGOING);
            if (relaciones.size() == 1){
                if (at.getNodo().hasRelationship(PatronesRels.SEQ)) {
                    at = patronSecuencial(at);
                }
                if (at.getNodo().hasRelationship(PatronesRels.LOOP) 
                        && (at.getNodo().getSingleRelationship(PatronesRels.LOOP, Direction.OUTGOING)).getEndNode().equals(at) ) {
                    at = patronLoop(at);
                }
                if (at.getNodo().hasRelationship(PatronesRels.AND_JOIN) ) {
                    dependencias.add(at);
                }
            }
            else{
                if (at.getNodo().hasRelationship(PatronesRels.AND_SPLIT)){
                    patronParalelo(at);
                }
                if (at.getNodo().hasRelationship(PatronesRels.OR_SPLIT)){
                    patronSelectivo(at);
                }
            }
            
        }
               
        double resultado;
    }

    private void patronSelectivo(AtributoCalidad root) {
        ArrayList<AtributoCalidad>dependencias;
        
        dependencias = new ArrayList<>();
        ArrayList<Relationship> relaciones; 
        ArrayList<Relationship> relacionesInt; 
        relaciones = (ArrayList<Relationship>) root.getNodo().getRelationships(Direction.OUTGOING);
        for (Iterator<Relationship> it = relaciones.iterator(); it.hasNext();) {
            Relationship relationship = it.next();
            AtributoCalidad at = new AtributoCalidad(relationship.getEndNode());
            relacionesInt = (ArrayList<Relationship>) at.getNodo().getRelationships(Direction.OUTGOING);
            if (relaciones.size() == 1){
                if (at.getNodo().hasRelationship(PatronesRels.SEQ)) {
                    at = patronSecuencial(at);
                }
                if (at.getNodo().hasRelationship(PatronesRels.LOOP) 
                        && (at.getNodo().getSingleRelationship(PatronesRels.LOOP, Direction.OUTGOING)).getEndNode().equals(at) ) {
                    at = patronLoop(at);
                }
                if (at.getNodo().hasRelationship(PatronesRels.AND_JOIN) ) {
                    dependencias.add(at);
                }
            }
            else{
                if (at.getNodo().hasRelationship(PatronesRels.AND_SPLIT)){
                    patronParalelo(at);
                }
                if (at.getNodo().hasRelationship(PatronesRels.OR_SPLIT)){
                    patronSelectivo(at);
                }
            }
            
        }
        double resultado;
    }

    private AtributoCalidad patronLoop(AtributoCalidad root) {
        //aqui debera mandar llamar la operacion secuencial desde el repocitorio de formulas.
        double resultado;
        root.getNodo().getSingleRelationship(PatronesRels.LOOP, Direction.OUTGOING).delete();
        return root;
    }
            
}

   