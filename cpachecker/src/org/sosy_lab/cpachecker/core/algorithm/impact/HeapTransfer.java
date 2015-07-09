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

import org.sosy_lab.cpachecker.cfa.ast.c.CDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CExpressionAssignmentStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CFieldReference;
import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionCallAssignmentStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CLeftHandSide;
import org.sosy_lab.cpachecker.cfa.ast.c.CSimpleDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.model.BlankEdge;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CAssumeEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CDeclarationEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;

import proveit.heapgraph.Graph;
import proveit.heapgraph.HeapVar;

public class HeapTransfer {
  static int iter = 0;

  public void post(CFAEdge edge, Vertex preV, Vertex postV) {
    System.out.println("[post] >>>");
    String graphName = "heap_" + preV.getId() + ".dot";
    try {
      DotPrinter.heapGraph(graphName, preV.getHeap());
    } catch (Exception e){
      e.printStackTrace(System.err);
    }

    if (edge instanceof CStatementEdge){
      CStatement stmt = ((CStatementEdge)edge).getStatement();
      System.out.println("edge stmt " + stmt);
      applyStmt(stmt, preV, postV);
    } else if (edge instanceof BlankEdge){
      postV.setHeap(preV.getHeap());
      if (preV.getHeap() == null){
        System.out.println("copy null heap...");
        assert(false);
      }
    } else if (edge instanceof CDeclarationEdge){
      CDeclaration decl = ((CDeclarationEdge)edge).getDeclaration();
      postV.setHeap(preV.getHeap());
      applyDecl(decl, postV);
    } else if (edge instanceof CAssumeEdge){
      //TODO: don't think assumes should change the heap
      postV.setHeap(preV.getHeap());
    } else {
      System.out.println("unknown edge type " + edge.getClass());
      assert(false);
    }

    if (postV.getHeap() == null){
      System.out.println("NULL HEAP");
      assert(false);
    }
    String graphNamePost = "heap_" + postV.getId();
    try {
      DotPrinter.heapGraph(graphName, postV.getHeap());
    } catch (Exception e){
      e.printStackTrace(System.err);
    }

    iter++;
  }

  public void applyStmt(CStatement stmt, Vertex preV, Vertex pW){

    //Expression assignment. Could be predicate var or heap var
    if (stmt instanceof CExpressionAssignmentStatement) {
      CExpressionAssignmentStatement assg =
          (CExpressionAssignmentStatement)stmt;
      CLeftHandSide lhs = assg.getLeftHandSide();
      System.out.println("LHS: " + lhs + " " + lhs.getClass());

      if (lhs instanceof CIdExpression){
        CSimpleDeclaration decl = extractDeclUsed(lhs);

        Graph heap = new Graph(preV.getHeap());
        pW.setHeap(heap);
        applyDecl(decl, pW);
      } else if (lhs instanceof CFieldReference){
        CFieldReference lhsRef = (CFieldReference)lhs;
        String field = lhsRef.getFieldName();
        CSimpleDeclaration decl = extractDeclUsed(lhsRef.getFieldOwner());
        CExpression rhs = assg.getRightHandSide();

        Graph heap = new Graph(preV.getHeap());
        pW.setHeap(heap);
        HeapVar vDst = heap.findOrMakeVar(decl);
        //HeapVar vSrc =heap.findOrMakeVar(pDst);
        //heap.store(vDst, vSrc, field);

      } else {
        System.out.println("unknown LHS " + lhs);
        assert(false);
      }
    } else if (stmt instanceof CFunctionCallAssignmentStatement){
      CFunctionCallAssignmentStatement assg =
          (CFunctionCallAssignmentStatement)stmt;
      Graph heap = new Graph(preV.getHeap());
      pW.setHeap(heap);
    } else {
      System.out.println("unknown Stmt type " + stmt.getClass());
      assert(false);
    }
  }

  public void applyDecl(CSimpleDeclaration decl, Vertex vert){
    //TODO: if the variable being declared is a HeapVar, we should
    // create it. Otherwise, we should track it as a predicate variable
  }

  public static CSimpleDeclaration extractDeclUsed(CExpression expr){
    System.out.println("ExtractDeclUsed " + expr.getClass());
    if (expr instanceof  CIdExpression){
      CIdExpression idExpr = (CIdExpression)expr;
      return idExpr.getDeclaration();
    }
    assert(false);
    return null;
  }
}

