	.file	"simple-ext_1_true-valid-memsafety.c"
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
	movl	$0, -28(%rbp)
	jmp	.L5
.L8:
	movq	-24(%rbp), %rax
	movl	-28(%rbp), %edx
	movl	%edx, (%rax)
	movl	$16, %edi
	call	malloc
	movq	%rax, -8(%rbp)
	cmpq	$0, -8(%rbp)
	jne	.L6
	movl	$1, %edi
	call	exit
.L6:
	movq	-24(%rbp), %rax
	movq	-8(%rbp), %rdx
	movq	%rdx, 8(%rax)
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -24(%rbp)
	addl	$1, -28(%rbp)
.L5:
	cmpl	$29, -28(%rbp)
	jg	.L7
	movl	$0, %eax
	call	__VERIFIER_nondet_int
	testl	%eax, %eax
	jne	.L8
.L7:
	movq	-24(%rbp), %rax
	movl	-28(%rbp), %edx
	movl	%edx, (%rax)
	movq	-24(%rbp), %rax
	movq	$0, 8(%rax)
	movq	-16(%rbp), %rax
	movq	%rax, -24(%rbp)
	movl	$0, -28(%rbp)
	jmp	.L9
.L12:
	movq	-24(%rbp), %rax
	movl	(%rax), %eax
	cmpl	-28(%rbp), %eax
	je	.L10
.L11:
	movl	$0, %eax
	call	__VERIFIER_error
.L10:
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -24(%rbp)
	addl	$1, -28(%rbp)
.L9:
	cmpq	$0, -24(%rbp)
	jne	.L12
	movq	-16(%rbp), %rax
	movq	%rax, -24(%rbp)
	jmp	.L13
.L14:
	movq	-24(%rbp), %rax
	movq	8(%rax), %rax
	movq	%rax, -8(%rbp)
	movq	-24(%rbp), %rax
	movq	%rax, %rdi
	call	free
	movq	-8(%rbp), %rax
	movq	%rax, -24(%rbp)
.L13:
	cmpq	$0, -24(%rbp)
	jne	.L14
	movl	$0, %eax
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE1:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.9.2-0ubuntu1~12.04) 4.9.2"
	.section	.note.GNU-stack,"",@progbits
