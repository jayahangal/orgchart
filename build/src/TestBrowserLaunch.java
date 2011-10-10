import java.awt.event.*;
import javax.swing.*;

public class TestBrowserLaunch {

   public static void main(String[] args) {
      JFrame frame = new JFrame();
      JPanel panel = new JPanel();
      final JTextField urlField =
         new JTextField("http://www.centerkey.com       ");
      JButton webButton = new JButton("Web Trip");
      webButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            BareBonesBrowserLaunch.openURL(urlField.getText().trim()); }
         } );
      frame.setTitle("Bare Bones Browser Launch");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      panel.add(new JLabel("URL:"));
      panel.add(urlField);
      panel.add(webButton);
      frame.getContentPane().add(panel);
      frame.pack();
      frame.setVisible(true);
      }

   }
