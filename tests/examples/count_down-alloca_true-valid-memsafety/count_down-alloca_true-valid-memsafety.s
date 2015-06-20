	.file	"count_down-alloca_true-valid-memsafety.c"
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
	subq	$32, %rsp
	movq	%fs:40, %rax
	movq	%rax, -8(%rbp)
	xorl	%eax, %eax
	call	__VERIFIER_nondet_int
	movl	%eax, -20(%rbp)
	cmpl	$0, -20(%rbp)
	jle	.L2
	movl	-20(%rbp), %eax
	cmpl	$536870910, %eax
	jbe	.L3
.L2:
	movl	$1, -20(%rbp)
.L3:
	movl	-20(%rbp), %eax
	cltq
	leaq	15(%rax), %rdx
	movl	$16, %eax
	subq	$1, %rax
	addq	%rdx, %rax
	movl	$16, %esi
	movl	$0, %edx
	divq	%rsi
	imulq	$16, %rax, %rax
	subq	%rax, %rsp
	movq	%rsp, %rax
	addq	$15, %rax
	shrq	$4, %rax
	salq	$4, %rax
	movq	%rax, -16(%rbp)
	cmpq	$0, -16(%rbp)
	jne	.L4
	movl	$0, %eax
	jmp	.L5
.L4:
	movl	$0, -32(%rbp)
	jmp	.L6
.L8:
	call	__VERIFIER_nondet_int
	movl	%eax, -24(%rbp)
	cmpl	$0, -24(%rbp)
	jns	.L7
	movl	$0, -24(%rbp)
.L7:
	movl	-32(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	movq	-16(%rbp), %rax
	addq	%rax, %rdx
	movl	-24(%rbp), %eax
	movl	%eax, (%rdx)
	addl	$1, -32(%rbp)
.L6:
	movl	-32(%rbp), %eax
	cmpl	-20(%rbp), %eax
	jl	.L8
	movl	$0, -28(%rbp)
	jmp	.L9
.L12:
	jmp	.L10
.L11:
	movl	-28(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	movq	-16(%rbp), %rax
	addq	%rdx, %rax
	movl	(%rax), %edx
	subl	$1, %edx
	movl	%edx, (%rax)
.L10:
	movl	-28(%rbp), %eax
	cltq
	leaq	0(,%rax,4), %rdx
	movq	-16(%rbp), %rax
	addq	%rdx, %rax
	movl	(%rax), %eax
	testl	%eax, %eax
	jg	.L11
	addl	$1, -28(%rbp)
.L9:
	movl	-28(%rbp), %eax
	cmpl	-20(%rbp), %eax
	jl	.L12
	movl	$0, %eax
.L5:
	movq	-8(%rbp), %rcx
	xorq	%fs:40, %rcx
	je	.L13
	call	__stack_chk_fail
.L13:
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE0:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.9.2-0ubuntu1~12.04) 4.9.2"
	.section	.note.GNU-stack,"",@progbits
