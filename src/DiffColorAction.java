
import prefuse.action.assignment.ColorAction;
import prefuse.visual.VisualItem;
import prefuse.data.expression.Predicate;
import prefuse.util.ColorLib;


class DiffColorAction extends ColorAction {

   public DiffColorAction(String grp, Predicate p, String field) {
	super(grp, p , field);
   } 

    public int getColor(VisualItem item) {
	if (OrgViewer.isNewNode(item)) {
	    return ColorLib.rgb(0, 0, 255);
	} else if (OrgViewer.isOldOnlyNode(item)) {
	    return ColorLib.rgb(255, 0, 0);
	} else if (OrgViewer.hasMoved(item)) {
	    return ColorLib.rgb(255, 20, 147); // peru 
	} else if (OrgViewer.isLeafNode(item)) {
	    return ColorLib.rgb(34, 139, 34);
	    //return ColorLib.rgb(50, 205, 50);
	} else {
	    return ColorLib.rgb(0, 100, 0);
	}
    }
}
