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
import proveit.heapgraph.SeparatorChecker;


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

  private class EnhancedPredicate implements BooleanFormula{

  }

  enum ExOP {AND, OR}
  private class ImpureFormula implements BooleanFormula{

    BooleanFormula pureLeaf;
    EnhancedPredicate impureLeaf;
    LinkedList<ImpureFormula> children;
    ExOP op;

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

    public ImpureFormula subDerefPredicate(EnhancedPredicate pre, BooleanFormula post) {
      // TODO Auto-generated method stub
      return null;
    }

    public BooleanFormula getPureBF(){
      assert(this.impureLeaf == null);
      if (this.pureLeaf != null){
        return this.pureLeaf;
      }
      BooleanFormula result = null;
      switch (this.op){
      case AND:
        for (ImpureFormula child : children){
          result = bfmgr.and(result, child);
        }
        break;
      case OR:
        for (ImpureFormula child : children){
          result = bfmgr.or(result, child);
        }
        break;
      }
      return result;
    }

    public boolean isPure() {
      LinkedList<ImpureFormula> worklist = new LinkedList<>();
      worklist.add(this);
      while (!worklist.isEmpty()){
        ImpureFormula form = worklist.pollFirst();
        if (form.pureLeaf != null){
          //Do nothing
        } else if (form.impureLeaf != null){
          return false;
        } else {
          for(ImpureFormula child : form.children){
            worklist.add(child);
          }
        }
      }
      return true;
    }
  }

  private class StateDescription{
    ImpureFormula phi;
    Graph pat;

    StateDescription(){
      phi = new ImpureFormula(bfmgr.makeBoolean(true));
      pat = Graph.emptyHeap();
    }

    StateDescription(ImpureFormula phi, Graph pat){
      this.phi = phi;
      this.pat = pat;
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

  private Graph weakest_entailment(Set<BooleanFormula> preds){
    assert(false);
    return null;
  }

  /**
  * @return For every StateDescription pair,
  * the formula is in a theory that describes only the program's data and
  * the pattern is entailed by REACH
  **/
  private Set<StateDescription> purify(Set<BooleanFormula> ctx, ImpureFormula phi, Graph reach){
    if (phi.isPure()) {
      ImpureFormula impurePhi = phi;
      EnhancedPredicate dp = impurePhi.getFirstDerefPredicate();


      Set<StateDescription> sT; {
      ImpureFormula phi_T = impurePhi.subDerefPredicate(dp, bfmgr.makeBoolean(true));
      Set<BooleanFormula> t_ctx = new HashSet<>();
      t_ctx.addAll(ctx);
      t_ctx.add(bfmgr.equivalence(dp, bfmgr.makeBoolean(true)));
      sT = purify(t_ctx, phi_T, reach);
      }

      Set<StateDescription> sF; {
      ImpureFormula phi_F = impurePhi.subDerefPredicate(dp, bfmgr.makeBoolean(false));
      Set<BooleanFormula> f_ctx = new HashSet<>();
      f_ctx.addAll(ctx);
      f_ctx.add(bfmgr.equivalence(dp, bfmgr.makeBoolean(false)));
      sF = purify(f_ctx, phi_F, reach);
      }

      Set<StateDescription> result = sT;
      result.addAll(sF);
      return result;
    } else {
      //phi has no dereference predicate
      BooleanFormula phiPure = phi.getPureBF();
      Graph pat_ctx = weakest_entailment(ctx);
      Graph pat_user = SeparatorChecker.findReach(reach, pat_ctx);
      Set<StateDescription> result = new HashSet<StateDescription>();
      result.add(new StateDescription(phi, pat_user));
      return result;
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
          ImpureFormula itpImpure = new ImpureFormula(itp);
          Set<StateDescription> pcases = purify(new HashSet<BooleanFormula>(), itpImpure, reach);
        }

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
    assert(false);
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


