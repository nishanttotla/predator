#include "pgNodePredicate.hh"

void PatternPredicate::PatternPredicate :
		AtomicProposition(PATTERN){ }

bool PatternPredicate::evalOn(AtomicProposition& other){
	if (other.type == PATTERN){
		PatternPredicate& otherPred = (PatternPredicate&)other;
		return pred.text == otherPred.text;
	} else if (other.type == HEAP){
		return concreteMatch((HeapNodeAdapter)otherPred);
	} else {
		return false;
	}
}

HeapNodeAdapter::HeapNodeAdapter() : AtomicProposition(HEAP){ }
