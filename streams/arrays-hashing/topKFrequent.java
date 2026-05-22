import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Solution {
    // making an inner class
    record Pair(int x, int y) {}

    public int[] topKFrequent(int[] nums, int k) {
        HashMap<Integer, Integer> freq = new HashMap<>();
        for(int i=0; i<nums.length; i++){
            freq.put(nums[i], freq.getOrDefault(nums[i], 0) + 1);
        }

        List<Pair> pairs = new ArrayList<>();
        for(int key: freq.keySet()){
            pairs.add(new Pair(key, freq.get(key)));
        }

        // check here we have reverse this values....
        pairs.sort((a, b) -> Integer.compare(b.y, a.y));

        int[] ans = new int[k];
        for(int i=0; i<pairs.size(); i++){
            if(k == 0) return ans;
            ans[i] = pairs.get(i).x;
            k--;
        }
        return ans;
    }
}