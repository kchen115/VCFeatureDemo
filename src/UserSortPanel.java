import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;


public class UserSortPanel extends JPanel{
    public static final String SORT_ALL = "All";
    public static final String SORT_TOTAL = "Total Score";
    public static final String SORT_NONE = "None";
    
    String sortOption = SORT_NONE;
    
    DummyUserData userData;
    HashMap<String, Double> maxWeight;
    // alternative, user, total score
    TreeMap<String, HashMap<String, Double>> totalScores;
    TreeMap<String, ArrayList<Pair<Double,String>>> sortedTotalScores;
    // alternative, criteria, user, scores
    TreeMap<String, TreeMap<String, HashMap<String, Double>>> scores;
    TreeMap<String, TreeMap<String, ArrayList<Pair<Double,String>>>> sortedScores;
    String selectedUser = null;
    static final int colWidth = 20;
    static final int topHeight = 300;
    double scaleValue = 1; // always >= 1
    static final int panelsPadding = 5;
    static final int altPadding = 3;
    
    public UserSortPanel(DummyUserData data) {
        userData = data;
        calculateMaps();
    }

    public void calculateMaps() {
        maxWeight = new HashMap<String, Double>();
        for (UserData user : userData.users.values()) {
            for (Map.Entry<String, Double> val : user.criteria_weight.entrySet()) {
                String crit = val.getKey();
                double wt = val.getValue();
                if (!maxWeight.containsKey(crit) || maxWeight.get(crit) < wt) {
                    maxWeight.put(crit, wt);
                }
            }
        }
        
        scaleValue = 0;
        for (Double val : maxWeight.values()) {
            scaleValue += val;
        }
        
        // alternative, user, total score
        // TreeMap<String, HashMap<String, String>> totalScores;
        // alternative, criteria, user, scores
       //  TreeMap<String, TreeMap<String, HashMap<String, Double>>> scores;

        totalScores = new TreeMap<String, HashMap<String, Double>>();
        scores = new TreeMap<String, TreeMap<String, HashMap<String, Double>>>();
        
        for (Map.Entry<String,UserData> user : userData.users.entrySet()) {
            
            for (Map.Entry<String, HashMap<String, Double>> alt : user.getValue().alternative_multiplier.entrySet()) {
                TreeMap<String, Double> weights = user.getValue().criteria_weight;
                
                for (Map.Entry<String, Double> obj : alt.getValue().entrySet()) {
                    
                    double score = weights.get(obj.getKey())*obj.getValue();
                    String altKey = alt.getKey();
                    String critKey = obj.getKey();
                    String userKey = user.getKey();
                    
                    TreeMap<String,HashMap<String,Double>> altMap = scores.get(altKey);
                    HashMap<String, Double> totAltMap = totalScores.get(altKey);
                    if (altMap == null) {
                        altMap = new TreeMap<String,HashMap<String,Double>>();
                        scores.put(altKey, altMap);
                        totAltMap = new HashMap<String, Double>();
                        totalScores.put(altKey, totAltMap);
                    }
                    
                    HashMap<String,Double> objMap = altMap.get(critKey);
                    if (objMap == null) {
                        objMap = new HashMap<String,Double>();
                        altMap.put(critKey, objMap);
                    }
                    objMap.put(userKey, score);
                    
                    if (totAltMap.containsKey(userKey)) {
                        totAltMap.put(userKey, totAltMap.get(userKey) + score);
                    } else {
                        totAltMap.put(userKey, score);
                    }
                }
            }
        }
        
        sortScores();
    }
    
