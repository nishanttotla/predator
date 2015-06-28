#include <stdlib.h>
extern int __VERIFIER_nondet_int(void);

int main() {
  int num = __VERIFIER_nondet_int();
  if (num > 5){
		int * i = NULL;
		*i = 4;
	}
  return 0;
}
