import java.util.Scanner;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        CalculatorView view = new CalculatorView();
        CalculatorModel model = new CalculatorModel();
        CalculatorController controller = new CalculatorController(model, view);
        
        controller.start();
    }
}

// Model - содержит логику вычислений
class CalculatorModel {
    public double calculate(String expression) throws Exception {
        expression = expression.replaceAll("\\s+", "");
        
        // Проверка скобок
        if (!checkParentheses(expression)) {
            throw new Exception("Несбалансированные скобки в выражении");
        }
        
        // Проверка количества операций
        if (countOperators(expression) > 15) {
            throw new Exception("Превышено максимальное количество операций (15)");
        }
        
        // Обработка специальных функций
        expression = preprocessExpression(expression);
        
        // Преобразование в обратную польскую нотацию
        String rpn = convertToRPN(expression);
        
        // Вычисление выражения
        return evaluateRPN(rpn);
    }
    
    private boolean checkParentheses(String expr) {
        Stack<Character> stack = new Stack<>();
        for (char c : expr.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty()) return false;
                stack.pop();
            }
        }
        return stack.isEmpty();
    }
    
    private int countOperators(String expr) {
        int count = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (isOperator(c) || (c == '*' && i < expr.length()-1 && expr.charAt(i+1) == '*')) {
                count++;
                if (c == '*' && i < expr.length()-1 && expr.charAt(i+1) == '*') i++;
            }
        }
        return count;
    }
    
    private String preprocessExpression(String expr) {
        // Заменяем ** на ^ для единообразия
        expr = expr.replace("**", "^");
        
        // Обрабатываем функции
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            
            if (c == 'e' && i+3 < expr.length() && expr.substring(i, i+4).equals("exp(")) {
                sb.append("e^(");
                i += 3;
            } else if (c == 'l' && i+3 < expr.length() && expr.substring(i, i+4).equals("log(")) {
                sb.append("log2(");
                i += 3;
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
    
    private String convertToRPN(String expression) throws Exception {
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.') {
                while (i < expression.length() && 
                      (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    output.append(expression.charAt(i++));
                }
                output.append(' ');
                i--;
            } 
            else if (Character.isLetter(c)) {
                // Обработка констант (например, e)
                if (c == 'e') {
                    output.append(Math.E).append(' ');
                }
            }
            else if (isFunction(c, i, expression)) {
                stack.push(c);
                i += (c == 'l' ? 4 : 3); // Пропускаем "log2(" или "e^("
            }
            else if (isOperator(c)) {
                while (!stack.isEmpty() && getPriority(stack.peek()) >= getPriority(c)) {
                    output.append(stack.pop()).append(' ');
                }
                stack.push(c);
            }
            else if (c == '(') {
                stack.push(c);
            }
            else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop()).append(' ');
                }
                if (!stack.isEmpty() && stack.peek() == '(') {
                    stack.pop();
                }
                if (!stack.isEmpty() && (stack.peek() == 'l' || stack.peek() == 'e')) {
                    output.append(stack.pop()).append(' ');
                }
            }
        }
        
        while (!stack.isEmpty()) {
            output.append(stack.pop()).append(' ');
        }
        
        return output.toString();
    }
    
    private boolean isFunction(char c, int pos, String expr) {
        if (c == 'e' && pos+2 < expr.length() && expr.charAt(pos+1) == '^' && expr.charAt(pos+2) == '(') {
            return true;
        }
        if (c == 'l' && pos+4 < expr.length() && expr.substring(pos, pos+5).equals("log2(")) {
            return true;
        }
        return false;
    }
    
    private double evaluateRPN(String rpn) throws Exception {
        Stack<Double> stack = new Stack<>();
        String[] tokens = rpn.split(" ");
        
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            
            if (Character.isDigit(token.charAt(0))) {
                stack.push(Double.parseDouble(token));
            } 
            else if (token.equals("e")) {
                stack.push(Math.E);
            }
            else {
                char op = token.charAt(0);
                
                if (op == '!') {
                    double a = stack.pop();
                    stack.push(factorial(a));
                } 
                else if (op == 'l' || op == 'e') {
                    double a = stack.pop();
                    if (op == 'l') {
                        stack.push(Math.log(a) / Math.log(2));
                    } else {
                        stack.push(Math.exp(a));
                    }
                }
                else {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(applyOperation(op, a, b));
                }
            }
        }
        
        return stack.pop();
    }
    
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '!';
    }
    
    private int getPriority(char op) {
        switch (op) {
            case '^': return 4;
            case '!': return 4;
            case 'e': case 'l': return 4; // Функции
            case '*': case '/': return 3;
            case '+': case '-': return 2;
            case '(': return 1;
            default: return 0;
        }
    }
    
    private double factorial(double n) throws Exception {
        if (n < 0) throw new Exception("Факториал отрицательного числа");
        if (n % 1 != 0) throw new Exception("Факториал только для целых чисел");
        
        int num = (int)n;
        double result = 1;
        for (int i = 2; i <= num; i++) {
            result *= i;
        }
        return result;
    }
    
    private double applyOperation(char op, double a, double b) throws Exception {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': 
                if (b == 0) throw new Exception("Деление на ноль");
                return a / b;
            case '^': return Math.pow(a, b);
            default: throw new Exception("Неизвестная операция");
        }
    }
}

// View - отвечает за ввод/вывод
class CalculatorView {
    private Scanner scanner = new Scanner(System.in);
    
    public String getInput() {
        System.out.print("Введите выражение (или 'exit' для выхода): ");
        return scanner.nextLine();
    }
    
    public void showResult(double result) {
        System.out.println("Результат: " + result);
    }
    
    public void showError(String message) {
        System.out.println("Ошибка: " + message);
    }
    
    public void close() {
        scanner.close();
    }
}

// Controller - связывает Model и View
class CalculatorController {
    private CalculatorModel model;
    private CalculatorView view;
    
    public CalculatorController(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;
    }
    
    public void start() {
        System.out.println("Расширенный калькулятор выражений");
        System.out.println("Поддерживаемые операции: + - * / ^ !");
        System.out.println("Функции: exp(), log(), скобки");
        
        while (true) {
            String input = view.getInput();
            if (input.equalsIgnoreCase("exit")) break;
            
            try {
                double result = model.calculate(input);
                view.showResult(result);
            } catch (Exception e) {
                view.showError(e.getMessage());
            }
        }
        
        view.close();
    }
}
