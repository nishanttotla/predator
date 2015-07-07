#include "oracleLookup.hh"
#include <map>

#include <cl/cl_msg.hh>
#include <cl/cldebug.hh>
#include <cl/clutil.hh>
#include <cl/memdebug.hh>
#include <cl/storage.hh>
#include "symutil.hh"

PatternGraph * PointMapper::lookup(struct ProgramPoint * pt){
	const struct cl_loc * loc = pt->loc;

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

bool PointMapper::put(struct ProgramPoint * pt, PatternGraph * mod){
	const struct cl_loc * loc = pt->loc;

	std::map<int, CharMapper *>::iterator itr = innerMap.find(loc->line);
	if (itr == innerMap.end()){
		innerMap[loc->line] = new CharMapper();
	}
	return innerMap[loc->line]->put(pt, mod);
}

PatternGraph * CharMapper::lookup(struct ProgramPoint * pt){
	const struct cl_loc * loc = pt->loc;

	std::map<int, IterationMapper *>::iterator itr = innerMap.find(loc->column);
	if (itr == innerMap.end()){
		return NULL;
	}
	IterationMapper * m = itr->second;
	return m->lookup(pt);
}

bool CharMapper::put(struct ProgramPoint * pt, PatternGraph * mod){
	const struct cl_loc * loc = pt->loc;

	std::map<int, IterationMapper *>::iterator itr = innerMap.find(loc->column);
	if (itr == innerMap.end()){
		innerMap[loc->column] = new IterationMapper();
	}
	return innerMap[loc->column]->put(pt, mod);
}

PatternGraph * IterationMapper::lookup(ProgramPoint * pt){
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

bool IterationMapper::put(struct ProgramPoint * pt, PatternGraph * mod){
	std::map<int, HeapInjection *>::iterator itr = innerMap.find(pt->visit);
	HeapInjection * inj  = new HeapInjection();
	inj->mod = mod;
	innerMap[pt->visit] = inj;
	return (itr == innerMap.end());
}
