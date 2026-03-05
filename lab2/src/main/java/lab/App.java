package lab;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class App extends JPanel implements ActionListener {
    private Timer timer;
    
    // Animation variables
    private double angle = 0; 
    private float alpha = 1.0f; 
    private float deltaAlpha = -0.015f;

    private static int maxWidth;
    private static int maxHeight;

    public App() {
        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, maxWidth + 1, maxHeight + 1);

        // Draw a frame
        BasicStroke frameStroke = new BasicStroke(15, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        g2d.setStroke(frameStroke);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(40, 40, maxWidth - 80, maxHeight - 160);

        double radiusOfMovement = 70.0;
        double centerX = maxWidth / 2.0;
        double centerY = maxHeight / 2.0;
        
        double tx = centerX + radiusOfMovement * Math.cos(angle);
        double ty = centerY + radiusOfMovement * Math.sin(angle) - 40;
        
        g2d.translate(tx, ty);

        // Setting transparency for shapes
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        drawTarget(g2d);
    }

    private void drawTarget(Graphics2D g2d) {
        // Circle radii
        double[] radii = {300, 255, 210, 165, 120, 75};

        // Gradient fill (for blue circles)
        GradientPaint blueGradient = new GradientPaint(
                -300, -300, Color.CYAN,
                 300,  300, Color.BLUE,
                 true);                 

        // Drawing circles: Standard primitive
        for (int i = 0; i < radii.length; i++) {
            double r = radii[i];
            Ellipse2D.Double circle = new Ellipse2D.Double(-r, -r, r * 2, r * 2);

            if (i < 2) {
                g2d.setPaint(blueGradient); // Use a gradient
            } else if (i < 4) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.YELLOW);
            }
            
            g2d.fill(circle);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(circle);
        }

        // A figure built from points
        // Making the central cross as a full polygon
        GeneralPath cross = new GeneralPath();
        double s = 2;  // Thickness
        double l = 20; // Length
        
        cross.moveTo(-s, -s);
        cross.lineTo(-s, -l); // Up
        cross.lineTo(s, -l);
        cross.lineTo(s, -s);
        cross.lineTo(l, -s);  // Right
        cross.lineTo(l, s);
        cross.lineTo(s, s);
        cross.lineTo(s, l);   // Down
        cross.lineTo(-s, l);
        cross.lineTo(-s, s);
        cross.lineTo(-l, s);  // Left
        cross.lineTo(-l, -s);
        cross.closePath();

        g2d.setColor(Color.BLACK);
        g2d.fill(cross);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Animation: Circular motion
        angle += 0.04; 

        // Animation: Changing transparency
        alpha += deltaAlpha;
        if (alpha < 0.1f) {
            alpha = 0.1f;
            deltaAlpha = -deltaAlpha;
        } else if (alpha > 1.0f) {
            alpha = 1.0f;
            deltaAlpha = -deltaAlpha;
        }

        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Target Animation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        
        frame.add(new App());
        frame.setVisible(true);

        Dimension size = frame.getSize();
        Insets insets = frame.getInsets();
        maxWidth = size.width - insets.left - insets.right - 1;
        maxHeight = size.height - insets.top - insets.bottom - 1;
    }
}
