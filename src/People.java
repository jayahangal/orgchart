import java.util.*;
import java.io.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

class People implements Serializable {

String _id;
String _name;
Attributes _attributes;
String _DN;
List _reports;
static Collection _all_people = new ArrayList();
static LdapContext lc;
static SearchControls ctls;

static void init () throws Exception
{
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://sun-ds.sun.com:389/ou=people,dc=sun,dc=com");
    env.put(Context.REFERRAL, "follow");
    lc = new InitialLdapContext(env, null);
    SearchControls ctls = new SearchControls();
}

public People(String id, int level) throws NamingException
{
    _name = "" ; // find_name_by_id (...);
    _id = id;

    NamingEnumeration results = lc.search("", "employeenumber="+id, ctls);
    if (!results.hasMore())
    {
        System.out.println ("People with id " + _id + " does not exist!\nAborting...");
        System.exit(2);
    }
    SearchResult res = (SearchResult) results.next();
    _name = res.getName();
    _DN = res.getNameInNamespace();
    _attributes = res.getAttributes();
    _reports = new ArrayList();
    _all_people.add (this);
}

People(SearchResult res, String id) {
    _id = id;
    _name = res.getName();
    _DN = res.getNameInNamespace();
    _attributes = res.getAttributes();
    _reports = new ArrayList();
    _all_people.add (this);
}

static void fill_reports (People person) throws Exception
{

//    NamingEnumeration results = lc.search(null, "reportsto="+_id, to_return);
    NamingEnumeration results = lc.search("", "reportsto="+person._id, ctls);
    while (results.hasMoreElements())
    {
        SearchResult result = (SearchResult) results.nextElement();
	String id = getEmpId(result);	
	People directReport = new People(result, id);
        person._reports.add (directReport);
        fill_reports(directReport);
    }
}

static private String getEmpId (SearchResult res) {
        Attributes attrs = res.getAttributes();
	Enumeration ids = attrs.getIDs();

        while (ids.hasMoreElements())
        {
            String x = (String) ids.nextElement();
            if (x.equals("employeenumber"))
            {
                String s = (String) attrs.get(x).toString();
                System.out.println ("s = " + s);
		return s;
	    }
	}
	return null;
}

public int find_total_reports()
{
    if (_reports.size() == 0)
    {
        return 1;
    }
    int sum = 0;
    Iterator e = _reports.iterator();
    while (e.hasNext())
    {
        People p = (People) e.next();
        sum += p.find_total_reports();
    }
    return sum+1;
}

static void populateDir(String host, int port, String treeRoot)
	throws NamingException
{
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://" + host + ":" + port);
    DirContext ctx = new InitialLdapContext(env, null);
    System.out.println(ctx.lookup("ou=jws,o=Sun Employees"));

    for (Iterator iter = _all_people.iterator(); iter.hasNext();) {
	People p = (People) iter.next();
	System.out.println(p._attributes);
	/*
	int index = p._name.lastIndexOf(" ");
	String cn = p._name.substring(0, index);
	*/
	String dn = p._name + "," + treeRoot;
	ctx.createSubcontext(dn, p._attributes);
    }
    ctx.close();
}

public static void main(String args[]) throws Exception
{
    int starting_id = 120125;
    init();
    People x = new People (args[0], 0);
    fill_reports (x);
    System.out.println(x.find_total_reports());
    for (Iterator iter = _all_people.iterator(); iter.hasNext();) {
	People p = (People) iter.next();
    	System.out.println(p._name);
    }
    populateDir("purvi.india.sun.com", 2389, "ou=jws,o=Sun Employees");
  
    if (args.length > 1)
    {
        ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (args[1]));
        oos.writeObject (_all_people);
	oos.close();
    }
}

}

