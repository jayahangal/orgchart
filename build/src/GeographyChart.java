import javax.swing.JFrame;
import org.jfree.ui.RefineryUtilities;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import javax.swing.Box;
import javax.swing.BoxLayout; 
import javax.swing.JPanel; 
import javax.swing.JComponent; 
import javax.swing.JRootPane; 
import java.awt.BorderLayout; 
import java.awt.Image; 
import java.awt.Toolkit; 
import java.awt.Component; 

import java.net.MalformedURLException; 
import java.net.URL; 
import prefuse.util.ColorLib;

class GeographyChart {

     PieChart p;
     static String datafile = "sun-geo-freq.txt";
     HashMap<String, PieDataset> geoset = new HashMap<String, PieDataset>();

     public GeographyChart(String datafile) {	
	try {
	    createGeoDataset(datafile);
	} catch (IOException e) {
	    e.printStackTrace();
	    System.err.println("Unable to do create Geography chart from file:" +
		datafile);
	}
     }

     public GeographyChart() {
     }

     void doChart(String person, String set) {
	//PieDataset ds = geoset.get(person.trim());
	PieDataset ds = getSet(set);
	//System.out.println("ds:" + ds);
	if (ds != null) {
	this.p = new PieChart("Geography Distribution " + person, ds);
	//doPhoto(p);
        p.pack();
        p.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        RefineryUtilities.centerFrameOnScreen(p);
        p.setVisible(true);
	}
    }

    void doPhoto(JFrame frame) {
       // get the image from the URL
	Image image;
	 try {
        URL url = new URL("http://photos.central.sun.com/120125.jpg");
    
        // Get the image
        image = Toolkit.getDefaultToolkit().getDefaultToolkit().createImage(url);
    	} catch (MalformedURLException e) {
    	} catch (IOException e) {
    	}

	JPanel panel = (JPanel) frame.getContentPane();
	Component comps[] = frame.getComponents();
 	for (int i = 0; i < comps.length; i++) {
	    System.out.println("comps:" + comps[i]);
	}

        Box boxY = new Box(BoxLayout.Y_AXIS);
        boxY.add(new OrgLegend(ColorLib.getColor(255, 0, 0), "test").get());
        //boxY.add(image);
        panel.setLayout(new BorderLayout());
        panel.add(boxY, BorderLayout.CENTER);
    }

    /**
     */
    private void createGeoDataset(String file) 
		throws IOException {
	ClassLoader cl = GeographyChart.class.getClassLoader();
	InputStream in = cl.getResourceAsStream(file);
	BufferedReader br = new BufferedReader(
				new InputStreamReader(in));	
	String entry;
	while ((entry = br.readLine()) != null) {
	     StringTokenizer st = new StringTokenizer(entry, "{}");
	     while (st.hasMoreTokens()) {
	         String name = st.nextToken();
		 //System.out.println("name:" + name);
	         name = name.trim();
	         String map = st.nextToken();
		 //System.out.println("map:" + map);
                  DefaultPieDataset dataset = new DefaultPieDataset();
	         StringTokenizer et = new StringTokenizer(map, " = ,");
	    	while (et.hasMoreTokens()) {  
			String dname = et.nextToken();
		 //System.out.println("data name:" + dname);

			String svalue = et.nextToken();
		 //System.out.println("data value:" + svalue);
			dataset.setValue(dname, Double.valueOf(svalue));		
	    	}
	  	geoset.put(name, dataset);  
	    }
	}
	br.close();
	//System.out.println("dataset:" + geoset);
    }

    private PieDataset getSet(String set) {
        DefaultPieDataset dataset = null;
	StringTokenizer st = new StringTokenizer(set, "{}");
	while (st.hasMoreTokens()) {
	    String map = st.nextToken();
	    //System.out.println("map:" + map);
            dataset = new DefaultPieDataset();
	    StringTokenizer et = new StringTokenizer(map, "=,");
	    	while (et.hasMoreTokens()) {  
		    String dname = et.nextToken();
		    //System.out.println("data name:" + dname);
		    String svalue = et.nextToken();
		    //System.out.println("data value:" + svalue);
		    dataset.setValue(dname, Double.valueOf(svalue));		
	    	}
	    }
	    return dataset;
    }
	

	
    public void closeChart() {
	if (p != null) {
            p.dispose();
	}
    }
}
