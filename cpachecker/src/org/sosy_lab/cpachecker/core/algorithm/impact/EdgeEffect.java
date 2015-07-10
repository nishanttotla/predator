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

import org.sosy_lab.cpachecker.cfa.ast.c.CExpressionAssignmentStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionCallAssignmentStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdgeType;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;
import org.sosy_lab.cpachecker.util.predicates.interfaces.view.BooleanFormulaManagerView;

import proveit.heapgraph.Graph;



public abstract class EdgeEffect {

  enum OpType {LOAD, STORE, COPY, DATA, ALLOC}
  OpType opType;


  public static EdgeEffect create(CFAEdge pEdge){
    //TODO: check on the type (load, store, copy, data)
    System.out.println("edge effect edge is " + pEdge.getClass());
    CFAEdgeType type = pEdge.getEdgeType();
    if (type == CFAEdgeType.AssumeEdge){
      return new SimpleEdgeEffect_Passthrough(pEdge);
    } else if (type == CFAEdgeType.BlankEdge){
      return new SimpleEdgeEffect_Passthrough(pEdge);
    } else if (type == CFAEdgeType.DeclarationEdge){
      return new SimpleEdgeEffect_Passthrough(pEdge);
    } else if (type == CFAEdgeType.StatementEdge){
      return createStmtEffect((CStatementEdge)pEdge);
    } else {
      System.out.println("unknown edge type " + type);
      assert(false);
      return null;
    }
  }

  private static EdgeEffect createStmtEffect(CStatementEdge pEdge) {
    System.out.println("[opTypeOfStmt] >>>");
    CStatement stmt = pEdge.getStatement();
    if (stmt instanceof CFunctionCallAssignmentStatement){
      CFunctionCallAssignmentStatement funcAssg = (CFunctionCallAssignmentStatement)stmt;
      String funcName = StmtUtil.getFunc(funcAssg.getRightHandSide());
      if (funcName == "malloc"){ return new SimpleEdgeEffect_Alloc(pEdge); }
      else { return null; }
    } else if (stmt instanceof CExpressionAssignmentStatement){
      CExpressionAssignmentStatement assg = (CExpressionAssignmentStatement)stmt;
      if (StmtUtil.hasDeref(assg.getRightHandSide()) != null){
        System.out.println("RHS DEREF!");
        return new SimpleEdgeEffect_Load(pEdge, assg.getLeftHandSide(), assg.getRightHandSide());
      } else if (StmtUtil.hasDeref(assg.getLeftHandSide()) != null) {
        System.out.println("LHS DEREF!");
        return new SimpleEdgeEffect_Store(pEdge, assg.getLeftHandSide(), assg.getRightHandSide());
      } else {
        return new SimpleEdgeEffect_DataOp(pEdge);
      }
    } else {
      System.out.println("unknown stmt of class " + stmt.getClass());
      assert(false);
    }
    return null;
  }

  public EdgeEffect() {
  }

  public Graph apply(Vertex v, Graph pre) {
    return pre;
  }

  public abstract Footprint apply(BooleanFormulaManagerView pBfmgr, Vertex pPrev, Footprint pF);
}
