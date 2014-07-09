import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;


public class DummyUserData {
    
    LinkedHashMap<String, UserData> users;
    static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    int numCriteria;
    int numAlternatives;
    
    public DummyUserData(int numUsers, int numCriteria, int numAlternatives) {
        users = new LinkedHashMap<String, UserData>();
        for (int i = 0; i < numUsers; i++) {
            UserData user = new UserData(numCriteria, numAlternatives);
            user.setUserColor(UserLegendPanel.USER_COLORS.get(i));
            users.put("User" + (i+1), user);
        }
        this.numCriteria = numCriteria;
        this.numAlternatives = numAlternatives;
    }
}
