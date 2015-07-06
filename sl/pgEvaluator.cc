#include "heapOracle.hh"
#include "pgEvaluator.hh"

#include <map>
#include <utility>

PGRelation matchingPropositions(NodeVec * vecA, NodeVec * vecB){
	PGRelation result;

	for (NodeVec::iterator aItr = vecA->begin(); aItr != vecA->end() ; ++aItr){
		ModifierNode * nodeA = *aItr;
		for (NodeVec::iterator bItr = vecB->begin() ; bItr != vecB->end() ; ++bItr){
			ModifierNode * nodeB = *bItr;
			if (nodeA->APs == nodeB->APs){
				result.insert(PGEdge(nodeA,nodeB));
			}
		}
	}

	return result;
}

PGRelation matchingRoots(PatternGraph * m1, PatternGraph * m2){
	NodeVec m1Roots = m1->roots();
	NodeVec m2Roots = m2->roots();

	PGRelation result;
	for (NodeVec::iterator m1Itr=m1Roots.begin(); m1Itr!=m1Roots.end(); ++m1Itr){
		ModifierNode * m1Root = *m1Itr;
		for (NodeVec::iterator m2Itr=m2Roots.begin(); m2Itr!=m2Roots.end(); ++m2Itr){
			ModifierNode * m2Root = *m2Itr;
			if (m1Root->APs == m2Root->APs){
				result.insert(PGEdge(m1Root,m2Root));
			}
		}
	}
	return result;
}

bool isSimulated(PatternGraph * g, PGRelation& rel){
	//Test if each root of is in the simuatlion
	NodeVec roots = ((PatternGraph *)g)->roots();
	std::vector<ModifierNode *>::iterator rootItr = roots.begin();
	std::vector<ModifierNode *>::iterator rootEnd = roots.end();
	while(rootItr != rootEnd){
		ModifierNode * node = *rootItr;
		PGRelation::iterator relItr = rel.begin();
		PGRelation::iterator relEnd = rel.end();
		bool thisRootInSimulation = false;
		while (relItr != relEnd){
			if (relItr->first == node){
				if (relItr->second->isRoot()){
					thisRootInSimulation = true;
					break;
				}
			}
			++relItr;
		}
		if (thisRootInSimulation == false){
			return false;
		}
		++rootItr;
	}
	return true;
}

SimulationChecker::SimulationChecker(){
}

std::vector<PGEdge> candidateMatches(PGEdge& edge, std::vector<PGEdge>& edgeList){
	std::vector<PGEdge> result;

	std::vector<PGEdge>::iterator edgeItr = edgeList.begin();
	std::vector<PGEdge>::iterator edgeEnd = edgeList.end();
	while (edgeItr != edgeEnd){
		PGEdge& candidateEdge = *edgeItr;
		if (candidateEdge.first->APs == edge.first->APs &&
			 candidateEdge.second->APs == edge.second->APs){
			 result.push_back(candidateEdge);
		 }
		++edgeItr;
	}
	return result;
}

PGRelation generateBaseRelation(PatternGraph * m1, PatternGraph * m2){
	std::vector<ModifierNode *>::iterator m1_nodeItr = m1->nodes.begin();
	std::vector<ModifierNode *>::iterator m1_nodeEnd = m1->nodes.end();

	PGRelation result;
	while (m1_nodeItr != m1_nodeEnd){
		std::vector<ModifierNode *>::iterator m2_nodeItr = m2->nodes.begin();
		std::vector<ModifierNode *>::iterator m2_nodeEnd = m2->nodes.end();
		while (m2_nodeItr != m2_nodeEnd){
			ModifierNode * m1_node = *m1_nodeItr;
			ModifierNode * m2_node = *m2_nodeItr;

			APSet& props = (*m1_nodeItr)->APs;
			APSet& otherProps = (*m2_nodeItr)->APs;

			if (props == otherProps){
				NodePair * pair = new NodePair(*m1_nodeItr, *m2_nodeItr);
				result.insert(PGEdge(*m1_nodeItr, *m2_nodeItr));
			}

			++m2_nodeItr;
		}
		++m1_nodeItr;
	}
}

