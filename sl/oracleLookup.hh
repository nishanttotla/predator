#ifndef H_GUARD_ORACLE_LOOKUP_H
#define H_GUARD_ORACLE_LOOKUP_H

#include <map>
#include "symutil.hh"

class PatternGraph;

/*
A set of classes that allow for easy, abstract lookup of
HeapModifications at a certain program point
*/

class HeapInjection{
	public:
		struct cl_loc * loc;
		PatternGraph * mod;
};

class IterationMapper{
	public:
		PatternGraph * lookup(struct ProgramPoint * pt);
		bool put(struct ProgramPoint * pt, int itr);
	private:
		std::map<int, HeapInjection *> innerMap;
};

class CharMapper{
	public:
		PatternGraph * lookup(struct ProgramPoint * pt);
		bool put(struct ProgramPt * pt, PatternGraph * mod);
	private:
		std::map<int, IterationMapper*> innerMap;
};

class PointMapper{
	public:
		PatternGraph * lookup(struct ProgramPoint * pt);
		void put(struct ProgramPoint * pt, PatternGraph * mod);
	private:
		std::map<int, CharMapper*> innerMap;
};

#endif
