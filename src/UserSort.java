import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;


public class UserSort extends JFrame implements ActionListener{
    DummyUserData userData;
    UserSortPanel display;
    
    public UserSort() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        
        userData = new DummyUserData(6, 5, 6);
        
        JPanel optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));
        UserLegendPanel usrPanel = new UserLegendPanel(userData);
        usrPanel.setUserSort(this);
        optPanel.add(usrPanel);
        
        JLabel lbl = new JLabel("Sort by");
        optPanel.add(lbl);
        
        ButtonGroup btnGrp = new ButtonGroup();
        JRadioButton btn = new JRadioButton(UserSortPanel.SORT_NONE);
        btn.setActionCommand(UserSortPanel.SORT_NONE);
        btn.addActionListener(this);
        btnGrp.add(btn);
        optPanel.add(btn);
        btn.setSelected(true);
        btn = new JRadioButton(UserSortPanel.SORT_TOTAL);
        btn.setActionCommand(UserSortPanel.SORT_TOTAL);
        btn.addActionListener(this);
        btnGrp.add(btn);
        optPanel.add(btn);
        btn = new JRadioButton(UserSortPanel.SORT_ALL);
        btn.setActionCommand(UserSortPanel.SORT_ALL);
        btn.addActionListener(this);
        btnGrp.add(btn);
        optPanel.add(btn);
        for (String crit : userData.users.values().iterator().next().criteria) {
            btn = new JRadioButton(crit);
            btn.setActionCommand(crit);
            btn.addActionListener(this);
            btnGrp.add(btn);
            optPanel.add(btn);
        }
        mainPanel.add(optPanel);

        display = new UserSortPanel(userData);
        JScrollPane scrollPane = new JScrollPane(display);
        mainPanel.add(scrollPane);
        
        WindowListener exitListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (VCFeatureDemo.frame != null) {
                    VCFeatureDemo.frame.setVisible(true);
                } else {
                    System.exit(0);
                }
            }
        };
        addWindowListener(exitListener);
        setContentPane(mainPanel);
        pack();
        setVisible(true);
    }

    public void setSelectedUser(String legendName) {
        display.setSelectedUser(legendName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() instanceof JRadioButton) {
            if (((JRadioButton) ae.getSource()).isSelected()) {
                display.sortOption = ae.getActionCommand();
                display.repaint();
            }
        }
    }
}
