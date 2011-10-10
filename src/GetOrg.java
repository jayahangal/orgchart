import java.util.*;
import java.io.*;

class GetOrg implements Serializable {

private static void print_usage_and_die()
{
    System.out.println ("Usage: java GetOrg -name <root name as: firstname lastname> [-output <file to write>] [-depth <n>] [-print]\n");
    System.exit (2);
}

public static void main(String args[]) throws Exception
{
    String root_id = null;
    String root_name = null;
    String out_filename = null;
    int max_depth = Integer.MAX_VALUE;
    boolean print_tree = false;
    Collection all_people = new HashSet();

    Person.init();

    try
    {
    for (int i = 0 ; i < args.length ; i++)
    {
	if (args[i].equals ("-output")) 
	    out_filename = args[++i];
	else if (args[i].equals ("-depth")) 
	    max_depth = Integer.parseInt (args[++i]);
	else if (args[i].equals ("-name")) 
	    root_name = args[++i];
	else if (args[i].equals ("-print")) 
	    print_tree = true;
	else
	    print_usage_and_die();
    }
    } catch (Exception e) { print_usage_and_die(); }

    if (root_id == null && root_name == null)
	print_usage_and_die();
    if ((print_tree == false) && (out_filename == null))
	print_tree = true; // if no -p and no outfile, just print 

    Person x;
    if (root_id != null) {
        x = new Person (root_id, true,  0);
    } else {
        x = new Person (root_name, false, 0);
    }
    int blanki = root_name.indexOf(" ");
    String firstName = (blanki != -1) ? root_name.substring(0, blanki)
		: root_name;
    Person.XMLFilename = OrgViewer.getNameByDate(new Date(), firstName);

    x.fill_reports (max_depth-1);
    System.out.println ();

    Person.add_person_and_reports_to_collection (all_people, x);
    x.find_total_reports();

    if (print_tree) {
	x.print_tree_downward(max_depth);
	x.print_XML_tree_downward(max_depth);
	
    }

    if (out_filename != null)
    {
	ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (out_filename));
	oos.writeObject (all_people);
    }
}

}

