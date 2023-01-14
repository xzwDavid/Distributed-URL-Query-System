import java.util.LinkedList;
import java.util.Stack;
import java.util.Queue;
public class Main{
    public static void main(String[] args){
        Stack<Integer> stk = new Stack<>();
        stk.push(1);
        stk.push(2);
        stk.push(3);
        Queue<Integer> que = new LinkedList<>();
        que.add(1);
        que.add(2);
        que.add(3);
        System.out.println(que);
        System.out.println(stk);
    }
}