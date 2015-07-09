struct node{
	int val;
	struct node * next;
};

extern int non_det();
extern void * alloc;

int main() {
	int v = 1;
	struct node * n;
	while(non_det()){
		n = alloc();
		n->val = !v;
	}

	error:
		return 0;
}
