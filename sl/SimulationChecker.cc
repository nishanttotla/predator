#include "SimulationChecker.hh"
#include "heapOracle.hh"

#include <map>
#include <utility>

class SimulationRelation{
};

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

bool SimulationChecker::simulates(PatternGraph * m1, PatternGraph * m2){
	PGRelation rel = generateBaseRelation(m1, m2);
	while(true){
		PGRelation rel_plus1 = iterateRelation(m1, m2, rel);
		if (rel.size() == rel_plus1.size()){
			//Reached a fixpoint
			break;
		} else {
			rel = rel_plus1;
		}
	}

	//Test if each root of is in the simuatlion
	bool eachRootInSimulation = true;
	std::vector<ModifierNode *>::iterator m1_nodeItr = m1->nodes.begin();
	std::vector<ModifierNode *>::iterator m1_nodeEnd = m1->nodes.end();
	while(m1_nodeItr != m1_nodeEnd){
		ModifierNode * node = *m1_nodeItr;
		if (node->inEdges.empty()){
			//TODO: ensure that there is a corresponding root in rel

			PGRelation::iterator relItr = rel.begin();
			PGRelation::iterator relEnd = rel.end();
			bool thisRootInSimulation = false;
			while (relItr != relEnd){
				if (relItr->first == node){
					if (relItr->second->inEdges.empty()){
						thisRootInSimulation = true;
						break;
					}
				}
				++relItr;
			}
			eachRootInSimulation =
				(thisRootInSimulation ? eachRootInSimulation : false);
		}
	}

	return eachRootInSimulation;
}

PatternGraph * SimulationChecker::counterExample(PatternGraph * m1, PatternGraph * m2){
	std::vector<ModifierNode *>::iterator m1_nodeItr = m1->nodes.begin();
	std::vector<ModifierNode *>::iterator m1_nodeEnd = m1->nodes.end();

	std::vector<ModifierNode *>::iterator m2_nodeItr = m2->nodes.begin();
	std::vector<ModifierNode *>::iterator m2_nodeEnd = m2->nodes.end();

	Coloring worklist;

	{
		while (m1_nodeItr != m1_nodeEnd){

			m2_nodeItr = m2->nodes.begin();
			while(m2_nodeItr != m2_nodeEnd){

				APSet& props = (*m1_nodeItr)->APs;
				APSet& otherProps = (*m2_nodeItr)->APs;

				if (props == otherProps){
					NodePair * pair = new NodePair(*m1_nodeItr, *m2_nodeItr);

					APSet& props = pair->first->APs;

					Coloring::iterator itr = worklist.find(props);
					if (itr == worklist.end()){
						worklist[props] = new PairVec();
					}
					PairVec * pairs = worklist[props];
					pairs->push_back(pair);
				} else {
					//Propositions don't match, no need to group them
				}
				m2_nodeItr++;
			}
			m1_nodeItr++;
		}
	}

	//At this point all (n1,n2) pairs are clustered. 
	// Now check to see if m1 simulates m2

	std::vector<PGEdge> m2_Edges = m2->getEdgeList();
	std::vector<PGEdge> m1_Edges = m1->getEdgeList();
	std::vector<PGEdge> m2_uncheckedEdges = m2->getEdgeList();
	bool hasSimulation = true;

	std::vector<std::pair<PGEdge, PGEdge>> matching;
	while(!m2_uncheckedEdges.empty()){
		PGEdge m2_uncheckedEdge = m2_uncheckedEdges.back();
		m2_uncheckedEdges.pop_back();
		//std::vector<PGEdge> candidateEdges = candidateMatches(m1_Edges);
		std::vector<PGEdge> candidateEdges;
		candidateEdges = candidateMatches(m2_uncheckedEdge, m1_Edges);
		PGEdge m1_candidateEdge = candidateEdges.back();
		if (candidateEdges.empty()){
			hasSimulation = false;
			break;
		} else {
			matching.push_back(std::pair<PGEdge, PGEdge>(m2_uncheckedEdge, m1_candidateEdge));
		}
	}
	
	//TODO: need to actually generate a counterexample now
	/*
	//Add all m1 nodes to the coloring
	while (m1_nodeItr != m1_nodeEnd){
		ModifierNode * m1_node = *m1_nodeItr;
		APSet& APs = m1_node->APs;

		AtomicProposition ap;
		std::map<AtomicProposition, int> apMap;


		Coloring::iterator found = coloring.find(APs);
		if (coloring.find(aps) == coloring.end()){
			coloring[aps] = new std::vector<modifiernode *>();
		}

		m1_nodeItr++;
	}
	*/
	return false;
}
