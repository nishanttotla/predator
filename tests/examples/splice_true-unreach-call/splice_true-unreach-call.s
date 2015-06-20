	.file	"splice_true-unreach-call.c"
	.text
	.globl	exit
	.type	exit, @function
exit:
.LFB0:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	movl	%edi, -4(%rbp)
.L2:
	jmp	.L2
	.cfi_endproc
.LFE0:
	.size	exit, .-exit
	.globl	main
	.type	main, @function
main:
.LFB1:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$48, %rsp
	movl	$1, -44(%rbp)
	movl	$16, %edi
	call	malloc
	movq	%rax, -16(%rbp)
	cmpq	$0, -16(%rbp)
	jne	.L4
	movl	$1, %edi
	call	exit
.L4:
	movq	-16(%rbp), %rax
	movq	%rax, -24(%rbp)
	jmp	.L5
.L9:
	cmpl	$0, -44(%rbp)
	je	.L6
	movq	-24(%rbp), %rax
	movl	$1, (%rax)
	movl	$0, -44(%rbp)
	jmp	.L7
.L6:
	movq	-24(%rbp), %rax
	movl	$2, (%rax)
	movl	$1, -44(%rbp)
.L7:
	movl	$16, %edi
	call	malloc
	movq	%rax, -8(%rbp)
	cmpq	$0, -8(%rbp)
	jne	.L8
	movl	$1, %edi
	call	exit
.L8:
	movq	-24(%rbp), %rax
	movq	-8(%rbp), %rdx
	movq	%rdx, 8(%rax)
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -24(%rbp)
.L5:
	movl	$0, %eax
	call	__VERIFIER_nondet_int
	testl	%eax, %eax
	jne	.L9
	movq	-24(%rbp), %rax
	movl	$3, (%rax)
	movq	-16(%rbp), %rax
	movl	(%rax), %eax
	cmpl	$3, %eax
	jne	.L10
	movl	$0, %eax
	jmp	.L11
.L10:
	movl	$1, -44(%rbp)
	movq	$0, -40(%rbp)
	movq	$0, -32(%rbp)
	movq	-16(%rbp), %rax
	movq	%rax, -24(%rbp)
	jmp	.L12
.L14:
	movq	-24(%rbp), %rax
	movq	%rax, -8(%rbp)
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -24(%rbp)
	cmpl	$0, -44(%rbp)
	je	.L13
	movq	-8(%rbp), %rax
	movq	-40(%rbp), %rdx
	movq	%rdx, 8(%rax)
	movq	-8(%rbp), %rax
	movq	%rax, -40(%rbp)
	movl	$0, -44(%rbp)
	jmp	.L12
.L13:
	movq	-8(%rbp), %rax
	movq	-32(%rbp), %rdx
	movq	%rdx, 8(%rax)
	movq	-8(%rbp), %rax
	movq	%rax, -32(%rbp)
	movl	$1, -44(%rbp)
.L12:
	movq	-24(%rbp), %rax
	movl	(%rax), %eax
	cmpl	$3, %eax
	jne	.L14
	movq	-40(%rbp), %rax
	movq	%rax, -24(%rbp)
	jmp	.L15
.L18:
	movq	-24(%rbp), %rax
	movl	(%rax), %eax
	cmpl	$1, %eax
	je	.L16
	jmp	.L17
.L16:
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -24(%rbp)
.L15:
	cmpq	$0, -24(%rbp)
	jne	.L18
	movq	-32(%rbp), %rax
	movq	%rax, -24(%rbp)
	jmp	.L19
.L21:
	movq	-24(%rbp), %rax
	movl	(%rax), %eax
	cmpl	$2, %eax
	je	.L20
	jmp	.L17
.L20:
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -24(%rbp)
.L19:
	cmpq	$0, -24(%rbp)
	jne	.L21
	movl	$0, %eax
	jmp	.L11
.L17:
	movl	$0, %eax
	call	__VERIFIER_error
.L11:
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE1:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.9.2-0ubuntu1~12.04) 4.9.2"
	.section	.note.GNU-stack,"",@progbits
