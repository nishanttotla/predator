#ifndef H_GUARD_PG_HEAP_ADAPTOR_H
#define H_GUARD_PG_HEAP_ADAPTOR_H

#include "heapOracle.hh"

class PGHeapAdaptor : PatternGraph{
	const SymHeap& heap;

	PGHeapAdaptor(const SymHeap &sh);
};

#endif
