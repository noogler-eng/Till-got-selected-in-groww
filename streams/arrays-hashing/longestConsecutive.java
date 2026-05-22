import java.util.HashSet;
import java.util.Set;

class Solution {
    public int longestConsecutive(int[] nums) {
        if(nums.length == 0) return 0;

        Set<Integer> set = new HashSet<>();
        for(int num: nums) set.add(num);
        
        // optmised way is traversing the set again ...
        int longest = 1;
        for(int i: set){
            if(set.contains(i - 1)){
                continue;
            }else{
                int count = 1;
                int val = i + 1;
                while(set.contains(val)){
                    count++;
                    val++;
                }
                longest = Math.max(longest, count);
            }
        }

        return longest;
    }
}