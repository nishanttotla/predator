#ifndef H_GUARD_HEAP_ORACLE_H
#define H_GUARD_HEAP_ORACLE_H

#include "oracleLookup.hh"

#include "fixed_point_proxy.hh"
#include "glconf.hh"
#include "sigcatch.hh"
#include "symabstract.hh"
#include "symcall.hh"
#include "symdebug.hh"
#include "symproc.hh"
#include "symstate.hh"
#include "symutil.hh"
#include "symtrace.hh"
#include "symheap.hh"
#include "util.hh"

#include <map>
#include <set>

class AtomicProposition;
class ModifierNode;

typedef std::vector< std::string > StringVec;
typedef std::set<AtomicProposition> APSet;
typedef std::map<std::string, ModifierNode *> NodeMap;

typedef std::pair<ModifierNode *, ModifierNode *> PGEdge;
typedef std::pair<PGEdge, bool> CheckedPGEdge;

typedef std::set<PGEdge> PGRelation;

typedef std::pair<ModifierNode *, ModifierNode *> NodePair;
typedef std::vector<NodePair *> PairVec;
typedef std::map<APSet, PairVec*> Coloring;

class AtomicProposition{
	public:
		std::string text;
};

extern bool operator<(const AtomicProposition& l, const AtomicProposition& r );
extern bool operator==(const AtomicProposition& l, const AtomicProposition& r );

class ModifierNode{
		public:
			std::string name;
			std::map<std::string, ModifierNode *> inEdges;
			std::map<std::string, ModifierNode *> outEdges;

			APSet APs;

			ModifierNode(std::string nameIn){
				this->name = nameIn;
			}

			bool samePropositions(std::set<AtomicProposition>& props);
};

//Pattern Graph
class PatternGraph{
	public: 
		PatternGraph(std::string& dotFile);
		std::vector<ModifierNode *> nodes;
		std::vector<PGEdge> getEdgeList();

		bool hasEdge(ModifierNode * src, ModifierNode * dst){
			std::map<std::string, ModifierNode *>::iterator itr = src->outEdges.begin();
			std::map<std::string, ModifierNode *>::iterator end = src->outEdges.end();
			while(itr != end){
				if (itr->second == dst){
					return true;
				}
				++itr;
			}
			return false;
		}
	private:	
		void parseNode(std::string& line);
		void parseEdge(std::string& line);
		NodeMap nodeMap;
};

class HeapOracle{
	public:
		class HeapInjection{
			public:
				struct cl_loc * loc;
				PatternGraph * mod;
		};

		HeapOracle(std::string filename);

		void step(SymHeap& sh, const CodeStorage::Insn * insn);
		PatternGraph * modAt(const struct cl_loc * loc);

	private:
		void readFile(std::string filename);
		void parseDotFile(std::string filename);
		PointMapper modAtProgramPoint;
};

#endif /* H_GUARD_HEAP_ORACLE_H */
