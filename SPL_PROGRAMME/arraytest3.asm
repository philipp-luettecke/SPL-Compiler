	.import	printi
	.import	printc
	.import	readi
	.import	readc
	.import	exit
	.import	time
	.import	clearAll
	.import	setPixel
	.import	drawLine
	.import	drawCircle
	.import	_indexError

	.code
	.align	4

	.export	main
main:
	sub	$29,$29,20		; allocate frame
	stw	$25,$29,0		; save old frame pointer
	add	$25,$29,20		; setup new frame pointer
	add	$8,$25,-8
	add	$9,$0,0
	add	$10,$0,2
	bgeu	$9,$10,_indexError
	mul	$9,$9,4
	add	$8,$8,$9
	add	$9,$0,10
	stw	$9,$8,0
	add	$8,$25,-12
	add	$9,$25,-8
	add	$10,$0,0
	add	$11,$0,2
	bgeu	$10,$11,_indexError
	mul	$10,$10,4
	add	$9,$9,$10
	ldw	$9,$9,0
	stw	$9,$8,0
	add	$8,$25,-12
	add	$9,$25,-16
	ldw	$9,$9,0
	stw	$9,$8,0
	ldw	$25,$29,0		; restore old frame pointer
	add	$29,$29,20		; release frame
	jr	$31			; return
