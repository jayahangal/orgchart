import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.ControlAdapter;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.Table;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.TreeMLReader;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.data.util.TreeNodeIterator;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

/**
 * Note, the code is modified not to show the org moves for the demo purposes.
 */
public class OrgViewer extends Display {

    private static final String tree= "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    
    private LabelRenderer m_nodeRenderer;
    private EdgeRenderer m_edgeRenderer;
    
    private String m_label = "label";
    private String locField = "Locations";
    private int m_orientation = Constants.ORIENT_LEFT_RIGHT;

    static List left;
    static List joined;
    static Map leftNodes;
    static ArrayList moved; 

    private Tree theViewTree;
    private Tree cmpTree;
    private String viewDataFile; 
    private String cmpDataFile; 
    private Date viewDate;
    private Date cmpDate;
    private GeographyChart gChart;
    private JFrame pplCard;

    static String orgRoot; // a unique name to identify the org file 
    static final String DATA_DIR = "data";
    static final String ORG_FILE_SUFFIX = "orgchart.xml";
    static final String GEO_FILE_SUFFIX = "geo-freq.txt";
	
    static boolean useCompany = false;
    static String companyName = "Sun Microsystems";
    static boolean isSunOrg = false;
    static String sunPhotoUrl = "http://photos.central.sun.com/";
    static String sunNameFinder = "http://namefinder.sfbay.sun.com/NameFinder?-s=";
    //static String defaultPhotoUrl = "images/pipal.jpg";
    
    public OrgViewer(String root, String viewStr, String cmpStr) {
        super(new Visualization());
	orgRoot = root;
	this.viewDate = getDateByName(viewStr);	
	this.cmpDate = getDateByName(cmpStr);	
	setupFiles(viewStr, cmpStr);
	setupTrees();
	//gChart = new GeographyChart(orgRoot + "-" + GEO_FILE_SUFFIX);
	gChart = new GeographyChart();
    }

    public OrgViewer(Date viewd, Date cmpd, String viewFile, String cmpFile) {
	super (new Visualization());
	OrgViewer.orgRoot = orgRoot;
	this.viewDate = viewd;
	this.cmpDate = cmpd;
	this.viewDataFile = viewFile;
	this.cmpDataFile = cmpFile;
	setupTrees();
	//gChart = new GeographyChart(orgRoot + "-" + GEO_FILE_SUFFIX);
	gChart = new GeographyChart();
    }

    private void setupFiles(String viewStr, String cmpStr) {
	this.viewDataFile = orgRoot + "-" + viewStr + "-" + ORG_FILE_SUFFIX;
	this.cmpDataFile  = orgRoot + "-" + cmpStr + "-" + ORG_FILE_SUFFIX;
    }
	
    private void setupTrees() {
	cmpTree = null;
        try {
            this.cmpTree  = (Tree) new TreeMLReader().readGraph(cmpDataFile);
            this.theViewTree  = (Tree) new TreeMLReader().readGraph(viewDataFile);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(1);
        }
	findDifference();
     }


     public void doVisualSetup(String label) {
        m_label = label;
        m_vis.add(tree, theViewTree);
         
        m_nodeRenderer = new OrgLabelRenderer();
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
        m_nodeRenderer.setRoundedCorner(8,8);
        m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
        
        DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
        rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
        m_vis.setRendererFactory(rf);
               
        // colors
        ItemAction nodeColor = new NodeColorAction(treeNodes);
        ItemAction textColor = new DiffColorAction(treeNodes,
                new VisiblePredicate(), VisualItem.TEXTCOLOR);
        m_vis.putAction("textColor", textColor);
        
        ItemAction edgeColor = new ColorAction(treeEdges,
                VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));
        
