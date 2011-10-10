import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
	
class OrgLegend {

      Color color;
      String label;
      Rectangle r;
      char letter;

    public OrgLegend(Color color, String label) {
	this.color = color;
	this.label = label;
    }

    public OrgLegend(char letter, String label) {
	this.letter = letter;
	this.label = label;
    }

    JLabel get() {
	r = new Rectangle(new Dimension(10,10));
        Icon icon = new Icon(){
		public int getIconHeight() {
		    return 10;
		} 
	 	public int getIconWidth() {
		    return 10;
		} 
		public void paintIcon(Component c, Graphics g, int x, int y) {
        		g.setColor(color);
        		g.fillRect(x, y, r.width, r.height);
		}
	};
        JLabel jlabel = new JLabel(label, icon, JLabel.CENTER);

        //Set the position of its text, relative to its icon:
        jlabel.setVerticalTextPosition(JLabel.CENTER);
        jlabel.setHorizontalTextPosition(JLabel.RIGHT);
	return jlabel;
    }

    JLabel getKeyLegend() {
	ImageIcon icon = null;
	if (letter == 'i') {
	    icon = Connect.createImageIcon("images/i-block.jpg", 23, 23);	
	} else if (letter == 'g') {
	    icon = Connect.createImageIcon("images/g-block.jpg", 23, 23);	
	}
        JLabel jlabel = new JLabel(label, icon, JLabel.CENTER);

        //Set the position of its text, relative to its icon:
        jlabel.setVerticalTextPosition(JLabel.CENTER);
        jlabel.setHorizontalTextPosition(JLabel.RIGHT);
	return jlabel;
    }

    public void paint(Graphics g) {
        g.setColor(color);
        g.fillRect(r.x, r.y, r.width, r.height);
    }
}

 
 	

	

      
