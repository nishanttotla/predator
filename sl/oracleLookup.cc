#include "oracleLookup.hh"
#include <map>

#include <cl/cl_msg.hh>
#include <cl/cldebug.hh>
#include <cl/clutil.hh>
#include <cl/memdebug.hh>
#include <cl/storage.hh>
#include "symutil.hh"

HeapModifier * PointMapper::lookup(struct ProgramPoint * pt){
	struct cl_loc * loc = pt->loc;

	std::map<int, CharMapper *>::iterator itr = innerMap.find(loc->line);
	if (itr == innerMap.end()){
		return NULL;
	}

	CharMapper * m = itr->second;
	if (m == NULL){
		return NULL;
	}
	return m->lookup(pt);
}

void PointMapper::put(struct ProgramPoint * pt, HeapModifier * mod){
	
}

HeapModifier * CharMapper::lookup(struct ProgramPoint * pt){
	struct cl_loc * loc = pt->loc;

	std::map<int, IterationMapper *>::iterator itr = innerMap.find(loc->column);
	if (itr == innerMap.end()){
		return NULL;
	}
	IterationMapper * m = itr->second;
	return m->lookup(pt);
}

HeapModifier * IterationMapper::lookup(ProgramPoint * pt){
	std::map<int, HeapInjection *>::iterator itr = innerMap.find(pt->visit);
	if (itr == innerMap.end()){
		return NULL;
	}

	HeapInjection * m = itr->second;
	if (m == NULL){
		return NULL;
	}
	return m->mod;
}
