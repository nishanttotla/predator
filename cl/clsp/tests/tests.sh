#!/usr/bin/env bash
export LANG=C
set -eu

HERE=$(dirname "${BASH_SOURCE[0]}")

TMP_ROOT=/tmp

CLSP_RUNNER=../clsp-run

DIFF="diff -q"
SIMDIFF=../simdiff
INSPECTDIFF=meld

SHOW_OK=1
INSPECT=0

SLINE=
for i in $(seq 0 63); do SLINE="-${SLINE}"; done
DLINE=
for i in $(seq 0 63); do DLINE="=${DLINE}"; done

declare -i CNT_OK=0
declare -i CNT_BAD=0

print_file () { printf "%-56s" "$1"; }
print_warn () { echo "warning: $2"; }

print_ok () {
    if [ $SHOW_OK -ne 0 ]; then
        printf "  \033[32mOK\033[0m\n"
    fi
}
print_similarity_ok () {
    if [ $SHOW_OK -ne 0 ]; then
        printf "\033[32m%s\033[0m\n" "$2"
    fi
}
#print_bad ()            { printf " \033[31mBAD\033[0m\n"; }
print_similarity_bad () { printf "\033[31m%s\033[0m\n" "$2"; }

ok ()      { let CNT_OK+=1;  print_file "$1"; print_ok  "$@"; }
sim_ok ()  { let CNT_OK+=1;  print_file "$1"; print_similarity_ok "$@"; }
#bad ()     { let CNT_BAD+=1; print_file "$1"; print_bad "$@"; }
sim_bad () { let CNT_BAD+=1; print_file "$1"; print_similarity_bad "$@"; }

warn () { print_file $1; print_warn "$@"; }
die ()  { ret=$?; printf "$@"; exit $ret; }

init () { make "${1:-files}"; }

# $1 = general options
# $2 = rel. path to input C file
# $3 = combined output/error stream file
# $4 = debug stream file
# $5 = warn stream file
run_clsp () {
    ${CLSP_RUNNER}                                                              \
        --fd-debug=4 --fd-warn=5 --fd-sp=3 --fd-cl=3 --fd-cl-debug=3 $1 -- "$2" \
        2>&1 3>/dev/null 4>"$4" 5>"$5" | tee "$3"
}

# $1 = general options
# $2 = rel. path to input C file
run_clsp_i () {
    ${CLSP_RUNNER} $1 -i -- "$2"
}

# $1 = file (already checked for existence)
do_interactive () {
    init "$1" || die "cannot initialize test"
    opts="$(grep "clsp-options:" "$1" | sed 's/.*: *\(.\+\)/\1/')"
    if run_clsp_i "${opts}" "$1"; then
        while true; do
            echo "is that interpretation okay?"
            read answer
            case "$answer" in
                y|yes)
                    dstfile=$(echo "$1" | sed 's/\.c//')
                    run_clsp "${opts}" "$1" /dev/null "${dstfile}.debug.ref" /dev/null
                    echo "confirmed"
                    break
                    ;;
                n|no)
                    echo "unconfirmed and cancelled"
                    #clean
                    break
                    ;;
            esac
        done
    else
        echo "make sure clsp does not exit with nonzero exit code first"
    fi
}

do_tests () {
    init || die "cannot initialize tests"

    tmpdir=$(mktemp --tmpdir="${TMP_ROOT}" --dir clsptestXXX)
    lastdir=

    echo $DLINE

    for src in $(find -name "*.debug.ref"|sed 's/\.\//|/1'|cut -d"|" -f2|sort); do
        dstdir="${tmpdir}/$(dirname "${src}")"
        if [ "${dstdir}" != "${lastdir}" ]; then
            echo $SLINE
            lastdir="${dstdir}"
        fi
        src=$(echo $src | sed 's/\.debug\.ref//')
        dstfile="${dstdir}/$(basename "${src}")"
        mkdir -p "${dstdir}" 2>/dev/null
        opts="$(grep "clsp-options:" "${src}.c" | sed 's/.*: *\(.\+\)/\1/')"
        limit="$(grep "simdiff-limit:" "${src}.c" | sed 's/.*: *\(.\+\)/\1/')"

        run_clsp "${opts}" "${src}.c" \
            "${dstfile}.outerr" "${dstfile}.debug" "${dstfile}.warn"

        ret=$?; [ $ret -ne 0 ] && warn "${src}" "unexpected exit code: $ret"
        [ -s "${dstfile}.outerr" ] && warn "${src}" "nonzero file ${dstfile}.outerr"
        [ -s "${dstfile}.warn" ] && warn "${src}" "nonzero file ${dstfile}.warn"

        if ${DIFF} "${src}.debug.ref" "${dstfile}.debug" >/dev/null; then
            ok "${src}"
        else
            sim=$(${SIMDIFF} "${src}.debug.ref" "${dstfile}.debug" "${limit}") \
                && sim_ok  "${src}" "${sim}"                                   \
                || sim_bad "${src}" "${sim}"
            [ $INSPECT -ne 0 ] \
                && $INSPECTDIFF "${src}.debug.ref" "${dstfile}.debug"
        fi
    done

    echo $DLINE

    printf "Good %55d\n" $CNT_OK
    printf "Bad  %55d\n" $CNT_BAD
    printf "SUM  %55d\n" $(($CNT_OK+$CNT_BAD))

    echo $DLINE
}


clean() {
    echo "cleaning"
    rm -rf -- ${TMP_ROOT}/clsptest* || :
}

case "${1:-""}" in
    "clean") $1;;
    "") do_tests;;
    *) init "$1" && [ -s "${1}" ] && do_interactive "$@" || echo "$0 [clean|<file>]"
esac