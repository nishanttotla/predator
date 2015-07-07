#include "config.h"
#include "symplot.hh"

#include <cl/cl_msg.hh>
#include <cl/clutil.hh>
#include <cl/storage.hh>

#include "plotenum.hh"
#include "symheap.hh"
#include "sympred.hh"
#include "symseg.hh"
#include "util.hh"
#include "worklist.hh"

#include <cctype>
#include <fstream>
#include <iomanip>
#include <map>
#include <set>
#include <string>

#include <boost/foreach.hpp>

// /////////////////////////////////////////////////////////////////////////////
// implementation of HeapCrawler
class HeapCrawler {
    public:
        HeapCrawler(const SymHeap &sh, const bool digForward = true):
            sh_(const_cast<SymHeap &>(sh)),
            digForward_(digForward)
        {
        }

        bool /* anyChange */ digObj(const TObjId);
        bool /* anyChange */ digVal(const TValId);

        const TObjSet objs() const { return objs_; }
        const TValSet vals() const { return vals_; }

    private:
        void digFields(const TObjId of);
        void operate();

    private:
        SymHeap                    &sh_;
        WorkList<TValId>            wl_;
        bool                        digForward_;
        TObjSet                     objs_;
        TValSet                     vals_;
};
