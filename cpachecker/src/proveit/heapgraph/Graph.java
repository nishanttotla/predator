/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2015  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package proveit.heapgraph;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.sosy_lab.cpachecker.cfa.ast.c.CSimpleDeclaration;


public class Graph {
  public enum ThreeVal {FALSE, MAYBE, TRUE}

  public Set<Node> nodes;
  public HashMap<LabeledEdge, ThreeVal> edges; // E: N x Fields x N -> B3
  public HeapVarLabeling heapVarLabeling; // V: N x Vars_h -> B3

  public Graph(){
    this.edges = new HashMap<>();
    this.nodes = new TreeSet<>();
    this.heapVarLabeling = new HeapVarLabeling();
  }

  public static Graph universalHeap(){
    return new Graph();
  }

  public static Graph emptyHeap(){
    return new Graph();
  }


  public Graph(Graph other){
    this();

    //TODO: should use entryset here
    for (LabeledEdge edge : other.edges.keySet()){
      this.edges.put(edge, other.edges.get(edge));
    }
    this.nodes.addAll(other.nodes);
  }

  public Set<Node> succNodes(Node aN) {
    Set<Node> result = new TreeSet<>();

    for (LabeledEdge e : edges.keySet()){
      if (e.src == aN){
        result.add(e.dst);
      }
    }
    return result;
  }

  public Set<Node> roots(){
    Set<Node> result = new TreeSet<>();
    for (Node node : nodes){
      if (node.root){
        result.add(node);
      }
    }
    return result;
  }

  public void allocNode(HeapVar vIn, Node nIn) {
    for (Node n : nodes){
      heapVarLabeling.update(n, vIn, ThreeVal.FALSE);
    }
    heapVarLabeling.update(nIn, vIn, ThreeVal.TRUE);
  }

  public void copyNode(HeapVar vDst, HeapVar vSrc){
    //No new nodes
    //No new edges
    heapVarLabeling.copy(vDst, vSrc);
  }

  public void load(HeapVar vDst, HeapVar vSrc, String field){
    Set<Node> srcAliases = heapVarLabeling.nodes(vSrc);
    boolean singleton = true;
    Node singletonNode = null;
    for (Node srcNode : srcAliases) {
      for (LabeledEdge edge : edges.keySet()){
        if (edge.src.equals(srcNode) && edge.field.equals(field)){
          ThreeVal edgeVal = edges.get(edge);
          if (edgeVal == ThreeVal.MAYBE || edgeVal == ThreeVal.TRUE){
            heapVarLabeling.update(srcNode, vDst, ThreeVal.MAYBE);
            if (singletonNode == null){
              singletonNode = srcNode;
            } else {
              singleton = false;
            }
          }
        }
      }
    }
    if (singletonNode != null && singleton == true){
      heapVarLabeling.update(singletonNode, vDst, ThreeVal.TRUE);
    }
  }

  public void store(HeapVar vDst, HeapVar vSrc, String field){
    Set<Node> srcAliases = heapVarLabeling.nodes(vSrc);
    for (Node srcNode : srcAliases){
      for (LabeledEdge edge : edges.keySet()){
        if (edge.src.equals(srcNode) && edge.field.equals(field)){
          ThreeVal edgeVal = edges.get(edge);
          if (edgeVal == ThreeVal.MAYBE || edgeVal == ThreeVal.TRUE){
            heapVarLabeling.update(srcNode, vDst, ThreeVal.MAYBE);
          }
        }
      }
    }
  }

  public HeapVar findOrMakeVar(CSimpleDeclaration pDst) {
    return heapVarLabeling.findOrMakeVar(pDst);
  }
}
