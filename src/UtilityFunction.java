import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


public class UtilityFunction extends JFrame implements ActionListener{

    ContinuousUtilityGraph display;
    
    public UtilityFunction() {
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        
        btnPanel.add(new JLabel("Interpolation"));
        ButtonGroup grp = new ButtonGroup();
        JRadioButton btn = new JRadioButton("Linear");
        btn.setActionCommand("Linear");
        btn.addActionListener(this);
        grp.add(btn);
        btnPanel.add(btn);
        btn.setSelected(true);
        btn = new JRadioButton("Spline");
        btn.setActionCommand("Spline");
        btn.addActionListener(this);
        grp.add(btn);
        btnPanel.add(btn);
        
        mainPanel.add(btnPanel);
        
        int pts = 5;
        display = new ContinuousUtilityGraph(generateEqualX(pts, 1, 10),
                                                                    generateLinearY(pts));
        mainPanel.add(display);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (VCFeatureDemo.frame != null) {
                    VCFeatureDemo.frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            }
        });
        //moving
        
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        pack();
        setVisible(true);
        
        
    }
    
    public double[] generateEqualX(int numPts, double minX, double maxX) {
        double[] x = new double[numPts];
        for (int i = 0; i < numPts; i++) {
            x[i] = minX + ((double)i)*(maxX-minX)/(numPts-1);
        } 
        
        return x;
    }
    
    public double[] generateLinearY(int numPts) {
        double[] y = new double[numPts];
        for (int i = 0; i < numPts; i++) {
            y[i] = ((double)i)/(numPts-1);
        } 
        
        return y;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Linear")) {
            display.plotType = ContinuousUtilityGraph.PLOT_LINEAR;
            display.plotPoints();
        } else if (ae.getActionCommand().equals("Spline")) {
            display.plotType = ContinuousUtilityGraph.PLOT_SPLINE;
            display.plotPoints();
        }
    }
    
}
