#ifndef H_GUARD_SIMULATION_CHECKER_H
#define H_GUARD_SIMULATION_CHECKER_H

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

class SimulationChecker{
	public:
		SimulationChecker();
		PatternGraph * counterExample(PatternGraph * m1, PatternGraph * m2);
		bool simulates(PatternGraph * m1, PatternGraph * m2);
};

#endif
