// import java.util.ArrayList;
// import java.util.Arrays;
import java.util.HashMap;
// import java.util.List;

class Solution {

    // making an inner class to store the value and index of the array
    // created a class
    // Immutable Fields: Two private final fields: int x and int y.
    // Constructor: A constructor that initializes these fields: new Pair(int x, int y)
    // here getter and setter are just variable
    record Pair(int x, int y) {};

    public int[] twoSum(int[] nums, int target) {
        
        // approach 1: using hashmap
        // making an hashmap with Integer and Integer as key and value respectively
        // HashMap<Integer, Integer> map = new HashMap<>();
        
        // traversing the array which sharing the index with the value
        // for(int i=0; i<nums.length; i++){
        //     map.put(nums[i], i);
        // }

        // sorting the array
        // clone will create a shallow copy so we dont mess up with original array
        // nums = nums.clone();
        // sorting done in o(nlogn) time complexity
        // Arrays.sort(nums);

        // int left = 0, right = nums.length - 1;
        // while(left < right){
        //     int sum = nums[left] + nums[right];
        //     if(sum == target){
        //         return new int[]{map.get(nums[left]), map.get(nums[right])};
        //     }else if(sum < target){
        //         left++;
        //     }else{
        //         right--;
        //     }
        // }

        // return new int[]{-1, -1};


        // another approach: using the inner class to store the value and index of the array
        // List<Pair> anotherNums = new ArrayList<>();
        // for(int i=0; i<nums.length; i++){
        //     anotherNums.add(new Pair(nums[i], i));
        // }

        // // sorting the array, a.x < b.x
        // anotherNums.sort((a, b) -> Integer.compare(a.x, b.x));

        // int left = 0, right = anotherNums.size() - 1;
        // while(left < right){
        //     int sum = anotherNums.get(left).x + anotherNums.get(right).x;
        //     if(sum == target){
        //         return new int[]{
        //             anotherNums.get(left).y, anotherNums.get(right).y
        //         };
        //     }
        //     else if(sum < target){
        //         left++;
        //     }else{
        //         right--;
        //     }
        // }

        // return new int[]{-1, -1};


        // another approach: using hashmap to store the value and index of the array
        HashMap<Integer, Integer> map = new HashMap<>();

        for(int i=0; i<nums.length; i++){
            if(map.containsKey(target - nums[i])){
                return new int[]{i, map.get(target - nums[i])};
            }
            map.put(nums[i], i);
        }

        return new int[]{-1, -1};
    }
}