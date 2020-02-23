import java.util.*;

public class TrieNode {
	private TrieNode[] children;
    private ArrayList<GraphNode> myNodes;
    private ArrayList<String> possibleWords;
    private int size;

    public TrieNode() {
        this.children = new TrieNode[27];
        this.possibleWords = new ArrayList<>();
        myNodes = new ArrayList<>();
    }


    public void addWord(String s, GraphNode g) {
        if (s.isEmpty()) {
            myNodes.add(g);
            return;
        } else {
            if (s.charAt(0) == ' ') {
                possibleWords.add(s);
                if (this.children[26] == null) {
                    this.children[26] = new TrieNode();
                }
                this.children[26].addWord(s.substring(1), g);
            } else if (!Character.isUpperCase(s.charAt(0))) {
                int index = s.charAt(0) - 'a';
                possibleWords.add(s);
            } else {
                int index = s.charAt(0) - 'A';
                possibleWords.add(s);
                if (this.children[index] == null) {
                    this.children[index] = new TrieNode();
                }
                this.children[index].addWord(s.substring(1), g);
            }
        }
    }

    public ArrayList<String> autoComplete(String prefix, String myWord) {
        int index = 0;
        if (prefix.isEmpty()) {
            ArrayList<String> toReturn = new ArrayList<>();
            for (String A : possibleWords)
                toReturn.add(myWord + A);
            return toReturn;
        } else {
            if (prefix.charAt(0) == ' ') {
                index = 26;
            } else if (Character.isUpperCase(prefix.charAt(0))){
                index = prefix.charAt(0) - 'A';
            } else {
                index = prefix.charAt(0) - 'a';
            }
            if (this.children[index] == null) {
                return null;
            } else {
                return this.children[index].autoComplete(prefix.substring(1), myWord + prefix.charAt(0));
            }
        }

    }

    public ArrayList<Map<String,Object>> getNodes(String location) {
        if (location.isEmpty()) {
            ArrayList<Map<String, Object>> myMapList = new ArrayList<>();
            for (GraphNode g : myNodes) {
                Map<String, Object> myMap = new HashMap<>();
                myMap.put("lat", g.getLatitude());
                myMap.put("lon", g.getLongitude());
                myMap.put("name", location);
                myMap.put("id", g.getId());

                myMapList.add(myMap);
                return myMapList;
            }
        } else {
                int index = location.charAt(0) - 'a';
                if (this.children[index] == null) {
                    return null;
                } else {
                    return this.children[index].getNodes(location.substring(1));
                }
            }
        return null;
        }


}