    public void sortScores() {
        sortedTotalScores = new TreeMap<String, ArrayList<Pair<Double,String>>>();
        sortedScores = new TreeMap<String, TreeMap<String, ArrayList<Pair<Double,String>>>>();
        
        for (Map.Entry<String, TreeMap<String, HashMap<String,Double>>> alt : scores.entrySet()) {
            ArrayList<Pair<Double, String>> totalSorted = new ArrayList<Pair<Double, String>>();
            for (Map.Entry<String, Double> user : totalScores.get(alt.getKey()).entrySet()) {
                totalSorted.add(new Pair<Double, String>(user.getValue(), user.getKey()));
            }
            Collections.sort(totalSorted, new Comparator<Pair<Double, String>>() {
                public int compare (Pair<Double, String> o1, Pair<Double, String> o2) throws ClassCastException { 
                    return o2.first.compareTo(o1.first);
                }
            });
            sortedTotalScores.put(alt.getKey(), totalSorted);
            
            TreeMap<String, ArrayList<Pair<Double,String>>> sortedAltMap = new TreeMap<String, ArrayList<Pair<Double,String>>>();
            for (Map.Entry<String, HashMap<String,Double>> obj : alt.getValue().entrySet()) {
                ArrayList<Pair<Double, String>> sorted = new ArrayList<Pair<Double, String>>();
                
                for (Map.Entry<String,Double> user : obj.getValue().entrySet()) {
                    sorted.add(new Pair<Double,String>(user.getValue(), user.getKey()));
                }
                Collections.sort(sorted, new Comparator<Pair<Double, String>>() {
                    public int compare (Pair<Double, String> o1, Pair<Double, String> o2) throws ClassCastException { 
                        return o2.first.compareTo(o1.first);
                    }
                });
                sortedAltMap.put(obj.getKey(), sorted);
                sortedScores.put(alt.getKey(), sortedAltMap);
            }
        }
                
    }
    
    public void setSelectedUser(String legendName) {
        selectedUser = legendName;
        repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(colWidth*userData.numAlternatives*userData.users.size() + userData.numAlternatives*2*altPadding, 
                            (int) (topHeight + scaleValue*topHeight + panelsPadding));
    }
    
    
    @Override
    public void paint(Graphics g) {
        if (sortOption.equals(SORT_ALL)) {
            paintSortAll(g);
        } else if (sortOption.equals(SORT_TOTAL)) {
            paintSortTotal(g);
        } else if (sortOption.equals(SORT_NONE)) {
            paintNormal(g);  
        } else if (sortOption != null && !sortOption.isEmpty()) {
            paintSort(g);
        } else {
            paintNormal(g);
        }
    }
    
