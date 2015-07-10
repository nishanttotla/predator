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


public class ProveIt {
  private HeapOracle oracle;
  private HeapTransfer heapTransfer;
  private BooleanFormulaManagerView bfmgr;
  private InterpolationManager itpmgr;

  public ProveIt(BooleanFormulaManagerView bfmgr, InterpolationManager itpmgr){
    oracle = new HeapOracle();
    heapTransfer = new HeapTransfer();
    this.bfmgr = bfmgr;
    this.itpmgr = itpmgr;
  }

  private class Op{

  }

  private class EnhancedPredicate implements BooleanFormula{

  }

  private class ImpureFormula implements BooleanFormula{
    BooleanFormula pureLeaf;
    EnhancedPredicate impureLeaf;
    LinkedList<ImpureFormula> children;
    Object op;

    ImpureFormula(BooleanFormula formula){
       this.pureLeaf = formula;
       impureLeaf = null;
    }

    public EnhancedPredicate getFirstDerefPredicate() {
      LinkedList<ImpureFormula> worklist = new LinkedList<>();
      worklist.add(this);
      while(!worklist.isEmpty()){
        ImpureFormula elt = worklist.pollFirst();
        if (elt.impureLeaf != null){
          return impureLeaf;
        }

        if (elt.pureLeaf != null){
          continue;
        } else {
          for (ImpureFormula child : elt.children){
            if (child.pureLeaf == null && child.impureLeaf == null){
              worklist.add(child);
            }
          }
        }
      }
      return null;
    }
  }

  private class StateDescription{
    ImpureFormula phi;
    Graph pat;

    void init(){
      phi = new ImpureFormula(bfmgr.makeBoolean(true));
      pat = Graph.emptyHeap();
    }

    BooleanFormula purify(){
      return null;
    }
  }

  private class Footprint{

  }

  private class PointInfo{
    //F: footprint
    //S: set of state descriptions
    Footprint F;
    Set<StateDescription> S;

    PointInfo(){
      F = new Footprint();
      S = new HashSet<StateDescription>();
    }

    void init(){
      StateDescription s = new StateDescription();
      s.init();
      S.add(s);
    }

    BooleanFormula restrict(StateDescription s){
      return null;
    }
  }

  BooleanFormula semanticConstraint(CFAEdge edge){
    BooleanFormula b = bfmgr.makeBoolean(true);
    return b;
  }

  private BooleanFormula suffixConstraint(List<Vertex> path, Vertex pt){

    BooleanFormula suffixFormula = bfmgr.makeBoolean(true);
    boolean start = false;
    for (Vertex v : path){
      if (start) {
        CFAEdge e = edge(v.getParent(), v);
        suffixFormula = bfmgr.and(suffixFormula, pt.getStateFormula());
      }
      if (v == pt){
        start = true;
      }
    }
    return suffixFormula;
  }

  private Object purify(Set<Object> pHashSet, BooleanFormula phi, Graph pReach){
    if (phi instanceof ImpureFormula) {
      ImpureFormula impurePhi = (ImpureFormula)phi;
      impurePhi.getFirstDerefPredicate();
//TODO      phi_T = impurePhi.purify(pHashSet, phi, pReach);
      return null;
    } else {
      return null;
    }
  }

  private BooleanFormula getItp(BooleanFormula pre, BooleanFormula upd, BooleanFormula post){
    BooleanFormula itp = null;
    try {
      LinkedList<BooleanFormula> forms = new LinkedList<BooleanFormula>();
      forms.add(bfmgr.and(pre, upd)); //TODO: should be pre ^ upd?
      forms.add(post);
      CounterexampleTraceInfo myCEX = itpmgr.buildCounterexampleTrace(forms);
      itp = myCEX.getInterpolants().get(0);
    } catch (Exception e){

    }
    return itp;
  }

  public CounterexampleTraceInfo buildCounterexampleTrace(List<Vertex> path, List<BooleanFormula> pathFormulas) {
    LinkedList<PointInfo> pts = new LinkedList<>();
    Vertex prev = null;
    PointInfo prevInfo = null;
    for (Vertex v : path){
      PointInfo currInfo;
      if (prev == null){ // Program root
        currInfo = new PointInfo();
      } else {
        currInfo = new PointInfo();

        CFAEdge edge = edge(prev, v);
        //Update Footprint

        for (StateDescription s : prevInfo.S){
          currInfo.F = this.tranformFootprint(prevInfo.F, edge);
          BooleanFormula pre = prevInfo.restrict(s);
          BooleanFormula upd = semanticConstraint(edge);
          BooleanFormula post = suffixConstraint(path, v);
          BooleanFormula itp = getItp(pre, upd, post);

          Graph reach = heapTransfer.post(edge, v, s.pat);
//          pCases = purify(new HashSet<>(), itp, reach);
        }

//        BooleanFormula interp = this.itpmgr.
//        itpmgr.buildCounterexampleTrace(pFormulas);

      }
      pts.addLast(currInfo);
      prevInfo = currInfo;
      prev = v;
    }
    return null;
  }

  private Footprint tranformFootprint(Footprint f, CFAEdge edge) {
    //TODO: pointer copy
    //TODO: load
    //TODO: store
    return null;
  }

  //Roughly corresponds to RefineTree
  /*
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
  */

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
      Graph r = findReach(patternR, separator);
      Graph e = findErr(path, separator);
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

  Graph findReach(Graph patternR, Graph separator){
    return null;
  }

  Graph findErr(List<Vertex> path, Graph separator){
    return null;
  }
}


