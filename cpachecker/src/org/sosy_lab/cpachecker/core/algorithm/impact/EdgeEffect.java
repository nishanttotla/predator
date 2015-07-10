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
import org.sosy_lab.cpachecker.cfa.model.CFAEdgeType;

import proveit.heapgraph.Graph;



public abstract class EdgeEffect {

  enum OpType {LOAD, STORE, COPY, DATA}
  OpType opType;


  public static EdgeEffect create(CFAEdge pEdge){
    //TODO: check on the type (load, store, copy, data)
    OpType type = checkType(pEdge);
    if (type == OpType.LOAD){
      return new SimpleEdgeEffect_Load(pEdge);
    } else if (type == OpType.STORE){
      return new SimpleEdgeEffect_Store(pEdge);
    } else if (type == OpType.COPY){
      return new SimpleEdgeEffect_Copy(pEdge);
    } else if (type == OpType.DATA){
      return new SimpleEdgeEffect_DataOp(pEdge);
    } else {
      return new SimpleEdgeEffect_Passthrough(pEdge);
    }
  }

  private static OpType checkType(CFAEdge pEdge) {
    System.out.println("edge effect edge is " + pEdge.getClass());
    CFAEdgeType type = pEdge.getEdgeType();
    if (type == CFAEdgeType.AssumeEdge){
      return null;
    } else if (type == CFAEdgeType.DeclarationEdge){
        return null;
    } else {
      System.out.println("unknown edge type " + type);
      assert(false);
      return null;
    }
  }

  public EdgeEffect() {
  }

  public Graph apply(Vertex v, Graph pPre) {
    return null;
  }

  public abstract Footprint apply(Vertex pPrev, Footprint pF);
}
