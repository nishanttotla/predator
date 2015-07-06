#include "pgHeapAdaptor.hh"

PGHeapAdaptor::PGHeapAdaptor(const SymHeap &sh) : heap(sh)
{
	std::cerr << "[PGHeapAdaptor::PGHeapAdaptor]\n" << std::endl;
}
