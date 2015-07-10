struct node{
	int val;
	struct node * next;
};

extern int non_det();

int main() {
	struct node * n = malloc(sizeof(struct node));
	int v = 1;

	n->next = 0;
	n->val = v;
	while(non_det()){
		struct node * n2;
		n2 = malloc(sizeof(struct node));
		n2->val = v;
		n2->next = n;
		v = !v;
	}

	v = 1;
	while(n != 0){
		if (n->val == v){
			error: return 1;
		}
		n = n->next;
		v = !v;
	}
	return 0;
}
