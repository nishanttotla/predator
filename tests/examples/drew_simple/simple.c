#include <stdlib.h>
extern int __VERIFIER_nondet_int(void);

int main() {
  int length = __VERIFIER_nondet_int();
  if (length < 1 || length > 1000) length = 5;
  int *arr = alloca(length);
  //if (!arr) return 0;
  if (arr != 0) return 0;
  int *a = arr;
	*a = 0;
  return 0;
}
