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

import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.sosy_lab.cpachecker.util.predicates.Solver;


public class SeparatorChecker {
  Solver solver;

  LinkedList<Relation> simulationFixpoint(Graph a, Graph b){
    LinkedList<Relation> relationChain = new LinkedList<>();

    //Generate the base relation - all pairs of nodes
    // with the same predicate
    Relation baseRelation = new Relation();
    for (Node aN : a.nodes){
      for (Node bN : b.nodes){
        if (aN.predicate.equals(bN.predicate)){
          baseRelation.add(aN, bN);
        }
      }
    }

    relationChain.addLast(baseRelation);

    Relation prevRelation = baseRelation;
    while(true){
      Relation nextRelation = new Relation();
      for (Edge e : prevRelation){
        Node aN = e.src;
        Node bN = e.dst;
        Set<Node> aNsuccs = a.succNodes(aN);
        boolean allSuccsSimulatable = true;
        for (Node aNsucc : aNsuccs){
           Set<Node> bNsuccs = b.succNodes(bN);
           boolean thisSuccSimulatable = false;
           for (Node bNsucc : bNsuccs){
              if (aNsucc.predicate.equals(bNsucc.predicate)){
                //We found a successor to b that simulates this
                // successor to a, if its in the relation then
                // aN->aNsucc can be simulated by bN->bNsucc
                if (prevRelation.hasEdge(aNsucc,bNsucc)) {
                  thisSuccSimulatable = true;
                }
              }
           }
           if (thisSuccSimulatable == false){
             allSuccsSimulatable = false;
           }
        }
        if (allSuccsSimulatable){
          nextRelation.add(aN, bN);
        }
      }

      if (nextRelation.size() == prevRelation.size()){
        //Reached a fixpoint - no more edges can be eliminated
        break;
      } else {
        relationChain.addLast(nextRelation);
        prevRelation = nextRelation;
      }
    }
    return relationChain;
  }


  LinkedList<Relation> entailedBy(Graph a, Graph b){
    LinkedList<Relation> chain = simulationFixpoint(a,b);
    Set<Node> aRoots = a.roots();

    Relation best = chain.getLast();
    Set<Node> matchedRoots = new TreeSet<Node>();
    for (Edge e : best){
      if (e.src.root && e.dst.root){
        matchedRoots.add(e.src);
      }
    }
    if (aRoots.size() == matchedRoots.size()){
      //Every root matched, so a is entailed by b
      return null;
    }
    return chain;
  }

  boolean separates(Graph hMinus, Graph hPlus, Graph u){
    LinkedList<Relation> hPlusNotCovered = entailedBy(hPlus, u);
    if (hPlusNotCovered == null){
      //The user has matched the positive example
    } else {
      // Generate hPlus counterexample
      return false;
    }

    LinkedList<Relation> hMinusCovers = entailedBy(u, hMinus);
    if (hMinusCovers != null){
      return false;
    }
    return true;
  }
}
