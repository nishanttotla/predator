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

import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFANode;
import org.sosy_lab.cpachecker.util.AbstractStates;
import org.sosy_lab.cpachecker.util.predicates.Solver;
import org.sosy_lab.cpachecker.util.predicates.interfaces.BooleanFormula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.ProverEnvironment;
import org.sosy_lab.cpachecker.util.predicates.interfaces.view.BooleanFormulaManagerView;
import org.sosy_lab.cpachecker.util.predicates.interpolation.CounterexampleTraceInfo;
import org.sosy_lab.cpachecker.util.predicates.interpolation.InterpolationManager;

import proveit.heapgraph.Graph;
import proveit.heapgraph.Node;
import proveit.heapgraph.SeparatorChecker;


public class ProveItInterp {
  private HeapOracle oracle;
  private BooleanFormulaManagerView bfmgr;
  private InterpolationManager itpmgr;
  private Solver solver;
  private SeparatorChecker user;

  public ProveItInterp(BooleanFormulaManagerView bfmgr, InterpolationManager itpmgr, Solver solver){
    oracle = new HeapOracle(bfmgr, ".");
    user = new SeparatorChecker(oracle);

    this.bfmgr = bfmgr;
    this.itpmgr = itpmgr;
    this.solver = solver;
  }

  enum ExOP {AND, OR}

  private class DerefTerm implements BooleanFormula{

    public Set<Node> pointsToSet() {
      Set<Node> result = new HashSet<Node>();
      assert(false);
      return result;
    }
  }

  private class ImpureFormula implements BooleanFormula{

    BooleanFormula pureLeaf;
    DerefTerm impureLeaf;
    LinkedList<ImpureFormula> children;
    ExOP op;

    ImpureFormula(BooleanFormula formula){
       this.pureLeaf = formula;
       impureLeaf = null;
       children = new LinkedList<>();
    }

    public List<DerefTerm> getAllDerefPredicates(){
      LinkedList<DerefTerm> results = new LinkedList<>();
      LinkedList<ImpureFormula> worklist = new LinkedList<>();
      worklist.add(this);
      while(!worklist.isEmpty()){
        ImpureFormula elt = worklist.pollFirst();
        if (elt.impureLeaf != null){
          results.add(impureLeaf);
        } else if (elt.pureLeaf != null){
          continue;
        } else {
          for (ImpureFormula child : elt.children){
            if (child.pureLeaf == null && child.impureLeaf == null){
              worklist.add(child);
            }
          }
        }
      }
      return results;
    }

