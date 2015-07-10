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
package org.sosy_lab.cpachecker.core.algorithm.impact;

import static org.sosy_lab.cpachecker.util.CFAUtils.leavingEdges;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.predicates.interfaces.BooleanFormula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.view.BooleanFormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.interpolation.CounterexampleTraceInfo;
import org.sosy_lab.cpachecker.util.predicates.interpolation.InterpolationManager;

import proveit.heapgraph.Graph;
import proveit.heapgraph.SeparatorChecker;


public class ProveIt {
  private HeapOracle oracle;
  private HeapTransfer heapTransfer;
  private BooleanFormulaManagerView bfmgr;
  private InterpolationManager itpmgr;
  private SeparatorChecker user;

  public ProveIt(BooleanFormulaManagerView bfmgr, InterpolationManager itpmgr){
    oracle = new HeapOracle(bfmgr, ".");
    heapTransfer = new HeapTransfer();
    this.bfmgr = bfmgr;
    this.itpmgr = itpmgr;
    this.user = new SeparatorChecker(oracle);
  }

  //Roughly corresponds to RefineTree
  public CounterexampleTraceInfo buildCounterexampleTrace(List<Vertex> path, List<BooleanFormula> pathFormulas) {

    for (int i = 0 ; i < path.size() ; i++){
      Vertex v = path.get(i);
      BooleanFormula form = pathFormulas.get(i);
      System.out.printf("[ProveIt] #%d: %s %s\n", i, v.getId(), form);
    }

    LinkedList<Vertex>worklist = new LinkedList<>();
    HashMap<Vertex, Graph> nodeGraph = new HashMap<>();

    worklist.add(path.get(0));
    while(!worklist.isEmpty()){
      Vertex v = worklist.pollFirst();
      System.out.println("[ProveIt] worklist v " + v);

      Graph patternR = null;
      if (!v.hasParent()){
        //We're at the root of the tree, use the universal heap
        patternR = Graph.universalHeap();
      } else {
        Vertex parent = v.getParent();
        Graph parentHeap = nodeGraph.get(parent);
        CFAEdge edge = edge(parent, v);
        patternR = heapTransfer.post(edge, parent, parentHeap);
      }

      Graph separator = query(path, patternR);
      if (separator == null){ System.out.println("[ProveIt] no separator"); return null; }
      else{
        nodeGraph.put(v, separator);

        for (Vertex c : v.getChildren()){
          worklist.addLast(c);
        }
      }
    }
    return null;
  }

  private CFAEdge edge(Vertex v, Vertex c){
    CFANode locSrc = AbstractStates.extractLocation(v);
    CFANode locDst = AbstractStates.extractLocation(c);
    for (CFAEdge edge : leavingEdges(locSrc)) { // Typically, this will only iterate once
      if (locDst.equals(edge.getSuccessor())){
        return edge;
      }
    }
    assert(false);
    return null;
  }

  private Graph query(List<Vertex> path, Graph patternR){
    Set<Graph> hPlus = new TreeSet<>();
    Set<Graph> hMinus = new TreeSet<>();

    {
      Set<Graph> intersection = new HashSet<>(hPlus);
      intersection.retainAll(hMinus);
      if (!intersection.isEmpty()){ return null; }
    }

    while (true){
      Graph separator = oracle.separator(hPlus, hMinus);
      if (separator == null){ return null; }
      Graph r = user.findReach(patternR, separator);
      Graph e = user.findErr(path, separator);
      if (r != null){
        hPlus.add(r);
        continue;
      } else if (e != null){
        hMinus.add(e);
        continue;
      } else {
        return separator;
      }
    }
  }
}


