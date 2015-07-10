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

import org.sosy_lab.cpachecker.cfa.ast.c.CFunctionCallAssignmentStatement;
import org.sosy_lab.cpachecker.cfa.ast.c.CLeftHandSide;
import org.sosy_lab.cpachecker.cfa.ast.c.CStatement;
import org.sosy_lab.cpachecker.cfa.model.CFAEdge;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;
import org.sosy_lab.cpachecker.util.predicates.interfaces.view.BooleanFormulaManagerView;


public class SimpleEdgeEffect_Alloc extends SimpleEdgeEffect {
  private String heapVar;

  public SimpleEdgeEffect_Alloc(CFAEdge pEdge) {
    super(pEdge);
    CStatement cStatement = ((CStatementEdge)pEdge).getStatement();
    if (cStatement instanceof CFunctionCallAssignmentStatement){
      CFunctionCallAssignmentStatement assg = (CFunctionCallAssignmentStatement)cStatement;
      CLeftHandSide lhs = assg.getLeftHandSide();
      heapVar = StmtUtil.getVarName(lhs);
    } else {
      assert(false);
    }
  }

  @Override
  public Footprint apply(BooleanFormulaManagerView bfmgr, Vertex pPrev, Footprint pre) {
    /*
    Footprint post = new Footprint(pre);
    post.terms.add(bfmgr.makeVariable(heapVar));
    return post;
    */
    return pre;
  }

}