PGRelation iterateRelation(PatternGraph * m1, PatternGraph * m2, PGRelation rel){
	PGRelation result;

	PGRelation::iterator rel_edgeItr = rel.begin();
	PGRelation::iterator rel_edgeEnd = rel.end();
	while (rel_edgeItr != rel_edgeEnd){

			ModifierNode * m1_relNode = (*rel_edgeItr).first;	// s
			ModifierNode * m2_relNode = (*rel_edgeItr).second; // s'

			std::map<std::string, ModifierNode *>::iterator m1_succItr = 
				m1_relNode->outEdges.begin();
			std::map<std::string, ModifierNode *>::iterator m1_succEnd = 
				m1_relNode->outEdges.end();

			bool allSuccsHaveCorrespondingEdge = true;
			//Here we are iterating through {s1|R(s,s1)} as per Clark pg. 180
			while(m1_succItr != m1_succEnd){
				bool thisSuccHasCorrespondingEdge = false;

				PGRelation::iterator rel_innerEdgeItr = rel.begin();
				PGRelation::iterator rel_innerEdgeEnd = rel.end();
				while (rel_innerEdgeItr != rel_innerEdgeEnd){
					ModifierNode * m1_innerRelNode = (*rel_edgeItr).first; //s1
					ModifierNode * m2_innerRelNode = (*rel_edgeItr).second; // s1'

					if (m2->hasEdge(m2_relNode,m2_innerRelNode)){ // R'(s',s1')
							thisSuccHasCorrespondingEdge = true;
					}
					++rel_innerEdgeItr;
				}
				allSuccsHaveCorrespondingEdge = 
					(thisSuccHasCorrespondingEdge ? allSuccsHaveCorrespondingEdge : false);
				++m1_succItr;
			}

			if (allSuccsHaveCorrespondingEdge){
				result.insert(*rel_edgeItr);
			}

			++rel_edgeItr;
	}

#if 0
	std::vector<PGEdge> m1_edges = m1->getEdgeList();
	std::vector<PGEdge>::iterator m1_edgeItr = m1_edges.begin();
	std::vector<PGEdge>::iterator	m1_edgeEnd = m1_edges.end();

	while(m1_edgeItr != m1_edgeEnd){
		ModifierNode * m1_src = (*m1_edgeItr)->first;
		ModifierNode * m1_dst = (*m1_edgeItr)->second;
		
		PGRelation::iterator rel_edgeItr = rel.begin();
		PGRelation::iterator rel_edgeEnd = rel.end();
		while (rel_edgeItr != rel_edgeEnd){
			ModifierNode * rel_src
			++rel_edgeItr;

			//There must be some node s2_dst s.t. m1_dst -> 
		}

		++m1_edgeItr;
	}
	#endif
}

std::vector<PGRelation> SimulationChecker::simulationFixpoint(PatternGraph * m1, PatternGraph * m2){
	std::vector<PGRelation> relationChain;

	PGRelation rel = generateBaseRelation(m1, m2);
	relationChain.push_back(rel);

	while(true){
		PGRelation rel_plus1 = iterateRelation(m1, m2, rel);
		if (rel.size() == rel_plus1.size()){
			//Reached a fixpoint
			break;
		} else {
			rel = rel_plus1;
			relationChain.push_back(rel);
		}
	}

	return relationChain;
}

PatternGraph * SimulationChecker::counterExample(PatternGraph * m1, PatternGraph * m2){
	std::vector<PGRelation> approximations = simulationFixpoint(m1, m2);

	
	{ //If the best approximation is a simulation relation, no counterexample possible
	if (isSimulated(m1, approximations.back())){ return NULL; }	
	}

{
	//At this point, we know that m1 is not simulated by m2. Figure out the
	//iteration at which each m1 root no longer had a corresponding root in m2 
	std::map<ModifierNode *, int> rootSeparation;
	NodeVec m1Roots = m1->roots();
	for (unsigned int i = 0 ; i < approximations.size() ; i++){
		NodeVec::iterator rootItr = m1Roots.begin();
		NodeVec::iterator rootEnd = m1Roots.end();
		PGRelation & rel = approximations[i];
		while(rootItr != rootEnd){
			ModifierNode * node = *rootItr;
			if (rootSeparation.find(node) == rootSeparation.end()){
				PGRelation::iterator relItr = rel.begin();
				PGRelation::iterator relEnd = rel.end();
				bool thisRootInSimulation = false;
				while (relItr != relEnd){
					if (relItr->first == node){
						if (relItr->second->isRoot()){
							thisRootInSimulation = true;
							break;
						}
					}
				}

				if (!thisRootInSimulation){ rootSeparation[node] = i; }
				++relItr;
			}
			++rootItr;
		}
	}
}

	PGRelation counter = matchingRoots(m1, m2);
	PGRelation frontier;
	PGRelation visited;
	frontier.insert(counter.begin(), counter.end());
	while(!frontier.empty()){
			PGRelation::iterator edgeItr = frontier.begin();
			PGEdge edge = *edgeItr;
			frontier.erase(edgeItr);
			if (visited.find(edge) == visited.end()){
				continue;
			} else {
				visited.insert(edge);
				counter.insert(edge);
			}
			ModifierNode * m1Node = edge.first;
			NodeVec m1Succs = m1Node->succs();
			ModifierNode * m2Node = edge.second;
			NodeVec m2Succs = m2Node->succs();
			PGRelation succPairs = matchingPropositions(&m1Succs, &m2Succs);
			frontier.insert(succPairs.begin(), succPairs.end());
	}
	return NULL;
}