    public BooleanFormula getFirstDerefPredicate() {
      LinkedList<ImpureFormula> worklist = new LinkedList<>();
      worklist.add(this);
      while(!worklist.isEmpty()){
        ImpureFormula elt = worklist.pollFirst();
        if (elt.impureLeaf != null){
          return impureLeaf;
        } else if (elt.pureLeaf != null){
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

    public ImpureFormula subDerefPredicate(BooleanFormula pre, BooleanFormula post) {
      // TODO Auto-generated method stub
      return null;
    }

    public BooleanFormula getPureBF(){
      assert(this.impureLeaf == null);
      if (this.pureLeaf != null){
        return this.pureLeaf;
      }

      System.out.println("Pure leaf is " + this.pureLeaf);
      System.out.println("impure leaf is " + this.impureLeaf);
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

  private class PointInfo{
    //F: footprint
    //S: set of state descriptions
    Footprint F;
    Set<StateDescription> S;

    PointInfo(){
      F = new Footprint();
      S = new HashSet<>();
    }

    void init(){
      StateDescription s = new StateDescription();
      S.add(s);
    }

    BooleanFormula restrict(StateDescription s){
      BooleanFormula result = bfmgr.makeBoolean(true);
      List<DerefTerm> derefPreds = s.phi.getAllDerefPredicates();
      for (DerefTerm deref : derefPreds){
        Set<Node> pointsTo = deref.pointsToSet();
      }
      return result;
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

  private Graph weakest_entailment(Set<BooleanFormula> preds) throws Exception{
    BooleanFormula ctx_term = null;
    for (BooleanFormula bf : preds){
      if (ctx_term == null){
        ctx_term = bf;
      } else {
        ctx_term = bfmgr.and(ctx_term, bf);
      }
    }
    if (ctx_term == null){ return Graph.emptyHeap(); }

    //Compute MUST_EQ classes
    //Is it valid for the two nodes to be equivalent? (e.g. is the negation unsat?)
    LinkedList<Set<BooleanFormula>> must_eq_classes = new LinkedList<>();
    for (BooleanFormula bf : preds){
      boolean foundClass = false;
      for (Set<BooleanFormula> must_eq : must_eq_classes){
        BooleanFormula rep = must_eq.iterator().next();
        BooleanFormula test = bfmgr.implication(ctx_term, bfmgr.equivalence(rep, bf));
        BooleanFormula testNeg = bfmgr.not(test);
        ProverEnvironment env = solver.newProverEnvironment();
        if (solver.isUnsat(testNeg)){
          foundClass = true;
          must_eq.add(ctx_term);
          break;
        }
      }
      if (foundClass == false){
        Set<BooleanFormula> new_class = new HashSet<>();
        new_class.add(bf);
        must_eq_classes.add(new_class);
      }
    }

    //Compute MAY_EQ classes
    //Is it consistent (e.g. is sat?)
    LinkedList<Set<BooleanFormula>> may_eq_classes = new LinkedList<>();
    for (BooleanFormula bf : preds){
      boolean foundClass = false;
      for (Set<BooleanFormula> may_eq : may_eq_classes){
        BooleanFormula rep = may_eq.iterator().next();
        BooleanFormula test = bfmgr.implication(ctx_term, bfmgr.equivalence(rep, bf));
        ProverEnvironment env = solver.newProverEnvironment();
        if (!solver.isUnsat(test)){
          foundClass = true;
          may_eq.add(ctx_term);
          break;
        }
      }
      if (foundClass == false){
        Set<BooleanFormula> new_class = new HashSet<>();
        new_class.add(bf);
        may_eq_classes.add(new_class);
      }
    }

    //TODO: this is an over-approximation
    Graph g = new Graph();
    for (Set<BooleanFormula> may_equal1 : may_eq_classes){
      Node n = new Node(bfmgr.makeBoolean(true));
      n.predicate = conjunction(may_equal1);
      for (Set<BooleanFormula> may_equal2 : may_eq_classes){
        if (may_equal1 == may_equal2){
          continue;
        } else {

        }
      }
    }
    return null;
  }

  private BooleanFormula conjunction(Set<BooleanFormula> elts){
    BooleanFormula res = null;
    for (BooleanFormula e : elts){
      if (res == null){
        res = e;
      } else {
        res = bfmgr.and(e, res);
      }
    }
    return res;
  }

  /**
  * @return For every StateDescription pair,
  * the formula is in a theory that describes only the program's data and
  * the pattern is entailed by REACH
  **/
  private Set<StateDescription> purify(
          Set<BooleanFormula> ctx, ImpureFormula phi, Graph reach)
          throws Exception
  {
    if (!phi.isPure()) {
      ImpureFormula impurePhi = phi;
      BooleanFormula dp = impurePhi.getFirstDerefPredicate();

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
      Graph pat_user = user.findReach(reach, pat_ctx);
      Set<StateDescription> result = new HashSet<StateDescription>();
      result.add(new StateDescription(phi, pat_user));
      return result;
    }
  }

  private BooleanFormula getItp(BooleanFormula pre, BooleanFormula upd, BooleanFormula post){
    BooleanFormula itp = null;
    System.out.printf("[getItp] pre %s upd %s post %s\n", pre, upd, post);
    try {
      LinkedList<BooleanFormula> forms = new LinkedList<>();
      forms.add(bfmgr.and(pre, upd));
      forms.add(post);
      CounterexampleTraceInfo myCEX = itpmgr.buildCounterexampleTrace(forms);
      if (myCEX.isSpurious()){
        System.out.println("spurious counterexample!");
        itp = myCEX.getInterpolants().get(0);
      } else {
        itp = myCEX.getCounterExampleFormulas().get(0);
        System.out.println("cex formula " + itp);
      }
    } catch (Exception e){
      System.out.println("Exception in generating interpolant");
      e.printStackTrace();
      assert(false);
    }
    System.out.println("interpolant is " + itp);
    return itp;
  }

  public CounterexampleTraceInfo buildCounterexampleTrace(List<Vertex> path, List<BooleanFormula> pathFormulas) {
    LinkedList<PointInfo> pts = new LinkedList<>();
    Vertex prev = null;
    PointInfo prevInfo = null;
    System.out.println("[ProveItInterp.buildCounterexampleTrace] >>>");
    for (Vertex v : path){
      System.out.println("At node " + v.getId());
      PointInfo currInfo;
      if (prev == null){ // Program root
        currInfo = new PointInfo();
        currInfo.init();
      } else {
        currInfo = new PointInfo();

        CFAEdge edge = edge(prev, v);
        EdgeEffect transform = EdgeEffect.create(edge);
        //Update Footprint

        System.out.println("prev info size is " + prevInfo.S.size());
        for (StateDescription s : prevInfo.S){
          try {
            currInfo.F = transform.apply(bfmgr, prev, prevInfo.F);
            BooleanFormula pre = prevInfo.restrict(s);
            BooleanFormula upd = semanticConstraint(edge);
            BooleanFormula post = suffixConstraint(path, v);
            BooleanFormula itp = getItp(pre, upd, post);

            Graph reach = transform.apply(v, s.pat);
            ImpureFormula itpImpure = new ImpureFormula(itp);

            Set<StateDescription> pcases = purify(new HashSet<BooleanFormula>(), itpImpure, reach);

          } catch(Exception e){
            e.printStackTrace();
          }
        }
      }
      pts.addLast(currInfo);
      prevInfo = currInfo;
      prev = v;
    }
    System.out.println("[ProveItInterp.buildCounterexampleTrace] <<<");
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
}


