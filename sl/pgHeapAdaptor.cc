#include "pgHeapAdaptor.hh"
#include "heapCrawler.hh"

PGHeapAdaptor::PGHeapAdaptor(const SymHeap &sh) : heap(sh)
{
	std::cerr << "[PGHeapAdaptor::PGHeapAdaptor]\n" << std::endl;

	HeapCrawler crawler(heap);

	TObjList allObjs;
	sh.gatherObjects(allObjs);
	BOOST_FOREACH(const TObjId val, allObjs){
		crawler.digObj(val);
	}

	const TObjSet& objs = crawler.objs();
	const TValSet& vals = crawler.vals();
	data = new CrawlData(sh, objs, vals);

	BOOST_FOREACH(const TObjId obj, data->objs){
			FldList liveFields;
			sh.gatherLiveFields(liveFields, obj);

			//CompositeObj
			std::cerr << "[PGHeapAdaptor::PGHeapAdaptor] obj is " << obj << std::endl;
			TSizeRange sz = sh.objSize(obj);
			bool isValid = sh.isValid(obj);
			/*
			if (isProgramVar(sh.objStorClass(obj))){
				describeVar(plot, obj)
			}
			*/
				
			//Do the raw object
			//Do the uniform blocks
			//Do the fields
	}



}


