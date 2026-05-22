class Solution {
    public boolean validDigit(int n, int x) {
        boolean isVlaid = false;
        while(n > 9){
            int digit = n % 10;
            if( digit == x ){
                isVlaid = true;
            }
            n = n / 10;
        }

        // if first digit matches then it is not valid
        if(n == x) isVlaid = false;
        return isVlaid;
    }
}