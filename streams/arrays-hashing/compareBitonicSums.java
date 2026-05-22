

class Solution {
    public int compareBitonicSums(int[] nums) {
        // Long will handle the larger values...
        long peakElement = Long.MIN_VALUE;
        long left = 0;
        long right = 0;

        for(int i=0; i<nums.length; i++){
            if(nums[i] > peakElement){
                peakElement = nums[i];
                left += nums[i];
            }else{
                right += nums[i];
            }
        }

        // peak element belongs to both part, so reducing peak element from left
        // reducing helps in not getting number more then MAX_VALUE
        left -= peakElement;
        if(left > right) return 0;
        else if(right > left) return 1;
        return -1;
    }
}