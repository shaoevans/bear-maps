
import java.util.*;


/**

 */
public class Trie {
    private HashMap<Character, TrieNode> firstChars;

    public Trie() {
        firstChars = new HashMap<>();
    }

    public void addLocation(String word, GraphNode information) {
        if (word == null || word.length() == 0) {
            return;
        }
        if (!firstChars.containsKey(word.charAt(0))) {
            firstChars.put(word.charAt(0), new TrieNode());
        }
        addHelper(word, information, firstChars.get(word.charAt(0)), 1);
    }

    private void addHelper(String word, GraphNode information, TrieNode node, int n) {
        if (n == word.length()) {
            node.information.add(information);
            return;
        }
        addHelper(word, information, node.getNode(word.charAt(n)), n + 1);
    }

    /**
     * Return the information associated with this location name in the Trie. Return
     * null if there is no information for the location name.
     */
    public List<Map<String, Object>> lookupInfo(String location) {
        if (location == null || location.length() == 0){
            return null;
        }
        if (firstChars.containsKey(location.charAt(0))) {
            return lookupHelper(location, firstChars.get(location.charAt(0)), 1);
        }
        return null;
    }

    private List<Map<String, Object>> lookupHelper(String location, TrieNode node, int n){
        if (n == location.length()){
            return node.getInfo();
        } else if (!node.map.containsKey(location.charAt(n))){
            return null;
        } else {
            return lookupHelper(location, node.map.get(location.charAt(n)), n+1);
        }
    }

    public List<String> lookupChildren(String location) {
        if (location == null || location.length() == 0){
            return null;
        }
        if (firstChars.containsKey(location.charAt(0))) {
            return lookupChildrenHelper(location, firstChars.get(location.charAt(0)), 1);
        }
        return null;
    }

    private List<String> lookupChildrenHelper(String location, TrieNode node, int n){
        if (n == location.length()){
            return node.getChildren();
        } else if (!node.map.containsKey(location.charAt(n))){
            return null;
        } else {
            return lookupChildrenHelper(location, node.map.get(location.charAt(n)), n+1);
        }
    }

    private class TrieNode {
        private HashMap<Character, TrieNode> map;
        List<GraphNode> information = new ArrayList<>();

        public TrieNode() {

            map = new HashMap<>();
        }

        public TrieNode getNode(Character c) {
            if (!map.containsKey(c)) {
                map.put(c, new TrieNode());
            }
            return map.get(c);
        }

        public List<String> getChildren(){
            LinkedList<String> children = new LinkedList<>();
            for (TrieNode n : map.values()){
                if (!n.information.isEmpty()){
                    children.add(n.information.get(0).locationName);
                    if (!n.map.isEmpty()){
                        children.addAll(n.getChildren());
                    }
                } else {
                    children.addAll(n.getChildren());
                }
            }
            return children;
        }

        public List<Map<String, Object>> getInfo(){
            LinkedList allInfo = new LinkedList();
            for (GraphNode node : information) {
                HashMap<String, Object> info = new HashMap<>();
                info.put("lat", node.latitude);
                info.put("lon", node.longitude);
                info.put("name", node.locationName);
                info.put("id", Long.parseLong(node.id));
                allInfo.add(info);
            }
            return allInfo;
        }
    }
}