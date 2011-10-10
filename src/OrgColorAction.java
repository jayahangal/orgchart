
import prefuse.action.assignment.ColorAction;
import prefuse.visual.VisualItem;
import prefuse.data.expression.Predicate;
import prefuse.util.ColorLib;

class OrgColorAction extends ColorAction {

   public OrgColorAction(String grp, Predicate p, String field) {
	super(grp, p , field);
   } 

    public int getColor(VisualItem item) {
	if (TreeView.isLeafNode(item)) {
	    return ColorLib.rgb(50, 205, 50);
	} else {
	    return ColorLib.rgb(0, 100, 0);
	}
    }
}
