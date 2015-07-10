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

import org.sosy_lab.cpachecker.cfa.ast.c.CExpression;
import org.sosy_lab.cpachecker.cfa.ast.c.CLeftHandSide;
import org.sosy_lab.cpachecker.cfa.model.c.CStatementEdge;
import org.sosy_lab.cpachecker.util.predicates.interfaces.BooleanFormula;
import org.sosy_lab.cpachecker.util.predicates.interfaces.view.BooleanFormulaManagerView;


public class SimpleEdgeEffect_Load extends SimpleEdgeEffect{
  String lhsVar;
  String rhsVar;

  public SimpleEdgeEffect_Load(CStatementEdge edge, CLeftHandSide pLeftHandSide, CExpression pRightHandSide) {
    super(edge);
    assert(false);
  }

  @Override
  public Footprint apply(BooleanFormulaManagerView bfmgr, Vertex pPrev, Footprint pre) {
    BooleanFormula lhsTerm = bfmgr.makeVariable(lhsVar);
    Footprint post = new Footprint(pre);
    post.addTerm(lhsTerm);
    return post;
  }
}
