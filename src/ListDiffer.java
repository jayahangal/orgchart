import java.util.HashSet;
import java.util.Iterator;
import java.io.*;

class ListDiffer {

    HashSet initialSet = new HashSet(39000);
    HashSet laterSet = new HashSet(39000);

    void read(String filename1, String filename2) throws IOException {
	readFile(filename1, initialSet);
	readFile(filename2, laterSet);
    }
	

   void readFile(String filename, HashSet set) throws IOException {

	BufferedReader reader = new BufferedReader(
				    new InputStreamReader(
				    new FileInputStream(filename)));
	String readLine;
	int endOfFirstWord;
	String setEntry;
	
	while ((readLine = reader.readLine()) != null) {
	    endOfFirstWord = readLine.indexOf(" ");
	    setEntry = readLine.substring(endOfFirstWord).trim();
	    set.add(setEntry);
	}
	
	reader.close();
    }

    void  printDiff() {
	//System.out.println("list1: " + initialSet);
	//System.out.println("list2: " + laterSet);
	boolean isChanged = initialSet.removeAll(laterSet);
	if (isChanged) {
	    Iterator iter = initialSet.iterator();
	    int cnt = 0;
	    while(iter.hasNext()) {
	 	cnt++;
		System.out.println(cnt + ". " + iter.next());	
	    }
        } else {
	    System.out.println("The lists haven't changed");	
	}
    }
	    

    public static void main(String args[]) throws IOException,
	FileNotFoundException {
	ListDiffer diff = new ListDiffer();
	if (args.length < 2 ) {
	    System.out.println("Usage ListDiffer list1 list2");
	    System.exit(1);
	}
	diff.read(args[0], args[1]);
	diff.printDiff();
    }
}
	
		
	


