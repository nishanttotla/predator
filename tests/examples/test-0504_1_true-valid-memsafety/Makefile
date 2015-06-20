prog = test-0504_1_true-valid-memsafety

test:
	-gcc-4.9 -fplugin=$(PREDATOR_ROOT)/predatorGit/sl_build/libsl.so \
	-S \
	-I$(PREDATOR_ROOT)/predatorGit/include/predator-builtins \
	-DPREDATOR \
	-fplugin-arg-libsl-dump-pp=linear_code.txt \
	-fplugin-arg-libsl-dump-types \
	-fplugin-arg-libsl-gen-dot=gen_dot.dot \
	-fplugin-arg-libsl-type-dot=type_graph.dot \
	-fplugin-arg-libsl-verbose=100 \
	$(prog).c > out.txt 2>&1 
	cat out.txt

clean:
	rm ./*.dot out.txt
