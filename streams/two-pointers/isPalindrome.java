class Solution {
    public boolean isPalindrome(String s) {
        StringBuilder sb = new StringBuilder();

        // converting the charater string to the char array
        for(char c: s.toCharArray()){
            if(Character.isLetterOrDigit(c)){
                sb.append(Character.toLowerCase(c));
            }
        }

        // converting string builder to string and converting it to lower case
        // lower case will handle the numbers also...
        String str = sb.toString();
        str = str.toLowerCase();

        int left = 0, right = str.length() - 1;
        while(left < right){
            if(str.charAt(left) != str.charAt(right)) return false;
            left++;
            right--;
        }

        return true;
    }
}