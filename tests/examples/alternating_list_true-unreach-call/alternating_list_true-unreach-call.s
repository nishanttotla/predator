	.file	"alternating_list_true-unreach-call.c"
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
	subq	$32, %rsp
	movl	$1, -28(%rbp)
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
	cmpl	$0, -28(%rbp)
	je	.L6
	movq	-24(%rbp), %rax
	movl	$1, (%rax)
	movl	$0, -28(%rbp)
	jmp	.L7
.L6:
	movq	-24(%rbp), %rax
	movl	$2, (%rax)
	movl	$1, -28(%rbp)
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
	movq	%rax, -24(%rbp)
	movl	$1, -28(%rbp)
	jmp	.L10
.L14:
	cmpl	$0, -28(%rbp)
	je	.L11
	movl	$0, -28(%rbp)
	movq	-24(%rbp), %rax
	movl	(%rax), %eax
	cmpl	$1, %eax
	je	.L12
	jmp	.L13
.L11:
	movl	$1, -28(%rbp)
	movq	-24(%rbp), %rax
	movl	(%rax), %eax
	cmpl	$2, %eax
	je	.L12
	jmp	.L13
.L12:
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -24(%rbp)
.L10:
	movq	-24(%rbp), %rax
	movl	(%rax), %eax
	cmpl	$3, %eax
	jne	.L14
	movl	$0, %eax
	jmp	.L16
.L13:
	movl	$0, %eax
	call	__VERIFIER_error
.L16:
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE1:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.9.2-0ubuntu1~12.04) 4.9.2"
	.section	.note.GNU-stack,"",@progbits
