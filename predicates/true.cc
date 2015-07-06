#include "../sl/symheap.hh"
#include "../sl/pgNodePredicate.hh"

extern "C"{
	bool pgcheckTrue(SymState * state){
		return true;
	}
};
