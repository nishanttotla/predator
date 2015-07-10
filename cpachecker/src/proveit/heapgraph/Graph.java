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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.sosy_lab.cpachecker.cfa.ast.c.CSimpleDeclaration;
import org.sosy_lab.cpachecker.util.predicates.interfaces.view.BooleanFormulaManagerView;


public class Graph {
  public enum ThreeVal {FALSE, MAYBE, TRUE}

  private HashMap<String, Node> nodeById;

  public Set<Node> nodes;
  public HashMap<LabeledEdge, ThreeVal> edges; // E: N x Fields x N -> B3
  public HeapVarLabeling heapVarLabeling; // V: N x Vars_h -> B3

  public Graph(){
    this.edges = new HashMap<>();
    this.nodes = new TreeSet<>();
    this.heapVarLabeling = new HeapVarLabeling();
  }

  public static Graph fromDot(BooleanFormulaManagerView bfmgr, String filename){
    Graph result = new Graph();
    result.nodeById = new HashMap<>();

    try{
    Scanner s = new Scanner(new File(filename));

    //Discard header
    s.nextLine();
    s.nextLine();
    s.nextLine();

    while(s.hasNextLine()){
      String line = s.nextLine().trim();
      //Directives
      if (line.indexOf("label") == 0){

      } else if (line.indexOf("clusterrank") == 0){
      } else if (line.indexOf("labelloc") == 0){
      } else if (line.indexOf("}") == 0){
      }
      //Nodes and Edges
      else {
        String[] pieces = line.split("[");
        String header = pieces[0];
        if (header.indexOf("->") >= 0){
          result.dotEdge(bfmgr, line);
        } else {
          result.dotNode(bfmgr, line);
        }
      }
    }

    //TODO
    } catch (Exception e){
      e.printStackTrace(System.err);
    }
    return result;
  }

  private void dotEdge(BooleanFormulaManagerView bfmgr, String text){

    String annotation;
    String head;
    {
    head = text.substring(0, text.indexOf("["));
    String annotationChunk = text.substring(text.indexOf("[") + 1);
    int annotationEndIdx = annotationChunk.indexOf("]");
    annotation = annotationChunk.substring(0, annotationEndIdx);
    annotation = annotation.trim();
    }

    String[] headPieces = head.split("->");
    String srcId = headPieces[0].replaceAll("\"", "").trim();
    String dstId = headPieces[0].replaceAll("\"", "").trim();

    String predicate;
    {
    String MARKER = "predicate=";
    int idxStart = annotation.indexOf(MARKER);
    int idxEnd = annotation.indexOf(" ", idxStart);
    predicate = annotation.substring(idxStart, idxEnd);
    }

    ThreeVal val;
    {
    String MARKER = "truth=";
    int idxStart = annotation.indexOf(MARKER);
    int idxEnd = annotation.indexOf(" ", idxStart);
    String truthVal = annotation.substring(idxStart, idxEnd);
    if (truthVal.equals("TRUE")){
      val = ThreeVal.TRUE;
    } else if (truthVal.equals("FALSE")){
      val = ThreeVal.FALSE;
    } else {
      val = ThreeVal.MAYBE;
    }
    }



    Node src = this.nodeById.get(srcId);
    Node dst = this.nodeById.get(dstId);
    Edge edge = new Edge(src, dst, val);

  }

  private void dotNode(BooleanFormulaManagerView bfmgr, String text){
    String[] pieces = text.split("\"");
    String nodeName = pieces[1];



    String annotation;
    {
    String annotationChunk = text.substring(text.indexOf("[") + 1);
    int annotationEndIdx = annotationChunk.indexOf("]");
    annotation = annotationChunk.substring(0, annotationEndIdx);
    annotation = annotation.trim();
    }

    String MARKER = "predicate=";
    String predicate;
    {
    int idxStart = annotation.indexOf(MARKER);
    int idxEnd = annotation.indexOf(" ", idxStart);
    predicate = annotation.substring(idxStart, idxEnd);
    }

    Node node = new Node(bfmgr.makeVariable(predicate));
    this.nodes.add(node);
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

  public Set<LabeledEdge> outgoingEdges(Node nn){
    Set<LabeledEdge> result = new HashSet<>();
    for (Entry<LabeledEdge, ThreeVal> entry : this.edges.entrySet()){
      LabeledEdge le = entry.getKey();
      if (le.src == nn) {
        result.add(le);
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

  public LinkedList<Node> getRoots() {
    LinkedList<Node> markedRoots = new LinkedList<Node>();
    LinkedList<Node> unmarkedRoots = new LinkedList<Node>();
    for (Node node : this.nodes){
      if (node.root){
        markedRoots.add(node);
      }
    }
    if (markedRoots.isEmpty()){
      return markedRoots;
    }

    for (Node node : this.nodes){
      boolean hasIncoming = false;
      for (Entry<LabeledEdge, ThreeVal> edge : this.edges.entrySet()){
        LabeledEdge key = edge.getKey();
        ThreeVal val = edge.getValue();
        if (val != ThreeVal.FALSE){
          if(key.dst.equals(node)){
            hasIncoming = true;
          }
        }
      }
      if (!hasIncoming){
        unmarkedRoots.add(node);
      }
    }
    return unmarkedRoots;
  }
}
