#ifndef H_GUARD_PG_NODE_PREDICATE_H
#define H_GUARD_PG_NODE_PREDICATE_H

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

class HeapNodePredicate;

enum APType { PATTERN, HEAP };

class AtomicProposition{
	public:
		APType type;

		AtomicProposition(APType typeIn) : type(typeIn){ }
};

class PatternPredicate : AtomicProposition{
	public:
		std::string text;
		PatternPredicate();
		virtual bool evalOn(AtomicProposition& other);

		//Implemented by subclasses
		virtual bool concreteMatch(HeapNodePredicate& other);
};

class HeapNodeAdapter : AtomicProposition{
	public:
		HeapNodeAdapter() : AtomicProposition(HEAP){}
};

class PGNodePredicate{
	virtual bool check(SymState * state);
};

class PredTrue{
	virtual bool check(SymState * state){ return true; }
};

#endif
