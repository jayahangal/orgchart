import java.util.*;
import java.io.*;

public class PrintOrg {

public static void print_collection (Collection c)
{
    for (Iterator it = c.iterator(); it.hasNext(); )
        System.out.println (it.next());
}

public static void print_usage_and_die()
{
    System.out.println ("Usage: java PrintOrg <saved org file>\n");
    System.exit (2);
}

public static void main(String args[]) throws Exception
{
    String in_filename = null, root_name = null;
    int max_depth = 1000;
    Person root;

    try
    {
        in_filename = args[0];
    } catch (Exception e) { print_usage_and_die(); }

    ObjectInputStream ois = new ObjectInputStream (new FileInputStream (in_filename));
    Collection all_people = (Collection) ois.readObject ();

        // find the root, traverse upwards from first person in collection
        root = (Person) all_people.iterator().next();
        Person.ASSERT (root != null, "There are no people entries in this file!");
        while (!root.is_root())
           root = root.boss();
    root.print_tree_downward (max_depth);
    root.print_XML_tree_downward(max_depth);
}

}
