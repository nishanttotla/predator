#ifndef H_GUARD_SIMULATION_CHECKER_H
#define H_GUARD_SIMULATION_CHECKER_H

#include "oracleLookup.hh"
#include "heapOracle.hh"

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
#include <vector>

class PatternGraph;

class SimulationChecker{
	public:
		SimulationChecker();
		/**
			* Gives a counterExample that shows how a concrete subgraph of m2
			* that cannot be simulated by m1. Returns NULL if no such counterexample
			* exists (i.e. m1 simulates m2).
		**/	
		PatternGraph * counterExample(PatternGraph * m1, PatternGraph * m2);

	private:
		/**
			* Generates a sequence of relations that serve as iteratively better
			* approximations of how m1 simluates m2. Note that this chain may not
			* contain a valid simulation and only serves as a helper function
			* to counterExample. 
			* pass the result to counterExample
		**/
		std::vector<PGRelation> simulationFixpoint(PatternGraph * m1, PatternGraph * m2);
	
};

#endif
