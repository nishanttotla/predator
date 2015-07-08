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

import proveit.heapgraph.Graph.ThreeVal;


public class HeapVarLabeling {
  private class HVEdge {
    public Node node;
    public HeapVar var;

    public HVEdge(Node node, HeapVar var){
      this.node = node;
      this.var = var;
    }

    @Override
    public boolean equals(Object o){
      HVEdge other = (HVEdge)o;
      if (other.node.equals(node) && other.var.equals(var)) {
        return true;
      }
      return false;
    }
  }

  private HashMap<HVEdge, Graph.ThreeVal> labels;

  public HeapVarLabeling(){
    labels = new HashMap<>();
  }

  public void update(Node node, HeapVar var, Graph.ThreeVal val){
    HVEdge edge = new HVEdge(node, var);
    labels.put(edge, val);
  }

  public void copy(HeapVar dst, HeapVar src){
    Set<HVEdge> edges = labels.keySet();
    for (HVEdge e : edges){
      if (e.var.equals(src)){
        Node srcNode = e.node;
        Graph.ThreeVal srcVal = labels.get(e);
        update(srcNode, dst, srcVal);
      }
    }
  }

  public Set<Node> nodes(HeapVar src) {
    //Gather the set S
    Set<Node> result = new TreeSet<Node>();
    Set<HVEdge> edges = labels.keySet();
    for (HVEdge e : edges){
      if (e.var.equals(src)){
        if (labels.get(e) != ThreeVal.FALSE){
           //Add to S
          result.add(e.node);
        }
      }
    }
    return result;
  }

  public HeapVar findOrMakeVar(CSimpleDeclaration pDst) {
    Set<HVEdge> edges = labels.keySet();
    for (HVEdge e : edges){
      if (e.var.decl.equals(pDst)){
        return e.var;
      }
    }

    HeapVar newVar = new HeapVar(pDst);

    for (HVEdge e : edges){
      HVEdge newEdge = new HVEdge(e.node, newVar);
      labels.put(newEdge, ThreeVal.FALSE);
    }

    return newVar;
  }
}
