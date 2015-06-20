#include <stdlib.h>
extern int __VERIFIER_nondet_int(void);

int main() {
  int length = __VERIFIER_nondet_int();
  if (length < 1 || length > 1000) length = 16;
  int *arr = alloca(length * sizeof(int));
  if (arr == 0) return 0;
	arr[0] = 0;
  return 0;
}
