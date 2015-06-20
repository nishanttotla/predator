#ifndef H_GUARD_ASSUMPTIONS_H
#define H_GUARD_ASSUMPTIONS_H

#include <string>
#include <iostream>

#include <cl/cl_msg.hh>
#include <cl/cldebug.hh>
#include <cl/clutil.hh>
#include <cl/memdebug.hh>
#include <cl/storage.hh>

#include "glconf.hh"
#include "prototype.hh"
#include "symabstract.hh"
#include "symbin.hh"
#include "symbt.hh"
#include "symgc.hh"
#include "symheap.hh"
#include "symplot.hh"
#include "symseg.hh"
#include "symstate.hh"
#include "symutil.hh"
#include "symtrace.hh"
#include "util.hh"

#include <map>
#include <vector>

class Assumptions{
	public:
		Assumptions(std::string filename);
		void step(const struct cl_loc *, const CodeStorage::Insn *insnCnd);

		//TValId valFromTrace(const struct cl_operand &op);
		//const struct cl_loc * stepBranch(const CodeStorage::Insn * insn);
		TValId stepBranch(const CodeStorage::Insn * insn);
	private:

		class Judgement{
			public:
				struct cl_loc * loc;
				TValId value;
				Judgement(std::string);
		};

		int judgementIdx;
		void readFile(std::string filename);
		std::map<std::string, std::vector<Judgement *> > pathAssumptions;
		std::vector<Judgement *> linearJudgements;
};

#endif /* H_GUARD_ASSUMPTIONS_H */
