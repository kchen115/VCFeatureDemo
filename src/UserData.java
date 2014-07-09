import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

    public class UserData {
        TreeMap<String, Double> criteria_weight;
        //  alternative, criteria
        TreeMap<String, HashMap<String, Double>> alternative_multiplier;
        Vector<String> criteria;
        int alternatives;
        Color userColor;
        
        public UserData(int numCriteria, int numAlternatives) {
            criteria_weight = new TreeMap<String, Double>();
            alternative_multiplier = new TreeMap<String, HashMap<String,Double>>();
            criteria = new Vector<String>();
            for (int i = 0; i < numCriteria; i++) {
                criteria.add("Objective" + DummyUserData.alphabet.charAt(i));
            }
            alternatives = numAlternatives;
            generateRandomWeights();
        }
        
        public Color getUserColor() {
            return userColor;
        }

        public void setUserColor(Color userColor) {
            this.userColor = userColor;
        }

        public void generateRandomWeights() {
            Random rand = new Random();
            int numCriteria = criteria.size();
            double remWeight = 1;
            double wt;
            ArrayList<Double> weights = new ArrayList<Double>();
            for (int i = 0; i < numCriteria-1; i++) {
                wt = (rand.nextGaussian()*0.1 + 0.5)*remWeight;
                if (wt > remWeight) wt = remWeight;
                weights.add(wt);
                remWeight -= wt;
            }
            weights.add(remWeight);
            Collections.shuffle(weights);
            for (int i = 0 ; i < numCriteria; i++) {
                String c = criteria.get(i);
                criteria_weight.put(c, weights.get(i));
            }
            for (int i = 1; i <= alternatives; i++) {
                HashMap<String, Double> altMap = new HashMap<String, Double>();
                for (String crit : criteria) {
                    wt = ((double) Math.round(Math.random()*10))/10;
                    altMap.put(crit, wt);
                }
                alternative_multiplier.put("Alt" + i, altMap);
            }
        }
    }