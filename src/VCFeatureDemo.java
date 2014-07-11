import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.Vector;

import javax.swing.*;

public class VCFeatureDemo extends JPanel
                             implements ActionListener{
    private static final long serialVersionUID = 1L;

    JPanel pnlInfo;
    
    protected static JFrame frame;
    
    String[] options = {"User sorting", "Score function definition"};
    JList<String> lstOptions;    
    DefaultListModel<String> listModel;
    JScrollPane scrList;     
    JButton btnShow;
    JButton btnCancel;    
    JPanel pnlButtons;

    public VCFeatureDemo(){   
        
        pnlInfo = new JPanel(new GridLayout(0, 1));
        pnlInfo.setBorder(BorderFactory.createEtchedBorder(1));        
        pnlInfo.add(new JLabel("Please select a feature to demonstrate"));
        
        //Set up the File List
        listModel = new DefaultListModel<String>(); 
        for (String opt : options)
            listModel.addElement(opt);
        
        lstOptions = new JList<String>(listModel);
        lstOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstOptions.setSelectedIndex(0);
        lstOptions.setVisibleRowCount(10);   
        
        scrList = new JScrollPane(lstOptions);
      
        scrList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        //Set up file Open/Create and Cancel command buttons 
        btnShow = new JButton("Show");
        btnShow.addActionListener(this);
        btnShow.setActionCommand("btnShow");
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        btnCancel.setActionCommand("btnCancel");
        
        pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.LINE_AXIS));
        pnlButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        pnlButtons.add(Box.createHorizontalGlue());
        pnlButtons.add(btnShow);
        pnlButtons.add(Box.createRigidArea(new Dimension(10, 0)));
        pnlButtons.add(btnCancel);        
 
        //Layout all panels
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(pnlInfo);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(scrList);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(pnlButtons);

        setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
 
    }
    
    public void actionPerformed(ActionEvent e) {        
        if ("btnShow".equals(e.getActionCommand())) {
            frame.setVisible(false);
            showFeature();
        }
        else if ("btnCancel".equals(e.getActionCommand())) {
            System.exit(0);
        }
    }
    
    public void showFeature() {
        String feature = lstOptions.getSelectedValue().toString();
        if (feature.equals(options[0])) {
            new UserSort();
        } else if (feature.equals(options[1])) {
            new UtilityFunction();
        }
    }
  
    public Vector<String> getFileNames(String name, Vector<String> list){
        try{
            FileReader fr = new FileReader(name);
            BufferedReader br = new BufferedReader(fr);
            StreamTokenizer st = new StreamTokenizer(br);
            st.whitespaceChars(',', ',');
            while(st.nextToken() != StreamTokenizer.TT_EOF) {
                if(st.ttype==StreamTokenizer.TT_WORD)
                    list.add(st.sval);
            }
            fr.close();         
        }catch(Exception e) {
            System.out.println("Exception: " + e);
        }
        return list;
    }     
    
    public int countEntries(String name){
        int count=0;
        try{
            FileReader fr = new FileReader(name);
            BufferedReader br = new BufferedReader(fr);
            StreamTokenizer st = new StreamTokenizer(br);
            st.whitespaceChars(',', ',');
            while(st.nextToken() != StreamTokenizer.TT_EOF) {
                if(st.ttype==StreamTokenizer.TT_WORD)
                    if(st.sval.equals("entry")){
                        count++;
                    }
            }
            fr.close();         
        }catch(Exception e) {
            System.out.println("Exception: " + e);
        }
        return count;       
    }
    
    public static void showStartView() {
        //Create and set up the window.
        frame = new JFrame("ValueCharts Plus");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new VCFeatureDemo());      
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showStartView();
            }
        });
    }
}


