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

import java.util.Set;
import java.util.TreeSet;

import org.sosy_lab.cpachecker.cfa.ast.c.CSimpleDeclaration;

import proveit.heapgraph.Graph;
import proveit.heapgraph.HeapVar;


class ProveItState {
  Graph g;
  Set<CSimpleDeclaration> vars;

  public ProveItState(){
    g = new Graph();
    vars = new TreeSet<CSimpleDeclaration>();
  }

  public ProveItState(ProveItState other){
    g = new Graph(other.g);
    vars.addAll(other.vars);
  }

  public ProveItState alloc(CSimpleDeclaration dst){
    ProveItState succ = new ProveItState(this);
    HeapVar vIn = succ.g.findOrMakeVar(dst);
//    Node nIn = new Node();
//    succ.g.allocNode(vIn, nIn);
    return succ;
  }

  public ProveItState load(CSimpleDeclaration dst, CSimpleDeclaration src, String field){
    ProveItState succ = new ProveItState(this);

    return succ;
  }
}
