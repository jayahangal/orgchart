import prefuse.visual.VisualItem;
import prefuse.render.LabelRenderer;
import prefuse.data.Tuple;
import prefuse.data.Node;

class OrgLabelRenderer extends LabelRenderer {

    public String getText (VisualItem item) {
	Tuple t = item.getSourceTuple();
        Node n;
        if (t instanceof Node) {
            n = (Node) t;
            String name = n.getString(0);
	    int number = n.getInt(2); //shd not be hard-coded
	    if (number == 0) {
		return name;
	    } else {
		return name + " (" + number + ")";
	    }
        }
	return "";
    }
}