    public void paintNormal(Graphics g) {
        int totalWidth = colWidth*userData.numAlternatives*userData.users.size() + userData.numAlternatives*2*altPadding;
        int totalHeight = (int) (topHeight + scaleValue*topHeight + panelsPadding);
        int altWidth = colWidth*userData.users.size() + 2*altPadding;
        
        ////////////////////
        // cumulative scores
        g.setColor(Color.white);
        g.fillRect(0, 0, totalWidth, totalHeight);
        
        int userXOffset = 0;
        for (Map.Entry<String, UserData> user : userData.users.entrySet()) {
            int altXOffset = altPadding;
            TreeMap<String, Double> weights = user.getValue().criteria_weight;
            TreeMap<String, HashMap<String, Double>> alt_mult = user.getValue().alternative_multiplier;
            
            for (HashMap<String, Double> alt : alt_mult.values()) {
                int YOffset = 0;
                int[] yPos = new int[weights.size()];
                int yIdx = 0;
                for (Map.Entry<String, Double> criteria : weights.entrySet()) {
                    double ratio = criteria.getValue()*alt.get(criteria.getKey());
                    yPos[yIdx++] = (int) Math.round(topHeight*ratio);
                }
                for (int i = yPos.length-1; i >=0; i--) {
                    int barHeight = yPos[i]+1;
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.getKey().equals(selectedUser))
                        userColor = user.getValue().userColor;
                    g.setColor(userColor);
                    g.fillRect(userXOffset + altXOffset, topHeight-barHeight-YOffset, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(userXOffset + altXOffset, topHeight-barHeight-YOffset, userXOffset + altXOffset + colWidth-1, topHeight-barHeight-YOffset);
                    g.drawLine(userXOffset + altXOffset + colWidth-1, 0, userXOffset + altXOffset + colWidth-1, topHeight-1);
                    YOffset += barHeight;
                }

                altXOffset += altWidth;
            }
            userXOffset += colWidth;
        }
        
        ///////////////
        // bars breakdown
        
        userXOffset = 0;
        for (Map.Entry<String, UserData> user : userData.users.entrySet()) {
            int altXOffset = altPadding;
            TreeMap<String, Double> weights = user.getValue().criteria_weight;
            TreeMap<String, HashMap<String, Double>> alt_mult = user.getValue().alternative_multiplier;
          
            
            for (HashMap<String, Double> alt : alt_mult.values()) {

                int YOffset = topHeight+panelsPadding;
                for (Map.Entry<String, Double> criteria : weights.entrySet()) {
                    double ratio = criteria.getValue()*alt.get(criteria.getKey());
                    int barHeight = (int) Math.round(ratio*topHeight);
                    
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.getKey().equals(selectedUser))
                        userColor = user.getValue().userColor;
                    g.setColor(userColor);
                    int maxHeight = (int) Math.round(maxWeight.get(criteria.getKey())*topHeight);
                    g.fillRect(userXOffset + altXOffset, YOffset + maxHeight-barHeight, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(0, YOffset, totalWidth-1, YOffset);
                    YOffset += maxHeight;
                }
                g.setColor(Color.lightGray);
                g.drawLine(userXOffset + altXOffset + colWidth-1, topHeight+panelsPadding, userXOffset + altXOffset + colWidth-1, totalHeight-1);

                altXOffset += altWidth;
            }
            userXOffset += colWidth;
        }
        
        ///////////////////
        // dividing lines
        
        g.setColor(Color.black);
        for (int i = 0; i < userData.numAlternatives; i++) {
            g.drawLine(i*altWidth, 0, i*altWidth, topHeight-1);
            g.drawLine(i*altWidth, topHeight+panelsPadding, i*altWidth, totalHeight);
        }
        g.drawLine(0, topHeight, totalWidth, topHeight);
    }

    
    public void paintSortAll(Graphics g) {
        int totalWidth = colWidth*userData.numAlternatives*userData.users.size() + userData.numAlternatives*2*altPadding;
        int totalHeight = (int) (topHeight + scaleValue*topHeight + panelsPadding);
        int altWidth = colWidth*userData.users.size() + 2*altPadding;
        
        ////////////////////
        // cumulative scores
        g.setColor(Color.white);
        g.fillRect(0, 0, totalWidth, totalHeight);
        
        int altXOffset = altPadding;
        for (Map.Entry<String, ArrayList<Pair<Double,String>>> alt : sortedTotalScores.entrySet()) {
            
            int userXOffset = 0;
            for (Pair<Double, String> user : alt.getValue()) {
                UserData data = userData.users.get(user.second);
                TreeMap<String, Double> weights = data.criteria_weight;
                HashMap<String, Double> alt_mult = data.alternative_multiplier.get(alt.getKey());
                
                int YOffset = 0;
                int[] yPos = new int[weights.size()];
                int yIdx = 0;
                for (Map.Entry<String, Double> criteria : weights.entrySet()) {
                    double ratio = criteria.getValue()*alt_mult.get(criteria.getKey());
                    yPos[yIdx++] = (int) Math.round(topHeight*ratio);
                }
                for (int i = yPos.length-1; i >=0; i--) {
                    int barHeight = yPos[i]+1;
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.second.equals(selectedUser))
                        userColor = data.userColor;
                    g.setColor(userColor);
                    g.fillRect(userXOffset + altXOffset, topHeight-barHeight-YOffset, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(userXOffset + altXOffset, topHeight-barHeight-YOffset, userXOffset + altXOffset + colWidth-1, topHeight-barHeight-YOffset);
                    g.drawLine(userXOffset + altXOffset + colWidth-1, 0, userXOffset + altXOffset + colWidth-1, topHeight-1);
                    YOffset += barHeight;
                }

                userXOffset += colWidth;
            }
            altXOffset += altWidth;
        }
        
        ///////////////
        // bars breakdown
        
        altXOffset = altPadding;
        for (Map.Entry<String, TreeMap<String, ArrayList<Pair<Double,String>>>> alt : sortedScores.entrySet()) {
            int YOffset = topHeight+panelsPadding;
            for (Map.Entry<String, ArrayList<Pair<Double,String>>> obj : alt.getValue().entrySet()) {
                
                int maxHeight = (int) Math.round(maxWeight.get(obj.getKey())*topHeight);
                int userXOffset = 0;
                for (Pair<Double, String> user : obj.getValue()) {
                    double ratio = user.first;
                    int barHeight = (int) Math.round(ratio*topHeight);
                    
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.second.equals(selectedUser))
                        userColor = userData.users.get(user.second).userColor;
                    g.setColor(userColor);
                    g.fillRect(userXOffset + altXOffset, YOffset + maxHeight-barHeight, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(0, YOffset, totalWidth-1, YOffset);
                    
                    g.setColor(Color.lightGray);
                    g.drawLine(userXOffset + altXOffset + colWidth-1, topHeight+panelsPadding, userXOffset + altXOffset + colWidth-1, totalHeight-1);
                    
                    userXOffset += colWidth;
                }
                
                YOffset += maxHeight;
            }
            altXOffset += altWidth;
        }
        
        ///////////////////
        // dividing lines
        
        g.setColor(Color.black);
        for (int i = 0; i < userData.numAlternatives; i++) {
            g.drawLine(i*altWidth, 0, i*altWidth, topHeight-1);
            g.drawLine(i*altWidth, topHeight+panelsPadding, i*altWidth, totalHeight);
        }
        g.drawLine(0, topHeight, totalWidth, topHeight);

    }
    
