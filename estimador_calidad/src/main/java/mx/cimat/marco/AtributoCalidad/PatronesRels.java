/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mx.cimat.marco.AtributoCalidad;

import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author Marco
 */
public enum PatronesRels implements RelationshipType{
  SEQ,
  AND_JOIN,
  AND_SPLIT,
  OR_JOIN,
  OR_SPLIT,
  XOR_JOIN,
  XOR_SPLIT,
  LOOP
}
