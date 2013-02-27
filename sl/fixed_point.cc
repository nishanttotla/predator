/*
 * Copyright (C) 2013 Kamil Dudka <kdudka@redhat.com>
 *
 * This file is part of predator.
 *
 * predator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * predator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with predator.  If not, see <http://www.gnu.org/licenses/>.
 */

#include "config.h"
#include "fixed_point.hh"

#include "cont_shape.hh"
#include "symplot.hh"
#include "symstate.hh"
#include "symtrace.hh"
#include "worklist.hh"

#include <cl/cl_msg.hh>
#include <cl/cldebug.hh>
#include <cl/storage.hh>

#include <fstream>
#include <map>

#include <boost/foreach.hpp>

namespace FixedPoint {

typedef const struct cl_loc                        *TLoc;
typedef const CodeStorage::Fnc                     *TFnc;
typedef int                                         TFncUid;
typedef std::map<TFncUid, TFnc>                     TFncMap;

typedef const CodeStorage::ControlFlow             &TControlFlow;
typedef const CodeStorage::Block                   *TBlock;
typedef WorkList<TBlock>                            TWorkList;

typedef std::map<TInsn, SymStateWithJoin>           TStateMap;

inline bool isTransparentInsn(const TInsn insn)
{
    const enum cl_insn_e code = insn->code;
    switch (code) {
        case CL_INSN_COND:
        case CL_INSN_JMP:
            return true;

        default:
            return false;
    }
}

class TraceIndex {
    public:
        void indexTraceOf(const SymHeap *sh);
        const SymHeap* nearestPredecessorOf(const SymHeap *sh) const;

    private:
        typedef std::map<const Trace::Node *, const SymHeap *> TLookup;
        TLookup lookup_;
};

void TraceIndex::indexTraceOf(const SymHeap *sh)
{
    const Trace::Node *tr = sh->traceNode();

    // we should never change the target heap of an already indexed trace node
    CL_BREAK_IF(hasKey(lookup_, tr) && lookup_[tr] != sh);

    lookup_[tr] = sh;
}

const SymHeap* TraceIndex::nearestPredecessorOf(const SymHeap *sh) const
{
    const Trace::Node *tr = sh->traceNode();

    while (0U < tr->parents().size()) {
        // TODO: handle trace nodes with more than one parent!
        tr = tr->parent();

        // check the current trace node
        const TLookup::const_iterator it = lookup_.find(tr);
        if (it == lookup_.end())
            continue;

        // found!
        const SymHeap *shPred = it->second;
        CL_BREAK_IF(shPred->traceNode() != tr);
        return shPred;
    }

    return /* not found */ 0;
}

struct StateByInsn::Private {
    TFncMap             visitedFncs;
    TStateMap           stateByInsn;
};

StateByInsn::StateByInsn():
    d(new Private)
{
}

StateByInsn::~StateByInsn()
{
    delete d;
}

bool /* any change */ StateByInsn::insert(const TInsn insn, const SymHeap &sh)
{
    SymStateWithJoin &state = d->stateByInsn[insn];

    if (!state.size()) {
        // update the map of visited functions
        const TFnc fnc = fncByCfg(insn->bb->cfg());
        const TFncUid uid = uidOf(*fnc);
        d->visitedFncs[uid] = fnc;
    }

    return state.insert(sh, /* allowThreeWay */ false);
}

struct PlotData {
    std::ostream       &out;
    TStateMap          &stateByInsn;
    std::string         name;