    public void paintSortTotal(Graphics g) {
        int totalWidth = colWidth*userData.numAlternatives*userData.users.size() + userData.numAlternatives*2*altPadding;
        int totalHeight = (int) (topHeight + scaleValue*topHeight + panelsPadding);
        int altWidth = colWidth*userData.users.size() + 2*altPadding;
        
        ////////////////////
        // cumulative scores
        g.setColor(Color.white);
        g.fillRect(0, 0, totalWidth, totalHeight);
        
        int altXOffset = altPadding;
        for (Map.Entry<String, ArrayList<Pair<Double,String>>> alt : sortedTotalScores.entrySet()) {
            
            int userXOffset = 0;
            for (Pair<Double, String> user : alt.getValue()) {
                UserData data = userData.users.get(user.second);
                TreeMap<String, Double> weights = data.criteria_weight;
                HashMap<String, Double> alt_mult = data.alternative_multiplier.get(alt.getKey());
                
                int YOffset = 0;
                int[] yPos = new int[weights.size()];
                int yIdx = 0;
                for (Map.Entry<String, Double> criteria : weights.entrySet()) {
                    double ratio = criteria.getValue()*alt_mult.get(criteria.getKey());
                    yPos[yIdx++] = (int) Math.round(topHeight*ratio);
                }
                for (int i = yPos.length-1; i >=0; i--) {
                    int barHeight = yPos[i]+1;
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.second.equals(selectedUser))
                        userColor = data.userColor;
                    g.setColor(userColor);
                    g.fillRect(userXOffset + altXOffset, topHeight-barHeight-YOffset, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(userXOffset + altXOffset, topHeight-barHeight-YOffset, userXOffset + altXOffset + colWidth-1, topHeight-barHeight-YOffset);
                    g.drawLine(userXOffset + altXOffset + colWidth-1, 0, userXOffset + altXOffset + colWidth-1, topHeight-1);
                    YOffset += barHeight;
                }

                userXOffset += colWidth;
            }
            altXOffset += altWidth;
        }
        
        ///////////////
        // bars breakdown
        
        altXOffset = altPadding;
        for (Map.Entry<String, TreeMap<String, ArrayList<Pair<Double,String>>>> alt : sortedScores.entrySet()) {
            int YOffset = topHeight+panelsPadding;
            for (Map.Entry<String, ArrayList<Pair<Double,String>>> obj : alt.getValue().entrySet()) {
                
                int maxHeight = (int) Math.round(maxWeight.get(obj.getKey())*topHeight);
                int userXOffset = 0;
                for (Pair<Double, String> user : sortedTotalScores.get(alt.getKey())) {
                    UserData data = userData.users.get(user.second);
                    TreeMap<String, Double> weights = data.criteria_weight;
                    HashMap<String, Double> alt_mult = data.alternative_multiplier.get(alt.getKey());
                    double ratio = weights.get(obj.getKey())*alt_mult.get(obj.getKey());
                    int barHeight = (int) Math.round(ratio*topHeight);
                    
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.second.equals(selectedUser))
                        userColor = userData.users.get(user.second).userColor;
                    g.setColor(userColor);
                    g.fillRect(userXOffset + altXOffset, YOffset + maxHeight-barHeight, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(0, YOffset, totalWidth-1, YOffset);
                    
                    g.setColor(Color.lightGray);
                    g.drawLine(userXOffset + altXOffset + colWidth-1, topHeight+panelsPadding, userXOffset + altXOffset + colWidth-1, totalHeight-1);
                    
                    userXOffset += colWidth;
                }
                
                YOffset += maxHeight;
            }
            altXOffset += altWidth;
        }
        
        ///////////////////
        // dividing lines
        
        g.setColor(Color.black);
        for (int i = 0; i < userData.numAlternatives; i++) {
            g.drawLine(i*altWidth, 0, i*altWidth, topHeight-1);
            g.drawLine(i*altWidth, topHeight+panelsPadding, i*altWidth, totalHeight);
        }
        g.drawLine(0, topHeight, totalWidth, topHeight);

    }
    
