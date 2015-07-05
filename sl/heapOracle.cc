#include "heapOracle.hh"

#include "config.h"
#include "symexec.hh"

#include <cl/cl_msg.hh>
#include <cl/cldebug.hh>
#include <cl/clutil.hh>
#include <cl/memdebug.hh>
#include <cl/storage.hh>

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

#include <queue>
#include <set>
#include <sstream>
#include <fstream>
#include <stdexcept>

#include <boost/foreach.hpp>
#include <boost/graph/graphviz.hpp>
#include <boost/algorithm/string.hpp>

bool operator<(const AtomicProposition& l, const AtomicProposition& r )
{
	return l.text < r.text;
}

bool operator==(const AtomicProposition& l, const AtomicProposition& r )
{
	return l.text == r.text;
}

HeapOracle::HeapOracle(std::string filename){
	readFile(filename);
}

void HeapOracle::readFile(std::string filename){
	fprintf(stderr, "[HeapOracle::readFile >>>] opening file %s\n", filename.c_str());
	std::ifstream infile(filename);
	if (!infile.is_open()){
		fprintf(stderr, "Could not open heap file %s\n", filename.c_str());
		return;
	}

	//start iterating through file, looking for a (program_point,Heap start)
	std::string line;
	while (getline(infile, line)){
		std::cerr << "process line " << line << "\n";
		std::stringstream lineAsStream(line);
		std::string ptString; 
		std::string dotFile;

		getline(lineAsStream, ptString, ',');
		getline(lineAsStream, dotFile);
		ProgramPoint * pt = ptFromLine(ptString);
		fprintf(stderr, "pt line is %s %d %d %d\n", 
			pt->loc->file, pt->loc->line, pt->loc->column, pt->visit);
		fprintf(stderr, "dot file is %s\n", dotFile.c_str());
		PatternGraph * mod = new PatternGraph(dotFile);
	}
	std::cerr << "[HeapOracle::readFile <<<]"  << std::endl;
}

void HeapOracle::step(SymHeap& sh, const CodeStorage::Insn * insn){

	std::ostringstream stream;

	stream << "[step] insn is " << *insn << " " << insn->loc << "\n";

	fprintf(stderr, "HeapOracle::step %s\n", stream.str().c_str());
}

PatternGraph * HeapOracle::modAt(const struct cl_loc * loc){
	return NULL;
}

PatternGraph::PatternGraph(std::string& dotFile){
	fprintf(stderr, "[PatternGraph::PatternGraph >>>] opening file %s\n", dotFile.c_str());
	std::ifstream infile(dotFile);
	if (!infile.is_open()){
		fprintf(stderr, "Could not open heap file %s\n", dotFile.c_str());
		return;
	}

	{ //Parse header
		std::string graphType;
		std::string graphName;
		std::string entry;
		getline(infile, graphType, ' ');
		getline(infile, graphName, ' ');
		getline(infile, entry, '\n');
		/*
		std::cerr << "type " << graphType 
							<< " name " << graphName 
							<< "entry " << entry << std::endl;
		*/
	}


	typedef std::vector< std::string > StringVec;

	std::string line;
	while (getline(infile, line)){
		boost::algorithm::trim(line);
		
		//directives
		if (line.find("label") == 0){
		} else if (line.find("clusterrank") == 0){
		} else if (line.find("labelloc") == 0){
		} else if (line.find("}") == 0){
		}
		//nodes and edges
		else {
			StringVec pieces;
			boost::algorithm::split( pieces, line, 
				boost::algorithm::is_any_of("["), 
				boost::algorithm::token_compress_on );
			//std::cerr << "un-IDed line: \"" << line << "\"\n";
			//std::cerr << "num pieces " << pieces.size() << "\n";

			if (pieces[0].find("->") != std::string::npos){
				//edge
				parseEdge(line);
			} else {
				//node
				parseNode(line);
			}
		}
	}
}

std::vector<PGEdge> PatternGraph::getEdgeList(){
	std::vector<PGEdge> result;

	std::vector<ModifierNode *>::iterator nItr = nodes.begin();
	std::vector<ModifierNode *>::iterator nEnd = nodes.end();
	while(nItr != nEnd){
		ModifierNode * n = *nItr;
		std::map<std::string, ModifierNode *>::iterator eItr = n->outEdges.begin();
		std::map<std::string, ModifierNode *>::iterator eEnd = n->outEdges.end();
		while (eItr != eEnd){
			ModifierNode * successor = eItr->second;
			PGEdge edge(n, successor);
			result.push_back(edge);
			++eItr;
		}
		++nItr;
	}
	return result;
}

void PatternGraph::parseNode(std::string& line){
	std::cerr << "node line is: \"" << line << "\"" << std::endl;
	StringVec pieces;
	boost::algorithm::split( pieces, line, 
		boost::algorithm::is_any_of("\""), 
		boost::algorithm::token_compress_on );
	std::string nodeName = pieces[1];	
	int annotationIdx = line.find("[");

	ModifierNode * node = new ModifierNode(nodeName);
	nodeMap[nodeName] = node;
	std::cerr << "[PatternGraph::parseNode] node " << nodeName << std::endl;
}

void PatternGraph::parseEdge(std::string& line){
	int annotationIdx = line.find("[");
	std::string lead = line.substr(0, annotationIdx);
	boost::algorithm::trim(lead);

	StringVec pieces;

	int arrowIdx = line.find("->");

	std::string from = lead.substr(0, arrowIdx);
	boost::algorithm::trim(from);
	boost::erase_all( from, "\"" );
	
	std::string to = lead.substr(arrowIdx + 2);
	boost::algorithm::trim(to);
	boost::erase_all( to, "\"" );

	std::cerr << "edge line is: \"" << line 
		<< "\" from/to is " << from 
		<< " to " << to << "\n";

	ModifierNode * fromNode = nodeMap[from];
	ModifierNode * toNode = nodeMap[to];
	std::cerr << "fromNode " << fromNode << " toNode " << toNode << std::endl;
	fromNode->outEdges[toNode->name] = toNode;
	toNode->inEdges[fromNode->name] = fromNode;
}