    PlotData(
            std::ostream           &out_,
            TStateMap              &stateByInsn_,
            const std::string      &name_):
        out(out_),
        stateByInsn(stateByInsn_),
        name(name_)
    {
    }
};

#define QUOT(what) "\"" << what << "\""
#define INSN(insn) QUOT("ins" << insn)
#define SHID(sh) QUOT("sh" << sh)
#define DOT_LINK(to) "\"" << to << ".svg\""

void plotInsn(PlotData &plot, const TInsn insn)
{
    // open cluster
    plot.out << "subgraph \"cluster" << insn << "\" {\n\tlabel=\"\"\n";

    // plot the root node
    plot.out << INSN(insn) << " [label=" << QUOT(*insn) << ", shape=box];\n";

    const SymState &state = plot.stateByInsn[insn];

    // XXX: detect container shapes
    ContShape::TShapeListByHeapIdx contShapes;
    ContShape::detectContShapes(&contShapes, state);

    const int cntHeaps = state.size();
    for (int i = cntHeaps - 1; 0 <= i; --i) {
        const SymHeap &sh = state[i];
        const TShapeList &shapeList = contShapes[i];

        TIdSet contShapeIds;
        BOOST_FOREACH(const Shape &shape, shapeList) {
            TObjSet contShapeObjs;
            objSetByShape(&contShapeObjs, sh, shape);
            BOOST_FOREACH(const TObjId obj, contShapeObjs)
                contShapeIds.insert(static_cast<int>(obj));
        }

        // plot the shape graph
        std::string shapeName;
        plotHeap(sh, plot.name + "-sh", /* loc */ 0, &shapeName, &contShapeIds);

        // plot the link to shape
        plot.out << SHID(&sh) << " [label=\"sh #" << i
            << "\", URL=" << DOT_LINK(shapeName);

        if (!shapeList.empty())
            plot.out << ", color=red, penwidth=3.0";

        plot.out << "];\n";
    }

    // close cluster
    plot.out << "}\n";
}

void plotInsnWrap(PlotData &plot, const TInsn insn, const TInsn last)
{
    TInsn src = insn;
    if (isTransparentInsn(insn))
        // look through!
        src = last;
    else
        plotInsn(plot, insn);

    // for terminal instructions, plot the outgoing edges
    const unsigned cntTargets = insn->targets.size();
    for (unsigned i = 0; i < cntTargets; ++i) {
        const TBlock bb = insn->targets[i];

        const char *label = "";
        if (CL_INSN_COND == insn->code)
            label = (!i) ? "T" : "F";

        plot.out << INSN(src) << " -> " << INSN(bb->front())
            << " [label=" << QUOT(label) << "];\n";
    }
}

void plotInnerEdge(PlotData &plot, const TInsn last, const TInsn insn)
{
    if (isTransparentInsn(insn))
        return;

    plot.out << INSN(last) << " -> " << INSN(insn) << ";\n";
}

/// traverse all basic blocks of the control-flow in a predictable order
void plotCfg(PlotData &plot, const TControlFlow cfg)
{
    TBlock bb = cfg.entry();
    TWorkList wl(bb);
    while (wl.next(bb)) {
        const TInsn entry = bb->front();
        TInsn last = entry;

        BOOST_FOREACH(const TInsn insn, *bb) {
            plotInsnWrap(plot, insn, last);

            if (insn != entry)
                plotInnerEdge(plot, last, insn);

            last = insn;
        }

        BOOST_FOREACH(const TBlock bbNext, bb->back()->targets)
            wl.schedule(bbNext);
    }
}

void plotFnc(const TFnc fnc, TStateMap &stateByInsn, const TraceIndex &trIndex)
{
    const std::string fncName = nameOf(*fnc);
    std::string plotName("fp-");
    plotName += fncName;

    // create a dot file
    const std::string fileName(plotName + ".dot");
    std::fstream out(fileName.c_str(), std::ios::out);
    if (!out) {
        CL_ERROR("unable to create file '" << fileName << "'");
        return;
    }

    // open graph
    out << "digraph " << QUOT(plotName)
        << " {\n\tlabel=<<FONT POINT-SIZE=\"36\">" << fncName
        << "()</FONT>>;\n\tclusterrank=local;\n\tlabelloc=t;\n";

    // plot the body
    PlotData plot(out, stateByInsn, plotName);
    plotCfg(plot, fnc->cfg);

    // plot trace edges
    BOOST_FOREACH(TStateMap::const_reference insItem, stateByInsn) {
        const TInsn insn = insItem.first;
        if (isTransparentInsn(insn))
            continue;

        BOOST_FOREACH(const SymHeap *sh, /* SymState */ insItem.second) {
            const SymHeap *shPred = trIndex.nearestPredecessorOf(sh);
            if (shPred)
                plot.out << SHID(shPred) << " -> " << SHID(sh) << ";\n";
        }
    }

    // close graph
    out << "}\n";
    if (!out)
        CL_ERROR("unable to write file '" << fileName << "'");
    out.close();
}

void StateByInsn::plotAll()
{
    // build trace index
    TraceIndex trIndex;
    BOOST_FOREACH(TStateMap::const_reference insItem, d->stateByInsn) {
        const TInsn insn = insItem.first;
        if (isTransparentInsn(insn))
            continue;

        const SymState &state = insItem.second;
        BOOST_FOREACH(const SymHeap *sh, state)
            trIndex.indexTraceOf(sh);
    }

    BOOST_FOREACH(TFncMap::const_reference fncItem, d->visitedFncs) {
        const TFnc fnc = fncItem.second;
        const TLoc loc = locationOf(*fnc);
        CL_NOTE_MSG(loc, "plotting fixed-point of " << nameOf(*fnc) << "()...");

        plotFnc(fnc, d->stateByInsn, trIndex);
    }
}

} // namespace FixedPoint
