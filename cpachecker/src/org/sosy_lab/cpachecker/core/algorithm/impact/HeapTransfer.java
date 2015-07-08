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
import org.sosy_lab.cpachecker.cfa.ast.c.CIdExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CLeftHandSide;
import org.sosy_lab.cpachecker.cfa.ast.c.CSimpleDeclaration;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;

public class HeapTransfer {
  public void post(CFAEdge edge, Vertex pV, Vertex pW) {
    if (edge instanceof CStatementEdge){
      CStatement stmt = ((CStatementEdge)edge).getStatement();
      System.out.println("edge stmt " + stmt);
      applyStmt(stmt, pV, pW);
    } else {
      return;
    }
  }

  public void applyStmt(CStatement stmt, Vertex pV, Vertex pW){
    System.out.println("CStatment " + stmt + " " + stmt.getClass());
    if (stmt instanceof CExpressionAssignmentStatement) {
      CExpressionAssignmentStatement assg =
          (CExpressionAssignmentStatement)stmt;
      CLeftHandSide lhs = assg.getLeftHandSide();
      System.out.println("LHS: " + lhs + " " + lhs.getClass());
      if (lhs instanceof CIdExpression){
        CIdExpression id = (CIdExpression)lhs;
        CSimpleDeclaration decl = id.getDeclaration();
        if (decl != null){

        }
        System.out.println(id + " Declaration " + id.getDeclaration());
      } else {

      }
    } else {
      System.out.println("unknown CExpression type " + stmt.getClass());
    }
  }


}

