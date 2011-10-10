import java.util.*;
import java.io.*;

class DiffOrgFiles implements Serializable {

private static void print_usage_and_die()
{
    System.out.println ("Usage: java DiffOrgFiles  [-o <old file>] [-n <new file>]\n");
    System.exit (2);
}

// prints fellows in c1, but not in c2
public static void diff_collection (Collection c1, Collection c2)
{
    int num = 0;
    int nFound = 0;
    for (Iterator it = c1.iterator(); it.hasNext(); )
    {
        Person p = (Person) it.next();
	boolean foundP = false;
    	for (Iterator it2 = c2.iterator(); it2.hasNext(); )
	{
            Person p2 = (Person) it2.next();
	    if (p.equals(p2)) {
		foundP = true;
		//System.out.println("found p:" + p);
		nFound++;
		break;
	    }
	}
	if (!foundP)
	
        // if (!c2.contains (p))
	{
	    ++num;
            System.out.println ("----------------\n" + num + ". ");
	    p.print_chain_of_command();
	}
    }
}

public static void main(String args[]) throws Exception
{
    String old_filename = null;
    String new_filename = null;
    int max_depth = Integer.MAX_VALUE;
    boolean print_tree = false;
    Collection all_people = new HashSet();

    Person.init();

    try
    {
    for (int i = 0 ; i < args.length ; i++)
    {
	if (args[i].equals ("-o")) 
	    old_filename = args[++i];
	else if (args[i].equals ("-n")) 
	    new_filename = args[++i];
	else
	    print_usage_and_die();
    }
    } catch (Exception e) { print_usage_and_die(); }

    if (old_filename != null && new_filename != null)
    {
        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (
					old_filename));
        Collection all_people_old = (Collection) ois.readObject ();
	ois.close();
        ois = new ObjectInputStream (new FileInputStream (
					new_filename));
        Collection all_people_new = (Collection) ois.readObject ();
	ois.close();
	System.out.println ("======== New employees total: " +
			all_people_new.size() + " ============");
	diff_collection (all_people_new, all_people_old);
	System.out.println ("\n======== employees left, old total:" +
			all_people_old.size() + " new total:" +
			all_people_new.size() + " ============");
	diff_collection (all_people_old, all_people_new);
        System.out.println("Number of people left:" +
		(all_people_old.size() - all_people_new.size()));
    }
}
}

