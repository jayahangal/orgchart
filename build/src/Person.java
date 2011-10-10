import java.util.*;
import java.io.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

class Person implements Serializable {

private static final long serialVersionUID = 3889201382071082945L;
private String _id;
private String _name;
private String _loc;
public List _reports;
private Person _boss; // this field is set to the same node or to null for the root
private int _total_reports;
private int _level;
private HashMap<String, Integer> _geo_map = new HashMap<String, Integer>(); // relevant for non-leaf nodes only
String namesFile = "while_house_staff.csv";

private static LdapContext lc;
static String XMLFilename = "sun-orgchart.xml";
//private static String geoFilename = "sun-geo-freq.txt";
private static PrintWriter treePrinter;
private static PrintWriter geoPrinter;
private static SearchControls ctls;

public Person boss() { return _boss; }

public static void ASSERT (boolean c, String m) 
{
    if (!c)
    {
	System.out.println ("Assertion failed: " + m);
        System.exit(2);
    }
}

// setup environment
public static void init () throws Exception
{
    Hashtable env = new Hashtable();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://sun-ds.sun.com:389/ou=people,dc=sun,dc=com");
    env.put(Context.REFERRAL, "follow");
    lc = new InitialLdapContext(env, null);

    SearchControls ctls = new SearchControls();
    String to_return[] = new String[3];
    to_return[0] = "employeenumber";
    to_return[1] = "cn";
    to_return[2] = "globallocation";
    ctls.setReturningAttributes(to_return);
}

// returns true if this person is the root of the tree
// he is in
boolean is_root ()
{
    return ((this._boss == null) || (this._boss == this));
}

public Person(String searchKey, boolean isIDPresent, int level)
		throws NamingException
{

    NamingEnumeration results;
    if (isIDPresent) {
        _id = searchKey;
        results = lc.search("", "employeenumber="+searchKey, ctls);
    } else {
	results = lc.search("", "cn="+searchKey, ctls);
    }
    if (!results.hasMore()) {
        System.out.println ("Person \"" + searchKey + "\" does not exist!\nAborting...");
        System.exit(2);
    }
    SearchResult sr = (SearchResult) results.next();
    fillSearchResult(sr);
    _reports = new ArrayList();
    _level = level;
}

public Person(int level) {
    _level = level;
    _reports = new ArrayList();
}

private void fillSearchResult(SearchResult res) {

    Attributes attrs = res.getAttributes();
    Enumeration ids = attrs.getIDs();

    while (ids.hasMoreElements())
    {
        String x = (String) ids.nextElement();
        if (x.equals ("cn"))
        {
            _name = attrs.get(x).toString();
            _name = _name.substring (0, _name.indexOf( ","));
            _name = _name.substring ("cn: ".length());
        }
        else if (x.equals ("globallocation"))
        {
            _loc = attrs.get(x).toString();
            _loc = _loc.substring ("globallocation: ".length());
        }
        else if (x.equals ("employeenumber"))
        {
            _id = attrs.get(x).toString();
            _id = _id.substring ("employeenumber: ".length());
	}
    }
    
    if (_name == null)
    {
        System.out.println ("Person with id " + _id + " does not exist!\nAborting...");
        System.exit(2);
    }
}

// fill in this person's _reports structure recursively,
// subject to max_levels
public void fill_reports (int max_levels) throws Exception
{
    if (max_levels == 0)
        return;

    NamingEnumeration results = lc.search("", "reportsto="+_id, ctls);
    while (results.hasMoreElements())
    {
        SearchResult result = (SearchResult) results.nextElement();
	Person p = new Person(_level+ 1);
	p.fillSearchResult(result);
	//System.out.println(p._name);
        _reports.add (p);
        p._boss = this;
        p.fill_reports(max_levels-1);
    }
}

String getLocation(String locCode) {
    if (locCode == null) {
	System.out.println("person with no location:" + _name);
	return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(locCode.charAt(0));
    sb.append(locCode.charAt(1));
    sb.append(locCode.charAt(2));
    return sb.toString();
}

// from the collection c, return a new collection consisting of
// the tree starting down from root_id 
public static Collection select_collection (Collection c, Person p)
{
    // find the person with this root_name in the collection first
    //Person p = find_person (c, root_id);

    // now add p and all his reports to a new collection
    Collection c1 = new HashSet();
    add_person_and_reports_to_collection (c1, p);
    System.out.println (c1.size());
    return c1;
}

public int find_total_reports()
{
    //System.out.println("Calling find total reports..for:" + _name);
    if (_reports.size() == 0)
    {
        _total_reports = 0;
    	updateGeoMap(_loc);
        return 1;
    }
    int sum = 0;
    Iterator e = _reports.iterator();
    while (e.hasNext())
    {
        Person p = (Person) e.next();
        sum += p.find_total_reports();
	//System.out.println("Merging with:" + p._name);
	mergeGeoMap(p._geo_map);
   }
    _total_reports = sum;
    updateGeoMap(_loc);
    return sum+1;
}

void updateGeoMap(String locCode) {
        int persons;
	String loc = getLocation(locCode);
        if(_geo_map.get(loc) == null) {
	    persons = 1;
	} else {
            persons = _geo_map.get(loc);
	    persons++;
	}
	_geo_map.put(loc, persons);
}

void mergeGeoMap(HashMap<String, Integer> map) {
    Iterator e = map.entrySet().iterator();
    while (e.hasNext()) {
	Map.Entry<String, Integer> me = (Map.Entry<String, Integer>) e.next();
	String key = me.getKey();
	if (_geo_map.containsKey(key)) {
	    int addedV = _geo_map.get(key) + me.getValue();
	    _geo_map.put(key, addedV);
	} else {
	    _geo_map.put(key, me.getValue());
	}
    }
}

// only return true if both the id string and the 
// boss's id are exactly the same (this helps
// capture moves within the organization)
public boolean equals (Object o)
{
    Person p = (Person) o;
    if (!p._id.equals (this._id))
	return false;

    // id is same, check the boss
    // some convoluted logic here to avoid nullptr exceptions
    if (p._boss == null) 
	return (this._boss == null);
    else 
    {
	// p._boss is not null
	if (this._boss == null)
	    return false;
	else {
	    return p._boss._id.equals (this._boss._id);
	}
    }
}

public int hashCode() {

    // compute XOR of _id, _boss._id
    int hashId = _id.hashCode();    

    if (_boss == null) {
	return hashId;
    }

    int hashBossId =_boss._id.hashCode();    
    return ((hashId | hashBossId) & (~hashId | ~hashBossId));
}
 

// right-justify string s to length len, and return it
private String adjust (String s, int len)
{
    if (s == null)
        s = "null";

    if (s.length() >= len)
        return s.substring (0, len);
    
    StringBuffer sb = new StringBuffer(s);
    for (int i = 0 ; i < len-s.length(); i++)
        sb.append (" ");
    return sb.toString();
}

public static void add_person_and_reports_to_collection (Collection c, Person p)
{
    c.add (p);
    for (Iterator it = p._reports.iterator() ; it.hasNext(); )
	add_person_and_reports_to_collection (c, (Person) it.next());
}

public String toString()
{
    StringBuffer sb = new StringBuffer();
    for (int i = 0 ; i < _level; i++)
        sb.append ("  ");

    String adjustedName = adjust (_name, 30);

    sb.append (adjust (adjustedName, 20) 
             + " ID: " + adjust (_id, 7)
             + ", Loc: " + adjust (_loc, 5)
             + ", Reports: " + _reports.size() + " direct, " 
             + _total_reports + " total");
    return sb.toString();
}

// print this person's chain of command by following _boss field
public void print_chain_of_command ()
{
    Person x = this;
    do {
        System.out.println (x);
        x = x._boss;
    } while ((x != null) && !x.is_root ());
}

public void print_tree_downward (int max_depth)
{
    if (max_depth == 0)
	return;

    System.out.println (this);
    if (_reports.size() <= 0)
        return;

    Iterator e = _reports.iterator();
    while (e.hasNext())
    {
        Person p = (Person) e.next();
        p.print_tree_downward(max_depth-1);
    }

    for (int i = 0 ; i < _level+1 ; i++)
        System.out.print ("-");
    System.out.println ();
}


 void print_XML_tree_downward (int max_depth) throws Exception {
    System.out.println("total reports:" + _total_reports);
    //readReplaceNames();
    initNewGeos();
    //geoPrinter = new PrintWriter(geoFilename);
    treePrinter = new PrintWriter(XMLFilename);
    treePrinter.println("<?xml version=\"1.0\" standalone=\"no\"?>");
    treePrinter.println("<tree>");
    treePrinter.println("<declarations>");
    treePrinter.println("<attributeDecl name=\"name\" type=\"String\"/>");
    treePrinter.println("<attributeDecl name=\"id\" type=\"String\"/>");
    treePrinter.println("<attributeDecl name=\"number\" type=\"Real\"/>");
    treePrinter.println("<attributeDecl name=\"locations\" type=\"String\"/>");
    treePrinter.println("</declarations>");
    generateXMLTree(max_depth);
    treePrinter.println("</tree>");
    treePrinter.flush();
    treePrinter.close();
    //geoPrinter.flush();
    //geoPrinter.close();
}

private void generateXMLTree(int max_depth) throws Exception {
    if (max_depth == 0)
        return;
    if (_reports.size() <= 0) {

        // write leaf node
        treePrinter.println("<leaf>");
        treePrinter.print("<attribute name=\"name\" value=\"");
        treePrinter.print(_name); //replaceName());
        treePrinter.println("\"/>");
     	treePrinter.print("<attribute name=\"id\" value=\"");
     	treePrinter.print(_id);
     	treePrinter.println("\"/>");
        // More attributes can go hear
        treePrinter.println("</leaf>");
        return;
    }
    // write branch node
     treePrinter.println("<branch>");
     treePrinter.print("<attribute name=\"name\" value=\"");
     treePrinter.print(_name); 		//replaceName()); // 
     treePrinter.println("\"/>");
     treePrinter.print("<attribute name=\"id\" value=\"");
     treePrinter.print(_id);
     treePrinter.println("\"/>");
     treePrinter.print("<attribute name=\"number\" value=\"");
     treePrinter.print(_total_reports);
     treePrinter.println("\"/>");
     treePrinter.print("<attribute name=\"locations\" value=\"");
     treePrinter.print(_geo_map.toString());  //getReplacedGeos(_geo_map.toString()));
     treePrinter.println("\"/>");
     // More attributes can go hear
     //geoPrinter.println(_name + " " + _geo_map);

    Iterator e = _reports.iterator();
    while (e.hasNext())
    {
        Person p = (Person) e.next();
        p.generateXMLTree(max_depth-1);
    }
    treePrinter.println("</branch>");
}

   /* Used for anonymizing the names */
    static ArrayList newNames = new ArrayList();
    String replaceName() {
	Random r = new Random();
	int next = r.nextInt(newNames.size());	
	return (String) newNames.get(next);
    }

    void readReplaceNames() throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader("white_house_staff.csv"));
	String line;
	while ((line = reader.readLine()) != null) {		
	    StringTokenizer st = new StringTokenizer(line, "\"");
	    if (st.hasMoreTokens()) {
	        String name = st.nextToken();
	        StringTokenizer st2 = new StringTokenizer(name, ",");
		String n = "";
		while (st2.hasMoreTokens()) {
		    n = n + " " + st2.nextToken();
		}
	        newNames.add(n);
	    }
	}	
	reader.close();
	System.out.println("size:" + newNames.size());
    }

    static HashMap renamedGeos = new HashMap();

    static void initNewGeos() {
	renamedGeos.put("sca", "DC");
	renamedGeos.put("blr", "Burlingame");
	renamedGeos.put("tlv", "Newyork");
	renamedGeos.put("ham", "Hamilton");
	renamedGeos.put("spb", "Philadelphia");
	renamedGeos.put("mrk", "Palo Alto");
	renamedGeos.put("tyo", "Orlando");
	renamedGeos.put("sfo", "San Francisco");
	renamedGeos.put("bne", "Portland");
	renamedGeos.put("lon", "Seattle");
	renamedGeos.put("prg", "Boston");
	renamedGeos.put("chi", "Minnesota");
	renamedGeos.put("ack", "KeyWest");
	renamedGeos.put("brm", "Edison");
	renamedGeos.put("sac", "New Hampshire");
	renamedGeos.put("ita", "Buffalo");
	renamedGeos.put("muc", "Pittsburg");
	renamedGeos.put("vie", "Manhatten");
	renamedGeos.put("gmp", "Providence");
	renamedGeos.put("par", "Austin");
	renamedGeos.put("sel", "Dallas");
	renamedGeos.put("sfl", "Huston");
	renamedGeos.put("nov", "Palo alto");
	renamedGeos.put("cry", "Sunnyvale");
	renamedGeos.put("blv", "Santa Clara");
	renamedGeos.put("atl", "San Jose");
	renamedGeos.put("gnb", "Oakland");
	renamedGeos.put("bjs", "Richmond");
	renamedGeos.put("dub", "Dublin");
	renamedGeos.put("str", "San Mateo");
	renamedGeos.put("mpk", "Menlo Park");
	renamedGeos.put("ber", "Detroit");
	renamedGeos.put("gva", "San Carlos");
	renamedGeos.put("ego", "Red Wood City");
	renamedGeos.put("aus", "Fremont");
	renamedGeos.put("mon", "Irvine");
	renamedGeos.put("bur", "Cupertino");
	renamedGeos.put("tpe", "Red Wood City");
    }
	
   /* Used for anonymizing the geos */
    static String  getReplacedGeos(String geoSet) {
	StringBuffer newSet = new StringBuffer("{");
	StringTokenizer st = new StringTokenizer(geoSet, "{}");
        while (st.hasMoreTokens()) {
            String map = st.nextToken();
            //System.out.println("map:" + map);
            StringTokenizer et = new StringTokenizer(map, " = ,");
		if (st.hasMoreTokens()) {
                    String dname = et.nextToken();
		    newSet.append(renamedGeos.get(dname));
		    newSet.append('=');
                    newSet.append(et.nextToken());
		}
                while (et.hasMoreTokens()) {
		    newSet.append(',');
                    String dname = et.nextToken();
		    newSet.append(renamedGeos.get(dname));
		    newSet.append('=');
                    newSet.append(et.nextToken());
                }
	}	
	newSet.append('}');
	return newSet.toString();
    }
}

