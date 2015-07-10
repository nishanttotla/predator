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

import org.sosy_lab.cpachecker.util.predicates.interfaces.view.BooleanFormulaManagerView;

import proveit.heapgraph.Graph;


public class HeapOracle {
  int idx = 0;
  String dirName;
  BooleanFormulaManagerView bfmgr;

  public HeapOracle(BooleanFormulaManagerView bfmgr, String dirName) {
    this.dirName = dirName;
  }

  public Graph separator(Set<Graph> pHPlus, Set<Graph> pHMinus) {
    String filename = String.format("%s/graph%03d.dot", dirName, idx);
    idx++;
    return Graph.fromDot(bfmgr, filename);
  }

}
