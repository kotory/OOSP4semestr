import java.util.Scanner;
import java.util.Stack;

public class SimpleMathCalculator {
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
        // Удаляем пробелы и проверяем базовые условия
        expression = expression.replaceAll("\\s+", "");
        if (!expression.matches("^-?\\d+.*\\d+$")) {
            throw new Exception("Выражение должно начинаться и заканчиваться числом");
        }
        
        // Преобразуем в обратную польскую нотацию
        String rpn = convertToRPN(expression);
        
        // Вычисляем выражение
        return evaluateRPN(rpn);
    }
    
    private String convertToRPN(String expression) {
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.') {
                // Собираем все число
                while (i < expression.length() && 
                      (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    output.append(expression.charAt(i++));
                }
                output.append(' ');
                i--;
            } else if (isOperator(c)) {
                while (!stack.isEmpty() && getPriority(stack.peek()) >= getPriority(c)) {
                    output.append(stack.pop()).append(' ');
                }
                stack.push(c);
            }
        }
        
        while (!stack.isEmpty()) {
            output.append(stack.pop()).append(' ');
        }
        
        return output.toString();
    }
    
    private double evaluateRPN(String rpn) throws Exception {
        Stack<Double> stack = new Stack<>();
        String[] tokens = rpn.split(" ");
        
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            
            if (Character.isDigit(token.charAt(0))) {
                stack.push(Double.parseDouble(token));
            } else {
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperation(token.charAt(0), a, b));
            }
        }
        
        return stack.pop();
    }
    
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }
    
    private int getPriority(char op) {
        switch (op) {
            case '^': return 3;
            case '*': case '/': return 2;
            case '+': case '-': return 1;
            default: return 0;
        }
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
        System.out.println("Простой калькулятор выражений");
        System.out.println("Поддерживаемые операции: + - * / ^");
        
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
