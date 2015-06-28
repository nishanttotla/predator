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

typedef std::vector< std::string > StringVec;

class ModifierNode{
		public:
			std::string name;
			std::map<std::string, ModifierNode *> inEdges;
			std::map<std::string, ModifierNode *> outEdges;

			ModifierNode(std::string nameIn){
				this->name = nameIn;
			}
};

//Pattern Graph
class HeapModifier{
	public: 
		HeapModifier(std::string& dotFile);
	private:	
		void parseNode(std::string& line);
		void parseEdge(std::string& line);
		std::vector<ModifierNode *> nodes;
		std::map<std::string, ModifierNode *> nodeMap;
};

class HeapOracle{
	public:
		class HeapInjection{
			public:
				struct cl_loc * loc;
				HeapModifier * mod;
		};

		HeapOracle(std::string filename);

		void step(SymHeap& sh, const CodeStorage::Insn * insn);
		HeapModifier * modAt(const struct cl_loc * loc);

	private:
		void readFile(std::string filename);
		void parseDotFile(std::string filename);
		PointMapper modAtProgramPoint;
};

#endif /* H_GUARD_HEAP_ORACLE_H */
