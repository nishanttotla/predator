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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.sosy_lab.cpachecker.core.reachedset.ReachedSet;

import proveit.heapgraph.Graph;
import proveit.heapgraph.LabeledEdge;
import proveit.heapgraph.Node;


public class DotPrinter {
  public static void unwindTreeToDot(
        String name, ReachedSet reached)
        throws Exception
  {
    PrintStream dot = new PrintStream(new FileOutputStream(name, false));
    DotPrinterHelper dph = new DotPrinterHelper(dot, name);
    dph.go(reached);

    return;
  }

  public static void heapGraph(String name, Graph graph)
      throws Exception{

    PrintStream dot = new PrintStream(new FileOutputStream(name, false));
    dot.println("digraph \"" + name + "\" {");
    dot.printf("\tlabel=<<FONT POINT-SIZE=\"18\">%s</FONT>>;\n", name);
    HashMap<Node, Integer> nodeID = new HashMap<>();

    if (graph == null){
      dot.printf("}\n");
      dot.flush();
      dot.close();
      return;
    }

    for (Node node : graph.nodes){
      int id = nodeID.size();
      nodeID.put(node, id);
      dot.printf("\t\"%d\" [shape=box, label=\"%s\"];\n",
          id, node.predicate);
    }

    for (LabeledEdge e : graph.edges.keySet()){
      dot.printf("\t\"%d\" -> \"%d\" []\n",
           nodeID.get(e.src),
           nodeID.get(e.dst)
           );
    }

    dot.printf("}\n");
    dot.flush();
    dot.close();
  }
}
