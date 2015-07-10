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

import org.sosy_lab.cpachecker.cfa.model.CFAEdge;

import proveit.heapgraph.Graph;

public class SimpleHeapTransfer {
  static int iter = 0;

  public Graph post(CFAEdge edge, Vertex v, Graph pre) {
    EdgeEffect effect = EdgeEffect.create(edge);
    System.out.println("[post] >>>");
    String graphName = "heap_" + v.getId() + ".dot";
    try {
      DotPrinter.heapGraph(graphName, pre);
    } catch (Exception e){
      e.printStackTrace(System.err);
    }

    Graph post = effect.apply(v, pre);
    return post;
  }
}