    public void paintSort(Graphics g) {
        int totalWidth = colWidth*userData.numAlternatives*userData.users.size() + userData.numAlternatives*2*altPadding;
        int totalHeight = (int) (topHeight + scaleValue*topHeight + panelsPadding);
        int altWidth = colWidth*userData.users.size() + 2*altPadding;
        
        ////////////////////
        // cumulative scores
        g.setColor(Color.white);
        g.fillRect(0, 0, totalWidth, totalHeight);
        
        int altXOffset = altPadding;
        for (Map.Entry<String, ArrayList<Pair<Double,String>>> alt : sortedTotalScores.entrySet()) {
            
            int userXOffset = 0;
            for (Pair<Double, String> user : sortedScores.get(alt.getKey()).get(sortOption)) {
                UserData data = userData.users.get(user.second);
                TreeMap<String, Double> weights = data.criteria_weight;
                HashMap<String, Double> alt_mult = data.alternative_multiplier.get(alt.getKey());
                
                int YOffset = 0;
                int[] yPos = new int[weights.size()];
                int yIdx = 0;
                for (Map.Entry<String, Double> criteria : weights.entrySet()) {
                    double ratio = criteria.getValue()*alt_mult.get(criteria.getKey());
                    yPos[yIdx++] = (int) Math.round(topHeight*ratio);
                }
                for (int i = yPos.length-1; i >=0; i--) {
                    int barHeight = yPos[i]+1;
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.second.equals(selectedUser))
                        userColor = data.userColor;
                    g.setColor(userColor);
                    g.fillRect(userXOffset + altXOffset, topHeight-barHeight-YOffset, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(userXOffset + altXOffset, topHeight-barHeight-YOffset, userXOffset + altXOffset + colWidth-1, topHeight-barHeight-YOffset);
                    g.drawLine(userXOffset + altXOffset + colWidth-1, 0, userXOffset + altXOffset + colWidth-1, topHeight-1);
                    YOffset += barHeight;
                }

                userXOffset += colWidth;
            }
            altXOffset += altWidth;
        }
        
        ///////////////
        // bars breakdown
        
        altXOffset = altPadding;
        for (Map.Entry<String, TreeMap<String, ArrayList<Pair<Double,String>>>> alt : sortedScores.entrySet()) {
            int YOffset = topHeight+panelsPadding;
            for (Map.Entry<String, ArrayList<Pair<Double,String>>> obj : alt.getValue().entrySet()) {
                
                int maxHeight = (int) Math.round(maxWeight.get(obj.getKey())*topHeight);
                int userXOffset = 0;
                for (Pair<Double, String> user : sortedScores.get(alt.getKey()).get(sortOption)) {
                    UserData data = userData.users.get(user.second);
                    TreeMap<String, Double> weights = data.criteria_weight;
                    HashMap<String, Double> alt_mult = data.alternative_multiplier.get(alt.getKey());
                    double ratio = weights.get(obj.getKey())*alt_mult.get(obj.getKey());
                    int barHeight = (int) Math.round(ratio*topHeight);
                    
                    Color userColor = Color.gray;
                    if (selectedUser == null || selectedUser.isEmpty() || user.second.equals(selectedUser))
                        userColor = userData.users.get(user.second).userColor;
                    g.setColor(userColor);
                    g.fillRect(userXOffset + altXOffset, YOffset + maxHeight-barHeight, colWidth, barHeight);
                    g.setColor(Color.lightGray);
                    g.drawLine(0, YOffset, totalWidth-1, YOffset);
                    
                    g.setColor(Color.lightGray);
                    g.drawLine(userXOffset + altXOffset + colWidth-1, topHeight+panelsPadding, userXOffset + altXOffset + colWidth-1, totalHeight-1);
                    
                    userXOffset += colWidth;
                }
                
                YOffset += maxHeight;
            }
            altXOffset += altWidth;
        }
        
        ///////////////////
        // dividing lines
        
        g.setColor(Color.black);
        for (int i = 0; i < userData.numAlternatives; i++) {
            g.drawLine(i*altWidth, 0, i*altWidth, topHeight-1);
            g.drawLine(i*altWidth, topHeight+panelsPadding, i*altWidth, totalHeight);
        }
        g.drawLine(0, topHeight, totalWidth, topHeight);

    }

    
}
