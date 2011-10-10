/* 
 * @(#)Getattr.java     1.5 00/04/28
 * 
 * Copyright 1997, 1998, 1999 Sun Microsystems, Inc. All Rights
 * Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in source and
 * binary code form, provided that i) this copyright notice and license
 * appear on all copies of the software; and ii) Licensee does not 
 * utilize the software in a manner which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE 
 * HEREBY EXCLUDED.  SUN AND ITS LICENSORS SHALL NOT BE LIABLE 
 * FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, 
 * MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN 
 * NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT 
 * OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line
 * control of aircraft, air traffic, aircraft navigation or aircraft
 * communications; or in the design, construction, operation or
 * maintenance of any nuclear facility. Licensee represents and warrants
 * that it will not use or redistribute the Software for such purposes.  
 */
import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attributes;
import javax.naming.NamingException;
import java.util.Hashtable;
import javax.naming.directory.*;
import javax.naming.NamingEnumeration;

/**
 * Demonstrates how to retrieve an attribute of a named object.
 *
 * usage: java Getattr
 */
class GetattrSun {
    public static void main(String[] args) {
        // Identify service provider to use
        Hashtable env = new Hashtable(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, 
            "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://sun-ds.sun.com/ou=people,dc=sun,dc=com");

        DirContext ctx;
        try {

            // Create the initial directory context
            ctx = new InitialDirContext(env);
	    
            // Ask for all attributes of the object 
            // Attributes attrs = ctx.getAttributes("cn=Jayalaxmi Hangal (jh120125)"); // + args[0]);
	    // System.out.println(attrs);

	    String filter = "(cn=Brain Sutphin*)";
	    SearchControls sc = new SearchControls();
	    NamingEnumeration result = ctx.search("", filter, sc);
	    while (result.hasMore()) {
		SearchResult sr = (SearchResult) result.next();
		printAttrs(sr.getAttributes());
	    }

            ctx.close();
        } catch(Exception e) {
		e.printStackTrace();
	}
    }


     static void printAttrs(Attributes attrs) {
        if (attrs == null) {
            System.out.println("No attributes");
        } else {
            /* Print each attribute */
            try {
                for (NamingEnumeration ae = attrs.getAll();
                     ae.hasMore();) {
                    Attribute attr = (Attribute)ae.next();
                    System.out.print(attr.getID() + ":");

                    /* print each value */
                    for (NamingEnumeration e = attr.getAll();
                         e.hasMore();
                         System.out.print(e.next() + ","))
                        ;
                    System.out.println();
                }
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }

}

