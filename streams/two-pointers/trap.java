class Solution {
    public int trap(int[] height) {
        int[] leftMax = new int[height.length];
        int[] rightMax = new int[height.length];

        int h = -1;
        for(int i=0; i<height.length; i++){
            leftMax[i] = h;
            h = Math.max(h, height[i]);
        }

        h = -1;
        for(int i=height.length - 1; i>=0; i--){
            rightMax[i] = h;
            h = Math.max(h, height[i]);
        }

        int maxWater = 0;
        for(int i=0; i<height.length; i++){
            if(leftMax[i] == -1 || rightMax[i] == -1) continue;
            int minHeight = Math.min(leftMax[i], rightMax[i]);
            if(minHeight > height[i]){
                maxWater += minHeight - height[i];
            }
        }
        return maxWater;
    }
}