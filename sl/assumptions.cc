#include "assumptions.hh"
#include "config.h"
#include "symexec.hh"

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
#include "util.hh"

#include <queue>
#include <set>
#include <sstream>
#include <fstream>
#include <stdexcept>

#include <boost/foreach.hpp>

Assumptions::Judgement::Judgement(std::string line){

		//Build a cl_loc for the location
		std::stringstream lineAsStream(line);
		std::string srcFile;
		std::string srcLine;
		std::string srcChar;
		std::string branch;

		getline(lineAsStream, srcFile, ':');
		getline(lineAsStream, srcLine, ':');
		getline(lineAsStream, srcChar, ':');
		getline(lineAsStream, branch, '\n');
		std::cerr << "branch " << branch << "\n";

	unsigned int lineVal = atoi(srcLine.c_str());
	unsigned int col = atoi(srcChar.c_str());
	TValId branchVal;
	if (branch.compare("true") == 0){
		branchVal = VAL_TRUE;
	} else if (branch.compare("false") == 0) {
		branchVal = VAL_FALSE;
	} else {
		fprintf(stderr, "Unknown branch val %s\n", branch.c_str());
		//branchVal = VAL_UNKNOWN;
	}

	this->loc = new struct cl_loc();
	this->loc->file = srcFile.c_str();
	this->loc->line = lineVal;
	this->loc->column = col;
	this->value = branchVal;
}

Assumptions::Assumptions(std::string filename){

	readFile(filename);
	judgementIdx = 0;

	return;
}

void Assumptions::readFile(std::string filename){
	printf("[Assumptions::readFile >>>] opening file %s\n", filename.c_str());

  std::ifstream infile (filename);
  if (!infile.is_open()){
		fprintf(stderr, "Could not open assumptions file %s\n", filename.c_str());
		return;
	}

	std::string line;
	while ( getline (infile,line) )
	{
		std::cerr << "process line " << line << "\n";
		Judgement * jdg = new Judgement(line);
		linearJudgements.push_back(jdg);
	}
	infile.close();
}

/*
TValId Assumptions::valFromTrace(const CodeStorage::Insn * insn){

	std::ostringstream stream;
	const struct cl_loc * loc = &insn->loc;
	stream << "[valFromTrace] insn " << *loc << " (" <<  *insn << ")\n";
	fprintf(stderr, "%s\n", stream.str().c_str());
	
	return VAL_FALSE;
}
*/

TValId Assumptions::stepBranch(const CodeStorage::Insn * insn){
	std::cerr << "[stepBranch] advancing from " << judgementIdx << std::endl;
	if (judgementIdx >= linearJudgements.size()){
		return VAL_INVALID;
	}

	Judgement * nextJudgement = linearJudgements[judgementIdx];
	judgementIdx++;

	return nextJudgement->value;
}

//void Assumptions::step(const struct &cl_loc){
void Assumptions::step(const struct cl_loc * loc, 
		const CodeStorage::Insn *insnCnd){

	std::ostringstream stream;
	stream << "step from " << *loc << "(" << *insnCnd << ")" << "\n";

	const CodeStorage::Block * trueBlock = insnCnd->targets[0];
	const CodeStorage::Block * falseBlock = insnCnd->targets[1];

	const CodeStorage::Insn * trueInsn = trueBlock->operator[](0);
	const CodeStorage::Insn * falseInsn = falseBlock->operator[](0);

	stream << "\ttrue insn " << trueInsn->loc << "(" <<  *trueInsn << ")\n";
	stream << "\tfalse insn " << falseInsn->loc << "(" <<  *falseInsn << ")\n";
	printf("%s\n", stream.str().c_str());
	return;
}
