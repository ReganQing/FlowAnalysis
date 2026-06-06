package Tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class Calculator {
    @Tool("两数之和")
    int sum(@P(value = "第一个数") int a, @P(value = "第二个数") int b) {
        return a + b;
    }

    @Tool("求平方根")
    double squareRoot(double a){
        return Math.sqrt(a);
    }
}
