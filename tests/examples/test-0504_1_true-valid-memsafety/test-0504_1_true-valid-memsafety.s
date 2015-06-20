	.file	"test-0504_1_true-valid-memsafety.c"
	.text
	.globl	main
	.type	main, @function
main:
.LFB0:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$64, %rsp
	movq	$0, -56(%rbp)
	movq	$0, -48(%rbp)
	movl	$0, -64(%rbp)
	movl	$0, -60(%rbp)
	jmp	.L2
.L10:
	addl	$1, -64(%rbp)
	movl	$40, %edi
	call	malloc
	movq	%rax, -32(%rbp)
	cmpq	$0, -32(%rbp)
	jne	.L3
	call	abort
.L3:
	movq	-32(%rbp), %rax
	movq	$0, 24(%rax)
	movq	-32(%rbp), %rax
	movq	$0, 32(%rax)
	movq	-32(%rbp), %rdx
	movq	-32(%rbp), %rax
	movq	%rdx, (%rax)
	movq	-32(%rbp), %rdx
	movq	-32(%rbp), %rax
	movq	%rdx, 8(%rax)
	movl	-64(%rbp), %eax
	leal	(%rax,%rax), %edx
	movq	-32(%rbp), %rax
	movl	%edx, 16(%rax)
	movq	$0, -24(%rbp)
	jmp	.L4
.L7:
	addl	$1, -60(%rbp)
	movl	$24, %edi
	call	malloc
	movq	%rax, -24(%rbp)
	cmpq	$0, -24(%rbp)
	jne	.L5
	call	abort
.L5:
	movq	-32(%rbp), %rax
	movq	(%rax), %rdx
	movq	-24(%rbp), %rax
	movq	%rdx, (%rax)
	movq	-24(%rbp), %rax
	movq	(%rax), %rax
	movq	-24(%rbp), %rdx
	movq	%rdx, 8(%rax)
	movq	-32(%rbp), %rdx
	movq	-24(%rbp), %rax
	movq	%rdx, 8(%rax)
	movl	-60(%rbp), %eax
	imull	-64(%rbp), %eax
	movl	%eax, %edx
	movq	-24(%rbp), %rax
	movl	%edx, 16(%rax)
	movq	-32(%rbp), %rax
	movq	-24(%rbp), %rdx
	movq	%rdx, (%rax)
	movq	$0, -24(%rbp)
.L4:
	cmpl	$2, -60(%rbp)
	jg	.L6
	call	__VERIFIER_nondet_int
	testl	%eax, %eax
	jne	.L7
.L6:
	movl	$0, -60(%rbp)
	cmpq	$0, -56(%rbp)
	jne	.L8
	movq	-32(%rbp), %rax
	movq	%rax, -56(%rbp)
	movq	-32(%rbp), %rax
	movq	%rax, -48(%rbp)
	jmp	.L2
.L8:
	movq	-48(%rbp), %rax
	movq	-32(%rbp), %rdx
	movq	%rdx, 24(%rax)
	movq	-32(%rbp), %rax
	movq	-48(%rbp), %rdx
	movq	%rdx, 32(%rax)
	movq	-32(%rbp), %rax
	movq	%rax, -48(%rbp)
.L2:
	cmpl	$4, -64(%rbp)
	jg	.L9
	call	__VERIFIER_nondet_int
	testl	%eax, %eax
	jne	.L10
.L9:
	jmp	.L11
.L14:
	subl	$1, -64(%rbp)
	movq	-56(%rbp), %rax
	movq	%rax, -16(%rbp)
	movq	-56(%rbp), %rax
	movq	24(%rax), %rax
	movq	%rax, -56(%rbp)
	movq	-16(%rbp), %rax
	movq	(%rax), %rax
	movq	%rax, -40(%rbp)
	jmp	.L12
.L13:
	movq	-40(%rbp), %rax
	movq	%rax, -8(%rbp)
	movq	-40(%rbp), %rax
	movq	(%rax), %rax
	movq	%rax, -40(%rbp)
	movq	-8(%rbp), %rax
	movl	16(%rax), %eax
	cmpl	$15, %eax
	jg	.L12
	movq	-8(%rbp), %rax
	movq	%rax, %rdi
	call	free
.L12:
	movq	-16(%rbp), %rax
	cmpq	-40(%rbp), %rax
	jne	.L13
	movq	-16(%rbp), %rax
	movq	%rax, %rdi
	call	free
.L11:
	cmpl	$0, -64(%rbp)
	jg	.L14
	movl	$0, %eax
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE0:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.9.2-0ubuntu1~12.04) 4.9.2"
	.section	.note.GNU-stack,"",@progbits
