prog = test

test:
	-gcc-4.9 -fplugin=$(PREDATOR_ROOT)/sl_build/libsl.so \
	-S \
	-I$(PREDATOR_ROOT)/include/predator-builtins \
	-DPREDATOR \
	-fplugin-arg-libsl-dump-pp=linear_code.txt \
	-fplugin-arg-libsl-dump-types \
	-fplugin-arg-libsl-gen-dot=gen_dot.dot \
	-fplugin-arg-libsl-type-dot=type_graph.dot \
	-fplugin-arg-libsl-verbose=100 \
	$(prog).c > out.txt 2>&1 
	cat out.txt

clean:
	rm -f ./*.dot out.txt $(prog).s linear_code.txt
