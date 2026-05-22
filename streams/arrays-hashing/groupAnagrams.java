import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Solution {
    public List<List<String>> groupAnagrams(String[] strs) {
        HashMap<String, List<String>> anagramGroups = new HashMap<>();
        List<List<String>> ans = new ArrayList<>();

        for(int i=0; i<strs.length; i++){
            String word = strs[i];
            // this is how we can sort a string in java using streams
            // word.chars() will return an IntStream of the characters in the string
            // sorted() will sort the characters in the stream
            // collect() will collect the sorted characters into a StringBuilder and then convert it to a string
            String sortedWord = word.chars()
                                    .sorted()
                                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                    .toString();
            List<String> group = anagramGroups.getOrDefault(sortedWord, new ArrayList<>());
            group.add(word);
            anagramGroups.put(sortedWord, group);
        }

        // this is how we can traverse an HashMap in java
        // traversing entry in the map
        for(Map.Entry<String, List<String>> entry: anagramGroups.entrySet()){
            ans.add(entry.getValue());
        }

        return ans;
    }
}