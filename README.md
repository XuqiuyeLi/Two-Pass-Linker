# Note
	- the symbol table is implemented with hashmap, so the sequence of the output is not sorted
	- the Error messages are not displayed exactly in the end, some may appear in the beginning of output


# How to run
1. Before compiling please have the TwoPassLinker.java in the same directory as any input files that will be utilized.
2. Compile using `javac TwoPassLinker.java` in terminal
3. Run with `java TwoPassLinker`
4. The program will ask you to type the name of an input file you would like to run with.
5. The program will run and display the output on the terminal.	 


# Errors
	- If a symbol is multiply defined, print an error message and use the value given in the last definition.
	- If a symbol is used but not defined, print an error message and use the value 111.
	- If a symbol is defined but not used, print a warning message.
	- If an absolute address exceeds the size of the machine, print an error message and use the largest legal value.
	- If multiple symbols are listed as used in the same instruction, print an error message and ignore all but the last usage given.
	- If a type R address in the list of Text entries exceeds the size of the module, treat the address as 0 (and relocate it since it is an R type address).