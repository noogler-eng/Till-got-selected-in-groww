import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Solution {
    public int[] productExceptSelf(int[] nums) {
        List<Integer> leftProd = new ArrayList<>();
        // this will make the size to the size of the nums array and fill it with 1
        List<Integer> rightProd = new ArrayList<>(Collections.nCopies(nums.length, 1));

        int left = 1;
        for(int i=0; i<nums.length; i++){
            leftProd.add(left);
            left = left * nums[i];
        }

        int right = 1;
        for(int i = nums.length - 1; i>=0; i--){
            rightProd.set(i, right);
            right = right * nums[i];
        }

        int[] ans = new int[nums.length];
        for(int i=0; i<nums.length; i++){
            ans[i] = leftProd.get(i) * rightProd.get(i);
        }

        return ans;
    }
}