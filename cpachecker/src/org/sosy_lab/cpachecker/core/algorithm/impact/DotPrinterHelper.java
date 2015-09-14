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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.sosy_lab.cpachecker.core.interfaces.AbstractState;
import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;
import org.sosy_lab.cpachecker.cpa.composite.CompositeState;
import org.sosy_lab.cpachecker.cpa.location.LocationState;


public class DotPrinterHelper {
  String name;
  PrintStream stream;

  public DotPrinterHelper(PrintStream stream, String name){
    this.name = name;
    this.stream = stream;
  }

  private void printHeader(){
    stream.println("digraph \"" + name + "\" {");
    stream.printf("\tlabel=<<FONT POINT-SIZE=\"18\">%s</FONT>>;\n", name);
  }

  private void printFooter(){
    stream.printf("}\n");
    stream.flush();
    stream.close();
  }

  private void printBody(DotNode head){

    LinkedList<DotNode> visited = new LinkedList<>();
    LinkedList<DotNode> worklist = new LinkedList<>();
    worklist.add(head);
    while(!worklist.isEmpty()){
      DotNode elt = worklist.peek(); worklist.pop();
      if (visited.contains(elt)){
        continue;
      } else {
        visited.add(elt);
      }

      Vertex v = (Vertex)elt.abstractStates.get(0);
      AbstractState wrapped = v.getWrappedState();
      String desc = wrapped.toString().replace("\n", "\\n");
      String desc2 = v.getId() + "\\n" + v.getStateFormula()
          + "\\n" + desc + elt.abstractStates.size();

      stream.printf("\t\"%d\" [shape=box, label=\"%s\"];\n",
          v.getId(),
          desc2);

      worklist.addAll(elt.children);
    }

    for (DotNode node : visited){
      for (DotNode child : node.children){
        stream.printf("\t\"%d\" -> \"%d\" [];\n",
            node.primeId, child.primeId);
      }
    }
  }

  public void go(ReachedSet reached){

    DotNode head = buildInitialDotGraph(reached);
    LinkedList<DotNode> worklist = new LinkedList<>();
    worklist.add(head);
    while(!worklist.isEmpty()){
      DotNode elt = worklist.peek(); worklist.pop();
      elt.mergeWithChildren();
      worklist.addAll(elt.children);
    }

    printHeader();
    printBody(head);
    printFooter();
  }

  private DotNode buildInitialDotGraph(ReachedSet set){
    HashMap<AbstractState, DotNode> nodes =
        new HashMap<>();

    for (AbstractState as : set){
      nodes.put(as, new DotNode(as));
    }

    for (DotNode n : nodes.values()){
      Vertex v = (Vertex)n.abstractStates.get(0);
      if (v.hasParent()){
        AbstractState parentState = v.getParent();
        DotNode parentDot = nodes.get(parentState);
        parentDot.children.add(n);
        n.parent = parentDot;
      }
    }

    List<DotNode> heads = new LinkedList<>();
    for (DotNode n : nodes.values()){
      if (n.parent == null){
        heads.add(n);
        System.out.println("head #" + heads.size() + " " + n.abstractStates.get(0));
      }
    }
    System.out.println("number of heads " + heads.size());
    return heads.iterator().next();
  }

  private class DotNode{
    Integer primeId;
    List<Integer> ids;
    List<AbstractState> abstractStates;
    DotNode parent;

    List<DotNode> children;

    DotNode(){
      this.abstractStates = new LinkedList<>();
      this.children = new LinkedList<>();
      this.ids = new LinkedList<>();
      this.parent = null;
    }

    DotNode(AbstractState as){
      this();
      this.abstractStates.add(as);
      this.primeId = ((Vertex)as).getId();
      this.ids.add(primeId);
    }

    void mergeWithChildren(){
      LinkedList<DotNode> worklist = new LinkedList<>();
      LinkedList<DotNode> visited = new LinkedList<>();
      worklist.addAll(children);
      while (!worklist.isEmpty()){
        DotNode elt = worklist.peekFirst(); worklist.removeFirst();
        if (visited.contains(elt)){
          continue;
        } else {
          visited.add(elt);
        }

        boolean doMerge = false;

        LocationState loc = elt.getLocState();
        if (loc == null){
          doMerge = false;
        } else {
          String locString = loc.toString();
          if (locString.contains("/usr/include")){ doMerge = true; }
          else if (locString.contains("/usr/lib")){ doMerge = true; }
          else { doMerge = false; }
        }

        //Do the merge
        if (doMerge){
          this.abstractStates.addAll(elt.abstractStates);
          this.ids.addAll(elt.ids);
          this.children.remove(elt);
          this.children.addAll(elt.children);
          for (DotNode newChild : elt.children){
            newChild.parent = this;
            if (!visited.contains(newChild)){
              worklist.add(newChild);
            }
          }
        }
      }
      return;
    }

    LocationState getLocState(){
      Vertex v = (Vertex)this.abstractStates.get(0);
      AbstractState wrapped = v.getWrappedState();
      if (wrapped instanceof CompositeState){
        CompositeState cs = (CompositeState)wrapped;
        int numStates = cs.getNumberOfStates();
        for (int stateIdx = 0 ; stateIdx < numStates ; stateIdx++){
          AbstractState state = cs.get(stateIdx);
          if (state instanceof LocationState){
            LocationState ls = (LocationState)state;
            return ls;
          }
        }
      }
      return null;
    }
  }
}

