import java.util.ArrayList;
import java.util.List;

class Solution {

    // dummy_input=["we","say",":","yes","!@#$%^&*()"]
    // 2we3say1:3yes10!@#$%^&*()
    public String encode(List<String> strs) {
        StringBuilder sb = new StringBuilder();
        for(String s: strs){
            sb.append(s.length()).append('#').append(s);
        }
        return sb.toString();
    }

    // #5Hello#5world
    public List<String> decode(String str) {
        // decoding implementation
        List<String> result = new ArrayList<>();
        int i=0;
        while(i < str.length()){
            int num = 0;
            while(str.charAt(i) != '#'){
                num = num * 10 + (str.charAt(i) - '0');
                i++;
            }

            result.add(str.substring(i + 1, i + 1 + num));
            i += 1 + num;
        }
        return result;
    }
}