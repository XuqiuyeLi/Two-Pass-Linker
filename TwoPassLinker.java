/*
 * Implement a 2-pass linker.
 *
 * The input consists of a series of object modules, each of which contains three parts: 
 * a definition list, a use list, and program text. 
 * Preceding all the object modules is a single integer giving the number of modules present.
 *
 * @author Summer Li
 */


import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


public class TwoPassLinker{
	// store symbol table in a hashmap
	private static Map<String, Integer> symbolTable = new HashMap<String, Integer>();
	// check if a symbol is used
	private static Map<String, Boolean> symbolUsed = new HashMap<String, Boolean>();
	// base addresses for all modules
	private static List<Integer> baseAddresses = new ArrayList<Integer>();
	private static List<Integer> finalAddr = new ArrayList<Integer>();


	public static void main(String[] args) {
         
         System.out.println("Enter the input file name: ");
		 Scanner scanner = new Scanner(System.in);
         String filename = scanner.nextLine();
         Scanner input1 = readFile(filename);
         passOne(input1);
         Scanner input2 = readFile(filename);
	 	 passTwo(input2);
	 }


	private static Scanner readFile(String filename){
	 	try{
			Scanner input = new Scanner(new BufferedReader(new FileReader(filename)));
			return input;
		}
		catch(Exception ex) {
			System.out.println("Error reading file!");
			System.exit(0);
		}
		return null;
	}

	/*
	 * determines the base address for each module and 
	 * storing the absolute address for each external symbol in the symbol table.
	 */
	private static void passOne(Scanner input){
		// get the total number of modules
		int totalMod = input.nextInt();
		// the first module starts at 0
		int currentMod = 0;
		// read each symbol and add to the symbol table
		for(int i = 0; i < totalMod; i++){
			// total symbols of each module
			int totalSymbols = input.nextInt();
			// processing definition list - finish adding symbols of current module
			for(int j = 0; j < totalSymbols; j++){
				String symbol = input.next();
				int offset = input.nextInt();
				// if the table already contains the symbol, print error message
				if(symbolTable.containsKey(symbol)){
					System.out.println();
					System.out.println("Error!");
					System.out.print(symbol);
					System.out.println(" is multiply defined; last value used.");
					symbolTable.put(symbol, currentMod + offset);
				}
				else{
					// add the new symbol to the table
					symbolTable.put(symbol, currentMod + offset);
					symbolUsed.put(symbol, false);
				}
			}
			// skip the use list which is not to be read in pass one
			int count = input.nextInt();
			for(int x = 0; x < count; x++){
				input.next();
				input.nextInt();
			}
			// get the length of the current module & base address for next module
			baseAddresses.add(currentMod);
			int length = input.nextInt();
			currentMod += length;

			// skip all tokens of the program text until end of current module
			for(int y = 0; y < length; y++){
				input.next();
				input.nextInt();
			}
		}

		// print the symbol table
		printTable(symbolTable);
		input.close();
	}

