#ifndef H_GUARD_PG_NODE_PREDICATE_H
#define H_GUARD_PG_NODE_PREDICATE_H

#include "heapOracle.hh"

class PGNodePredicate{
	virtual bool check(SymState * state);
};

class PredTrue{
	virtual bool check(SymState * state){ return true; }
};

#endif
