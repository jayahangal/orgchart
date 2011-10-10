import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import java.util.StringTokenizer;
import java.util.Random;
import java.net.URL;
import java.net.MalformedURLException;

import prefuse.util.FontLib;

public class Connect extends JPanel
	implements ActionListener {

    static final String DEFAULT_PHOTO = "images/pipal.jpg";
    static final String LINKED_IN_SRCH_PREFIX = "http://www.linkedin.com/search?pplSearchOrigin=GLHD&";
    static final String LINKED_IN_SRCH_SUFFIX = "&search=";
    static final String FACEBOOK_SRCH_PREFIX = "http://www.facebook.com/search/?o=2048&init=s%3Aemail&";
    static final String NAMEFINDER_PREFIX = "http://namefinder.sfbay.sun.com/NameFinder?-s=";
    static final String COMPANY_SRCH = "&company="; //Sun+Microsystems
   
    String linkedInUrl;
    String facebookUrl;
    static boolean useDefault = true;
    String photoUrl;
    String companyUrl;
    String companyLogo = "images/sunLogo.gif";
    protected JButton li, fb, cb;

    //static JFrame connectFrame;

    public Connect(String name, String photoUrl, String companyUrl) {
	String urlname = name.trim();
	this.photoUrl = photoUrl;
	this.companyUrl = companyUrl;
	if (this.photoUrl != null) {
	    photoUrl = getPhotoName();
	}
	linkedInUrl = LINKED_IN_SRCH_PREFIX + "keywords=" + getQueryName(name);
	if (OrgViewer.useCompany) {
	   linkedInUrl += COMPANY_SRCH + getQueryName(OrgViewer.companyName) + LINKED_IN_SRCH_SUFFIX;
	} else {
	   linkedInUrl += LINKED_IN_SRCH_SUFFIX;
	}
	facebookUrl = FACEBOOK_SRCH_PREFIX + "q=" + getQueryName(name);
        ImageIcon liButtonIcon = createImageIcon("images/linkedInLogo.jpg", 40, 40);
        ImageIcon fbButtonIcon = createImageIcon("images/facebookLogo.png", 40, 40);
        ImageIcon compIcon;

        li = new JButton("", liButtonIcon);
        li.setMnemonic(KeyEvent.VK_L);
        li.setActionCommand("ln link");

        fb = new JButton("",  fbButtonIcon);
        fb.setMnemonic(KeyEvent.VK_F);
        fb.setActionCommand("fb link");
        fb.setEnabled(true);

	if (companyUrl != null) {
	    compIcon = createImageIcon(companyLogo, 40, 40);
            cb = new JButton("",  compIcon);
            cb.setMnemonic(KeyEvent.VK_S);
            cb.setActionCommand("sun link");
            cb.setEnabled(true);
            cb.addActionListener(this);
	}

        //Listen for actions on buttons 1 and 2.
        li.addActionListener(this);
        fb.addActionListener(this);

        //li.setToolTipText("Click this button to disable the middle button.");
        //fb.setToolTipText("Click this button to enable the middle button.");

	JLabel pictLabel = getPictureLabel(name); 

        //Add Components to this container, using the default FlowLayout.
	Box box = new Box(BoxLayout.X_AXIS);
	box.add(Box.createHorizontalStrut(20));
        box.add(li);
	box.add(Box.createHorizontalStrut(30));
        box.add(fb);
	box.add(Box.createHorizontalStrut(20));

	Box box2 = new Box(BoxLayout.X_AXIS);
	box2.add(pictLabel);
	box2.add(Box.createHorizontalStrut(20));

	Box boxY = new Box(BoxLayout.Y_AXIS);
	boxY.add(box2);	
	boxY.add(Box.createVerticalStrut(20));
	boxY.add(box);	
	boxY.add(Box.createVerticalStrut(5));
	if (companyUrl != null) {
	   Box box3 = new Box(BoxLayout.X_AXIS);
	   box3.add(Box.createHorizontalStrut(20));
	   box3.add(cb);
	   box3.add(Box.createHorizontalStrut(20));
	   boxY.add(box3);
	}
	boxY.add(Box.createVerticalStrut(10));

	add(boxY, BorderLayout.CENTER);

	Border raisedBdr = BorderFactory.createRaisedBevelBorder();
	Border lineBdr = BorderFactory.createLineBorder(getRandomColor(), 5);
	setBorder(lineBdr);
    }

    public void actionPerformed(ActionEvent e) {
	//System.out.println("action received");	
        if ("fb link".equals(e.getActionCommand())) {
            BareBonesBrowserLaunch.openURL(facebookUrl);
        } else if ("ln link".equals(e.getActionCommand())) {
            BareBonesBrowserLaunch.openURL(linkedInUrl);
        } else {
            BareBonesBrowserLaunch.openURL(companyUrl);
	}
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    static ImageIcon createImageIcon(String path, int width, int height) {
        URL imgURL = null;
	if (path != null) {
            imgURL = Connect.class.getResource(path);
	}
        if (imgURL == null) {
	    try {
	        imgURL = new URL(path);
	    } catch (MalformedURLException e) {
		System.out.println("Getting the photoname..");
		imgURL = Connect.class.getResource(getPhotoName()); 
	    }
	}
	//System.out.println("URL:" + imgURL);
	if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
	    Image scaledImage = (icon.getImage()).getScaledInstance(width, height, Image.SCALE_SMOOTH);
	    icon.setImage(scaledImage);
	    return icon;
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static JFrame getPplCard(String name, String photoUrl, String companyUrl) {

        //Create and set up the window.
        JFrame frame = new JFrame("Connect");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Add content to the window.
        frame.add(new Connect(name, photoUrl, companyUrl));
	Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
	frame.setLocation(center.x, center.y);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	return frame;
    }

    private JLabel getPictureLabel(String name) {
	ImageIcon photo = createImageIcon(photoUrl, 100, 110);	
        JLabel jlabel = new JLabel(name, photo, JLabel.CENTER);
	jlabel.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
        //Set the position of its text, relative to its icon:
        jlabel.setVerticalTextPosition(JLabel.TOP);
        jlabel.setHorizontalTextPosition(JLabel.CENTER);
	return jlabel;
    }


    private String getQueryName(String name) {
	StringBuilder sb = new StringBuilder();
	StringTokenizer st = new StringTokenizer(name);
	while (st.hasMoreTokens()) {
	    String part = st.nextToken();
	    sb.append(part);
	    sb.append('+');
	}
	// take out the trailing '+'
	int len = sb.length();
	sb = sb.delete(len - 1, len);	
	//System.out.println("query:" + sb);
	return sb.toString();
    }

    static int switchPhoto = 1;

    static String getPhotoName() {
	if (useDefault) {
	    if (switchPhoto == 1) {
		switchPhoto = 0;
		return "images/pipal.jpg";
	    } else {
		switchPhoto = 1;
		return "images/pipal-pink.jpg";
	    }
        }
	return "";
     }

     Color getRandomColor() {
	Random r= new Random();
        int random = r.nextInt(5);
	switch(random) {
	    case 0: return Color.ORANGE;	    
	    case 1: return Color.DARK_GRAY;	    
	    case 2: return Color.GRAY;	    
	    case 3: return Color.BLUE;	    
	    case 4: return Color.GREEN;	    
	    case 5: return Color.YELLOW;	    
	    default: return Color.BLACK;
	}
    }

    public static void createAndShowGUI() {

        //Create and set up the window.
        JFrame frame = new JFrame("Connect");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new Connect("Barrack Obama", null, null));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