        // quick repaint
        ActionList repaint = new ActionList();
        repaint.add(nodeColor);
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);
        
        // full paint
        ActionList fullPaint = new ActionList();
        fullPaint.add(nodeColor);
        m_vis.putAction("fullPaint", fullPaint);
        
        // animate paint change
        ActionList animatePaint = new ActionList(400);
        animatePaint.add(new ColorAnimator(treeNodes));
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);
        
        // create the tree layout action
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(tree,
                m_orientation, 50, 0, 8);
        treeLayout.setLayoutAnchor(new Point2D.Double(25,300));
        m_vis.putAction("treeLayout", treeLayout);
        
        CollapsedSubtreeLayout subLayout = 
            new CollapsedSubtreeLayout(tree, m_orientation);
        m_vis.putAction("subLayout", subLayout);
        
        AutoPanAction autoPan = new AutoPanAction();
        
        // create the filtering and layout
        ActionList filter = new ActionList();
        filter.add(new FisheyeTreeFilter(tree, 2));
        filter.add(new FontAction(treeNodes, FontLib.getFont("Tahoma", 16)));
        filter.add(treeLayout);
        filter.add(subLayout);
        filter.add(textColor);
        filter.add(nodeColor);
        filter.add(edgeColor);
        m_vis.putAction("filter", filter);
        
        // animated transition
        ActionList animate = new ActionList(1000);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(autoPan);
        animate.add(new QualityControlAnimator());
        animate.add(new VisibilityAnimator(tree));
        animate.add(new LocationAnimator(treeNodes));
        animate.add(new ColorAnimator(treeNodes));
        animate.add(new RepaintAction());
        m_vis.putAction("animate", animate);
        m_vis.alwaysRunAfter("filter", "animate");
        
        // create animator for orientation changes
        ActionList orient = new ActionList(2000);
        orient.setPacingFunction(new SlowInSlowOutPacer());
        orient.add(autoPan);
        orient.add(new QualityControlAnimator());
        orient.add(new LocationAnimator(treeNodes));
        orient.add(new RepaintAction());
        m_vis.putAction("orient", orient);
        
        // ------------------------------------------------
        
        // initialize the display
        setSize(700,600);
        setItemSorter(new TreeDepthItemSorter());
        addControlListener(new ZoomToFitControl());
        addControlListener(new ZoomControl());
        addControlListener(new WheelZoomControl());
        addControlListener(new PanControl());
        addControlListener(new FocusControl(1, "filter"));
        
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_LEFT_RIGHT),
            "left-to-right", KeyStroke.getKeyStroke("ctrl 1"), WHEN_FOCUSED);
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_TOP_BOTTOM),
            "top-to-bottom", KeyStroke.getKeyStroke("ctrl 2"), WHEN_FOCUSED);
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_RIGHT_LEFT),
            "right-to-left", KeyStroke.getKeyStroke("ctrl 3"), WHEN_FOCUSED);
        registerKeyboardAction(
            new OrientAction(Constants.ORIENT_BOTTOM_TOP),
            "bottom-to-top", KeyStroke.getKeyStroke("ctrl 4"), WHEN_FOCUSED);
        
        // ------------------------------------------------
        
        // filter graph and perform layout
        setOrientation(m_orientation);
        m_vis.run("filter");
       
        //TupleSet search = new PrefixSearchTupleSet(); 

	/*
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
        search.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                m_vis.cancel("animatePaint");
                m_vis.run("fullPaint");
                m_vis.run("animatePaint");
            }
        }); */
    }


    // ------------------------------------------------------------------------

    public void findDifference() {

	ArrayList  oldies = new ArrayList();
	ArrayList  newBuddies = new ArrayList();
	collect_all(cmpTree.getRoot(), oldies);
	collect_all(theViewTree.getRoot(), newBuddies);

	// old_one - new_one = left
	left = diffCollection(oldies, newBuddies);

	System.out.println("No people left:" + left.size());

        // new_one - old_one = joined
	joined = diffCollection(newBuddies, oldies);
	System.out.println("No people joined:" + joined.size());
	
	leftNodes = new HashMap();
	findLeftNodes(cmpTree.getRoot());
	
	// add LeftNodes to the new tree
	addLeftNodes(theViewTree.getRoot(), 0);

	//
	moved = new ArrayList();
	//findMoves(theViewTree.getRoot());
	//System.out.println("People moved:" + moved);
    }

    void collect_all(Node n, Collection collected) {
	collected.add(n.getString(0));
	Iterator iter = n.children();
	while (iter.hasNext()) {
	    collect_all((Node) iter.next(), collected);
	}
    }
		
    // Find people n1 but not in n2.
    public List diffCollection(Collection c1 , Collection c2) {
	List list = new ArrayList();
	Iterator iter = c1.iterator();
	while (iter.hasNext()) {
	    String name = (String) iter.next();
	    if (!c2.contains(name)) {
		list.add(name);
	    }
	}	
	return list;
    }	     

    // old tree
    static void findLeftNodes(Node n) {
	String name = n.getString(0);
	if (left.contains(name)) {
	    String parentName = n.getParent().getString(0);
	    List children = (List) leftNodes.get(parentName);
	    if (children == null) {
		children = new ArrayList();
	    }
	    children.add(n);
	    leftNodes.put(parentName, children);
	    if (leftNodes.size() == left.size()) {
		return;
	    }
	}
	if (n.getChildCount() != 0) {
	    Iterator iter = n.children();	   
	    while (iter.hasNext()) {
		findLeftNodes((Node) iter.next());
	    }
	}
    } 

    // new Tree
    void addLeftNodes(Node n, int nodesAdded) {
	String name = n.getString(0);
	List children;
	if ((children = (List) leftNodes.get(name)) != null) { 
	   for (int i = 0; i < children.size(); i++) {
		Node child = (Node) children.get(i); 	
		Node c = theViewTree.addChild(n);
		c.setString(0, child.getString(0));
		nodesAdded++;
	    }
	}	
	if (nodesAdded == left.size()) {
	    return;
	}
	if (n.getChildCount() != 0) {
	    Iterator iter = n.children();	   
	    while (iter.hasNext()) {
		addLeftNodes((Node) iter.next(), nodesAdded);
	    }
	}
    } 

    void findMoves(Node node) {
	compareParent(node);
	if (node.getChildCount() > 0) {
	    Iterator iter = node.children();	   
	    while (iter.hasNext()) {
	   	Node n = (Node) iter.next();
	   	findMoves(n);
	    }
	}
    }

    void compareParent(Node n) {
	   //System.out.println(" Comparing parents for:" + n);
	   Node n_cmp = findNodeByName(n.getString(0), cmpTree.getRoot());
	   if (n_cmp != null) { // found the node in the other tree
	       //System.out.println("node n:" + n);
	       Node p = theViewTree.getParent(n);
	       Node p_cmp = cmpTree.getParent(n_cmp);
               if (p != null && p_cmp != null) {
		    if (! (p.getString(0)).equals(p_cmp.getString(0))) {
		        moved.add(n.getString(0));
		    }
	       }
	    }
    }

    Node findNodeByName(String name, Node node) {
	TreeNodeIterator iter = new TreeNodeIterator(node);
	while (iter.hasNext()) {
	    Node n = (Node) iter.next();
	    if (n.getString(0).equals(name)) {
	         return n;
	    }
	}
	return null;
    }
	    
    static boolean hasMoved(VisualItem vi) {
	Tuple t = vi.getSourceTuple();
	Node n;
	if (t instanceof Node) {
	    n = (Node) t;
	    String name = n.getString(0);
	    if (moved.contains(name)) {
		return true;
	    }
	}
	return false;
    }
	
    static boolean isNewNode(VisualItem vi) {
	Tuple t = vi.getSourceTuple();
	Node n;
	if (t instanceof Node) {
	    n = (Node) t;
	    String name = n.getString(0);
	    if (joined.contains(name)) {
	        return true;
	    }
	} 
	return false;
    }

    static boolean isOldOnlyNode(VisualItem vi) {
	Tuple t = vi.getSourceTuple();
	Node n;
	if (t instanceof Node) {
	    n = (Node) t;
	    String name = n.getString(0);
	    if (left.contains(name)) {
	        return true;
	    }
	} 
	return false;
    }

    void updateNode(VisualItem vi) {
	Tuple t = vi.getSourceTuple();
	Node parent;
	if (t instanceof Node) {
	    parent = (Node) t;
	    String parentName = parent.getString(0);
	    List children = (List) leftNodes.get(parentName);
	    if (children != null) {
		for (int i = 0; i < children.size(); i++) {
		    theViewTree.addChildEdge(parent, (Node) children.get(i));
		}
	    }
	}
    }
    
    // ------------------------------------------------------------------------
    
    public void setOrientation(int orientation) {
        NodeLinkTreeLayout rtl 
            = (NodeLinkTreeLayout)m_vis.getAction("treeLayout");
        CollapsedSubtreeLayout stl
            = (CollapsedSubtreeLayout)m_vis.getAction("subLayout");
        switch ( orientation ) {
        case Constants.ORIENT_LEFT_RIGHT:
            m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
            m_edgeRenderer.setHorizontalAlignment1(Constants.RIGHT);
            m_edgeRenderer.setHorizontalAlignment2(Constants.LEFT);
            m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
            break;
        case Constants.ORIENT_RIGHT_LEFT:
            m_nodeRenderer.setHorizontalAlignment(Constants.RIGHT);
            m_edgeRenderer.setHorizontalAlignment1(Constants.LEFT);
            m_edgeRenderer.setHorizontalAlignment2(Constants.RIGHT);
            m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
            break;
        case Constants.ORIENT_TOP_BOTTOM:
            m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment1(Constants.BOTTOM);
            m_edgeRenderer.setVerticalAlignment2(Constants.TOP);
            break;
        case Constants.ORIENT_BOTTOM_TOP:
            m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
            m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
            m_edgeRenderer.setVerticalAlignment1(Constants.TOP);
            m_edgeRenderer.setVerticalAlignment2(Constants.BOTTOM);
            break;
        default:
            throw new IllegalArgumentException(
                "Unrecognized orientation value: "+orientation);
        }
        m_orientation = orientation;
        rtl.setOrientation(orientation);
        stl.setOrientation(orientation);
    }
    
    public int getOrientation() {
        return m_orientation;
    }
    
    // ------------------------------------------------------------------------
    
    public static void main(String argv[]) {
	String orgRoot = null;
	String viewStr = null;
	String cmpStr = null;

        if ( argv.length > 1 ) {
             orgRoot = argv[0];
             viewStr = argv[1];
	      if (argv.length > 2) {
                cmpStr = argv[2];
	      }
	      if (cmpStr == null) {
		cmpStr = viewStr;
	      }
              OrgViewer o = new OrgViewer(orgRoot, viewStr, cmpStr);
	      showOrg(o);
        } else {
	    System.out.println("Usage: OrgViewer <org root's first name> <date of org (MONTH-DAY-YEAR)> [date of org to compare with]"); 
	    System.out.println("For example: java OrgViewer Jonathan  7-1-2009  10-1-2009");
	}
    }

    public static void showOrg(OrgViewer viewer) {
        String label = "name";
        JComponent view = viewer.getConfiguredView(label);
        JFrame frame = new JFrame("D e m o - o r g c h a r t |  t r e e v i e w");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(view);
        frame.pack();
        frame.setVisible(true);
    }

    static  Color BACKGROUND = Color.WHITE;
    static Color FOREGROUND = Color.BLACK;
    
    public JComponent getConfiguredView(String label) {
	doVisualSetup(label);
        
        // create a new treemap
        setBackground(BACKGROUND);
        setForeground(FOREGROUND);
	
	ImageIcon logo = Connect.createImageIcon("images/pipal-logo.png", 50, 50);
	JLabel logoLabel = new JLabel("", logo, SwingConstants.CENTER);
	JLabel legend1 = (new OrgLegend(ColorLib.getColor(0, 100, 0), "Manager")).get();
	JLabel legend2 = (new OrgLegend(ColorLib.getColor(34, 139, 34), "Individual Contributor")).get();
	JLabel legend3 = (new OrgLegend(ColorLib.getColor(0, 0, 255), "New-Joinee")).get();
	JLabel legend4 = (new OrgLegend(ColorLib.getColor(255, 0, 0), "Ex-employee")).get();
	JLabel legend5 = (new OrgLegend(ColorLib.getColor(255,20,147), "Moved-employee")).get();
	JLabel legend6 = (new OrgLegend('i', "People information")).getKeyLegend();
	JLabel legend7 = (new OrgLegend('g', "Geographic distribution")).getKeyLegend();
        
        // create a search panel for the tree map
        //JSearchPanel search = new JSearchPanel(getVisualization(),
        //    treeNodes, Visualization.SEARCH_ITEMS, label, true, true);
        //search.setShowResultCount(true);

	SearchTupleSet search = new PrefixSearchTupleSet();
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
        search.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                m_vis.cancel("animatePaint");
                m_vis.run("fullPaint");
                m_vis.run("animatePaint");
            }
        });
	SearchQueryBinding sq = new SearchQueryBinding((Table) m_vis
                .getGroup(treeNodes), label, search);

        // set up search box
        JSearchPanel searcher = sq.createSearchPanel();
        searcher.setShowResultCount(true);
        searcher.setLabelText("Search: ");
        searcher.setBorder(BorderFactory.createEmptyBorder(5,5,4,0));
        searcher.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
        searcher.setBackground(BACKGROUND);
        searcher.setForeground(FOREGROUND);

        // construct the filtering predicate
        //AndPredicate filter = new AndPredicate(searchQ.getPredicate());
        
	String status = "";
	if (joined.size() > 0) {
	   status = "Joined #:" + joined.size();
	} 
	if (left.size() > 0) {
	   if (!status.equals("")) {
		status += ",  ";
	   }
	   status += "Left #:" + left.size();
	}
	if (status == "") {
	    status = orgRoot + "'s organization";
	}
        final JFastLabel statusLabel = new JFastLabel(status);
        statusLabel.setPreferredSize(new Dimension(450, 20));
        statusLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
        statusLabel.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
        statusLabel.setBackground(BACKGROUND);
        statusLabel.setForeground(FOREGROUND);
	
	// set the date of the spinner based on the filename.
        JSpinner spinner1 = createDateSpinner(viewDate);
        JLabel dateLabel1 = getLabel("You are viewing org of:");
        dateLabel1.setLabelFor(spinner1);

        JSpinner spinner2 = createDateSpinner(cmpDate);
        JLabel dateLabel2 = getLabel("Compare with");
        dateLabel2.setLabelFor(spinner2);

        addControlListener(new ControlAdapter() {
	    GeographyChart g;

            @Override public void itemEntered(VisualItem item, MouseEvent e) {

		// Sets the keyboard focus back to display
		item.getVisualization().getDisplay(0).requestFocusInWindow();

		Tuple t = item.getSourceTuple();
        	/*Node n;
		if (isSunOrg) {
        	    if (t instanceof Node) {
            	        n = (Node) t;
            	        String empId = n.getString(1); //shd not be hard-coded
		        item.getVisualization().getDisplay(0).setToolTipText("<html><img src=\"" +
      				sunPhotoUrl + empId + ".jpg\"" +
				" width=\"60\" height=\"50\"" +
      				">");
		    } 
		} */
                if ( item.canGetString(m_label) ) {
                    statusLabel.setText(item.getString(m_label));
		}
	    }
	    @Override public void itemClicked(VisualItem item, MouseEvent e) {
		//if (item.isExpanded() && !isLeafNode(item)) {
		//     g = new GeographyChart(item.getString(m_label));		    
		if (gChart != null) {
		    gChart.closeChart();		    
		}
		if (pplCard != null) {
		    pplCard.dispose();
		}
            }
            @Override public void itemExited(VisualItem item, MouseEvent e) {
                statusLabel.setText(null);

		//if (gChart != null) {
		//    gChart.closeChart();		    
		//}
            }
	    @Override public void itemKeyTyped(VisualItem item, KeyEvent e) { 
		if (e.getKeyChar() == 'g' ||
			e.getKeyChar() == 'G') {
		    if (!isLeafNode(item)) {
			 Tuple t = item.getSourceTuple();
        		 Node n = (Node) t;
			 gChart.doChart(item.getString(m_label), n.getString(3)); // do not hard-code
		    }
		}
		else if (e.getKeyChar() == 'c' ||
			e.getKeyChar() == 'c') {
		    if (gChart != null) {
		    	    gChart.closeChart();		    
		    }
		} else if (e.getKeyChar() == 'i' ||
			e.getKeyChar() == 'I') {
		    if (pplCard != null) {
			pplCard.dispose();
		    }
		    Tuple t = item.getSourceTuple();
        	    Node n = (Node) t;
		    String photoUrl = null;
		    if (isSunOrg) {
            	        String empId = n.getString(1); //shd not be hard-coded
    		        photoUrl = sunPhotoUrl + empId + ".jpg";
			String namefinderUrl = sunNameFinder + empId;
		        pplCard = Connect.getPplCard(item.getString(m_label), photoUrl, namefinderUrl);
		    } else {
		        pplCard = Connect.getPplCard(item.getString(m_label), null, null);
		    }
		}
	    }
        });
        spinner1.addChangeListener(new ChangeListener () {
	    public void stateChanged(ChangeEvent e) {
		Object o = e.getSource();
        	if (o instanceof JSpinner) {
		    JSpinner jsp = (JSpinner) o;
            	    SpinnerDateModel model = (SpinnerDateModel) jsp.getModel();
            	    Date date = model.getDate();
		    String filename = getNameByDate(date, orgRoot);
            	    System.out.println("changed date:" + date + " filename:" + filename + " of org-in-view");
		    if (viewNeedsUpdate(date)) {
			File f = new File (filename);
			if (!f.exists()) {
			    System.out.println("WARNING: The required file data:" + filename +
						" does not exists");
			} else {
        		    OrgViewer org = new OrgViewer(date, cmpDate, filename, cmpDataFile);
			    showOrg(org);
			} 
		   }
		}
	    }
          } );
        spinner2.addChangeListener(new ChangeListener () {
	    public void stateChanged(ChangeEvent e) {
		Object o = e.getSource();
        	if (o instanceof JSpinner) {
		    JSpinner jsp = (JSpinner) o;
            	    SpinnerDateModel model = (SpinnerDateModel) jsp.getModel();
            	    Date date = model.getDate();
		    String filename = getNameByDate(date, orgRoot);
            	    System.out.println("changed date:" + date + " filename:" + filename + " of org-for-comparing");
		    if (cmpNeedsUpdate(date)) {
			File f = new File (filename);
			if (!f.exists()) {
			    System.out.println("WARNING: The required file data:" + filename +
						" does not exists");
			} else {
        		    OrgViewer org = new OrgViewer(viewDate, date, viewDataFile, filename);
			    showOrg(org);
			} 
		   }
	        }
	    }
          } );

        Box boxY = new Box(BoxLayout.Y_AXIS);
        boxY.add(Box.createVerticalStrut(6));
	boxY.add(legend6);
        boxY.add(Box.createVerticalStrut(6));
	boxY.add(legend7);
	int legendStart = getSize().height - 150;
	System.out.println("legend start:" + legendStart);
        boxY.add(Box.createVerticalStrut(legendStart));
	boxY.add(legend1);
	boxY.add(legend2);
	boxY.add(legend3);
	boxY.add(legend4);
	boxY.add(legend5);
        boxY.setBackground(BACKGROUND);

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(5));
        box.add(statusLabel);
        box.add(Box.createHorizontalGlue());
        box.add(dateLabel1);
        box.add(spinner1);
        box.add(Box.createHorizontalStrut(10));
        box.add(dateLabel2);
        box.add(spinner2);
        box.add(Box.createHorizontalStrut(5));
        box.add(searcher);
        box.setBackground(BACKGROUND);

        Box boxLogo = new Box(BoxLayout.Y_AXIS);
	boxLogo.add(logoLabel);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setForeground(FOREGROUND);
        panel.add(boxY, BorderLayout.EAST);
        panel.add(boxLogo, BorderLayout.WEST);
        panel.add(this, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);
        return panel;
    }

    boolean viewNeedsUpdate(Date newDate) {
	if (viewDate.equals(newDate)) {
	    return false;
	} else {
	    return true;
	}
    }

    boolean cmpNeedsUpdate(Date newDate) {
	if (cmpDate.equals(newDate)) {
	    return false;
	} else {
	    return true;
	}
    }
    
   Date getDateByName(String date) {
	StringTokenizer st = new StringTokenizer(date, "-");
        int month = nextTokenToInt(st);
	month--; // Calendar API uses 0-11 for numbering months
        int day = nextTokenToInt(st);
        int year = nextTokenToInt(st);
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.MONTH, month);
	calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    int nextTokenToInt(StringTokenizer st) {
	String str = null;
	if (st.hasMoreTokens()) {
	    str = st.nextToken();
	}
	if (str == null) {
	    System.out.println("Error parsing the date, use MM-DD-yyyy format");
	    System.exit(1);
	}
	int val = -1;
	try {
	    val = Integer.parseInt(str); 
	} catch (Exception e) {
	    System.out.println("Error parsing the date, use MM-DD-yyyy format");
	    System.exit(1);
	}
	return val;
    }
	
    static String getNameByDate(Date date, String root) {
	Calendar cal = Calendar.getInstance();
	cal.setTime(date);
	StringBuilder fileBuf = new StringBuilder();
	fileBuf.append(root);
	fileBuf.append('-');
	fileBuf.append(String.valueOf(cal.get(Calendar.MONTH) + 1));		
	fileBuf.append('-');
	fileBuf.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));		
	fileBuf.append('-');
	fileBuf.append(String.valueOf(cal.get(Calendar.YEAR)));		
	fileBuf.append('-');
	fileBuf.append(ORG_FILE_SUFFIX);
	return fileBuf.toString();
    }

    static boolean isLeafNode(VisualItem vi) {
	Tuple t = vi.getSourceTuple();
	Node n;
	if (t instanceof Node) {
	    n = (Node) t;
	    //System.out.println("# children:" + n.getChildCount());
	    if (n.getChildCount() > 0) {
	        return false;
	    }
	} 
	return true;
    }
    
    // ------------------------------------------------------------------------
   
    public class OrientAction extends AbstractAction {
        private int orientation;
        
        public OrientAction(int orientation) {
            this.orientation = orientation;
        }
        public void actionPerformed(ActionEvent evt) {
            setOrientation(orientation);
            getVisualization().cancel("orient");
            getVisualization().run("tree");
            getVisualization().run("orient");
        }
    }
    
    public class AutoPanAction extends Action {
        private Point2D m_start = new Point2D.Double();
        private Point2D m_end   = new Point2D.Double();
        private Point2D m_cur   = new Point2D.Double();
        private int     m_bias  = 150;
        
        @Override
		public void run(double frac) {
            TupleSet ts = m_vis.getFocusGroup(Visualization.FOCUS_ITEMS);
            if ( ts.getTupleCount() == 0 )
                return;
            
            if ( frac == 0.0 ) {
                int xbias=0, ybias=0;
                switch ( m_orientation ) {
                case Constants.ORIENT_LEFT_RIGHT:
                    xbias = m_bias;
                    break;
                case Constants.ORIENT_RIGHT_LEFT:
                    xbias = -m_bias;
                    break;
                case Constants.ORIENT_TOP_BOTTOM:
                    ybias = m_bias;
                    break;
                case Constants.ORIENT_BOTTOM_TOP:
                    ybias = -m_bias;
                    break;
                }

                VisualItem vi = (VisualItem)ts.tuples().next();
                m_cur.setLocation(getWidth()/2, getHeight()/2);
                getAbsoluteCoordinate(m_cur, m_start);
                m_end.setLocation(vi.getX()+xbias, vi.getY()+ybias);
            } else {
                m_cur.setLocation(m_start.getX() + frac*(m_end.getX()-m_start.getX()),
                                  m_start.getY() + frac*(m_end.getY()-m_start.getY()));
                panToAbs(m_cur);
            }
        }
    }
    
    public static class NodeColorAction extends ColorAction {
        public NodeColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }
        
        @Override
		public int getColor(VisualItem item) {
            if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
                return ColorLib.rgb(255,190,190);
            else if ( m_vis.isInGroup(item, Visualization.FOCUS_ITEMS) )
                return ColorLib.rgb(198,229,229);
            else if ( item.getDOI() > -1 )
                return ColorLib.rgb(164,193,193);
            else
                return ColorLib.rgba(255,255,255,0);
        }
        
    } // end of inner class TreeMapColorAction

    public JSpinner createDateSpinner(Date initDate) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(initDate);
        calendar.add(Calendar.YEAR, -1);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        SpinnerModel dateModel = new SpinnerDateModel(initDate,
                                     earliestDate,
                                     latestDate,
                                     Calendar.YEAR);//ignored for user input
        JSpinner spinner = new JSpinner (dateModel);
	spinner.setPreferredSize(new Dimension(90, 20));
        JSpinner.DateEditor e = new JSpinner.DateEditor(spinner, "MM/dd/yy");
	e.setFont(new Font("Tahoma", Font.PLAIN, 11));
        spinner.setEditor(e);
	return spinner;
    }

    public JLabel getLabel(String name) {
	JLabel label = new JLabel(name);
        label.setBorder(BorderFactory.createEmptyBorder(5,5,4,0));
        label.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
        label.setBackground(BACKGROUND);
        label.setForeground(FOREGROUND);
	return label;
    }
}
