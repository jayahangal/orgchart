import java.util.*;
import java.io.*;

class DiffOrg implements Serializable {

private static void print_usage_and_die()
{
    System.out.println ("Usage: java DiffOrg -name <root name as: firstname lastname> [-output <file to write>] [-cmp <file to compare>] [-depth <n>] [-print]\n");
    System.exit (2);
}

// prints fellows in c1, but not in c2
public static void diff_collection (Collection c1, Collection c2)
{
    int num = 0;
    for (Iterator it = c1.iterator(); it.hasNext(); )
    {
        Person p = (Person) it.next();
	boolean foundP = false;
    	for (Iterator it2 = c2.iterator(); it2.hasNext(); )
	{
            Person p2 = (Person) it2.next();
	    if (p.equals(p2)) {
		foundP = true;
		break;
	    }
	}
	if (!foundP)
        //if (!c2.contains (p))
	{
	    ++num;
            System.out.println ("----------------\n" + num + ". ");
	    p.print_chain_of_command();
	}
    }
}

public static void main(String args[]) throws Exception
{
    String root_id = null;
    String root_name = null;
    String out_filename = null;
    String compare_filename = null;
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
	else if (args[i].equals ("-cmp")) 
	    compare_filename = args[++i];
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
    //if ((print_tree == false) && (out_filename == null))
    //	print_tree = true; // if no -p and no outfile, just print 

    Person x;
    if (root_id != null) {
        x = new Person (root_id, true,  0);
    } else {
        x = new Person (root_name, false, 0);
    }
   
    x.fill_reports (max_depth-1);
    System.out.println ();

    Person.add_person_and_reports_to_collection (all_people, x);
    x.find_total_reports();

    if (print_tree)
	x.print_tree_downward(max_depth);

    if (compare_filename != null)
    {
        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (
					compare_filename));
        Collection all_people_old = (Collection) ois.readObject ();
	System.out.println ("======== New employees ============");
	diff_collection (all_people, all_people_old);
	System.out.println ("\n======== employees left, old total:" +
                        all_people_old.size() + " new total:" +
                        all_people.size() + " ============");

	diff_collection (all_people_old, all_people);
        System.out.println("Number of people left:" +
		(all_people_old.size() - all_people.size()));
    }

    if (out_filename != null)
    {
	ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (out_filename));
	oos.writeObject (all_people);
    }
}

}

