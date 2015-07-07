#ifndef H_GUARD_PG_HEAP_ADAPTOR_H
#define H_GUARD_PG_HEAP_ADAPTOR_H

#include "heapOracle.hh"

class CrawlData{
	public:
		const SymHeap& sh;
		const TObjSet& objs;
		const TValSet& vals;

		CrawlData(const SymHeap& shIn, const TObjSet& objsIn, const TValSet& valsIn)
		: sh(shIn), objs(objsIn), vals(valsIn)
		{ }
};

class PGHeapAdaptor : PatternGraph{
	const SymHeap& heap;
	CrawlData * data;

	PGHeapAdaptor(const SymHeap &sh);
};

#endif