	/*
	 * uses the base addresses and the symbol table computed in pass one 
	 * to generate the actual output by relocating relative addresses and resolving external references.
	 */
	private static void passTwo(Scanner input){
		int totalMod = input.nextInt();
		// the first module starts at 0
		int currentMod = 0;
		System.out.println("  Memory Map");
		for(int i = 0; i < totalMod; i++){
			// skip the definition list already processed in pass one
			int totalSymbols = input.nextInt();
			for(int x = 0; x < totalSymbols; x++){
				input.next();
				input.next();
			}
			// process the use list
			int count = input.nextInt();
			// how to make the useList size of the program text tokens length????
			String[] useList = new String[100];
			// fill the uselist with empty string
			Arrays.fill(useList, "");
			for(int y = 0; y < count; y++){
				String use = input.next();
				int index = input.nextInt();
				if(useList[index] != ""){
					int position = currentMod + index;
					System.out.println("Error: Multiple symbols used at position " + position +"; last value used");
					symbolUsed.put(useList[index], true);
				}
				// assign the use value
				useList[index] = use;
			}
			// process the program text
			int length = input.nextInt();
			// hold all adddresses for current module
			int[] currentModAddr = new int[length];
			for(int j = 0; j < length; j++){
				// current final address 
				int address = 0;
				String letter = input.next();
				// IMMEDIATE - ignore
				if(letter.equals("I")){
					address = input.nextInt();
					currentModAddr[j] = address;	
				}
				// ABSOLUTE - if correct, ignore
				if(letter.equals("A")){
					address = input.nextInt();
					if(address % 1000 > 300){
						System.out.println(address + " Error: Absolute address exceeded machine size; max legal value used.");
						address = (address / 1000 ) * 1000 + 299;
					}
					currentModAddr[j] = address;	

				}
				// RELATIVE - check for errors and add the module start.
				if(letter.equals("R")){
					// relocate relative address
					address = currentMod + input.nextInt();
					if(address % 1000 > currentMod + length){
						System.out.println(address + " Error: Relative address exceeds module size; 0 (relative) used.");
						address = (address / 1000 ) * 1000 + currentMod;
					}
					currentModAddr[j] = address;	

				}
				//------------ OK ---------------//
				// EXTERNAL - resolve to absolute addresses
				if(letter.equals("E")){
					currentModAddr[j] = input.nextInt();
				}

			}
			// Resolve the external references from use list for current module
			for(int p = 0; p < length; p++){
				String ref = useList[p];
				// found the start of a linked list
				if(ref != ""){
					if(useSymbol(ref) != -1){
						currentModAddr = resolveExternal(currentModAddr, p, ref);
						// // mark the symbol as used from use list
						symbolUsed.put(ref, true);
					}
					else{
						System.out.print("Error: ");
						System.out.print(ref);
						System.out.println(" was used not defined; It has been given the value 111.");
						symbolTable.put(ref, 111);
						currentModAddr = resolveExternal(currentModAddr, p, ref);
						symbolUsed.put(ref, true);
					}
				}
			}

			// the base address for next module
			currentMod += length;
			// add the absolute addresses for current module
			for(int q = 0; q < currentModAddr.length; q++){
				finalAddr.add(currentModAddr[q]);
			}
		}
		

		printMap(finalAddr);
		// check if there is unused symbol in the definition table
		for(Map.Entry entry : symbolUsed.entrySet()){
            if(entry.getValue().equals(false)){
            	System.out.print("Warning:");
				System.out.print(entry.getKey());
				System.out.println(" was defined but not used.");
            }
        }
		input.close();
	}

	/*
	 * Resolve External addresses to absolute addresses from given array
	 */ 
	private static int[] resolveExternal(int[] array,  int index, String ref){
		int address = array[index];
		int next = address % 1000;
		// not the end of the list
		while(next != 777){
			address = (address / 1000 ) * 1000 + useSymbol(ref);
			array[index] = address;
			address = array[next];
			index= next;
			next = address % 1000;
		}
		// dealing with end of the list
		address = (address / 1000 ) * 1000 + useSymbol(ref);
		array[index] = address;
		return array;
	}


	/*
	 * Find target in symbol table and return its value, -1 if not found
	 */ 
	private static int useSymbol(String key){
		if(symbolTable.containsKey(key)){
			symbolUsed.put(key, true);
			return symbolTable.get(key);
		}
		else{
			return -1;
		}
	}

	/*
	 * Print the symbol hash table
	 */
	private static void printTable(Map<String, Integer> table){
		System.out.println();
		System.out.println("  Symbol Table");
		// return a set view of the mappings contained 
		for (Map.Entry<String,Integer> symbol : table.entrySet()) {
			// getKey() and getValue() are map.entry methods
			System.out.println("  " + symbol.getKey() + " = " + symbol.getValue());
		}
		System.out.println();
	}


	/*
	 * Print the memory map for final absoulte addresses
	 */
	private static void printMap(List<Integer> list){
		for (int i = 0; i < list.size(); i++) {
		 	System.out.print("  " + i + ":   ");
		 	System.out.println(list.get(i));
		 }
		 System.out.println();
	}


}
