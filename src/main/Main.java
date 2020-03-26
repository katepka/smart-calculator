package main;

import java.math.BigInteger;
import java.util.*;

public class Main {

    private static Map<String, BigInteger> map = new HashMap<>();
    private static String operators = "-+/*";
    private static String delimiter = "() " + operators;
    private static boolean flag = true;

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            String input = "";
            while (!input.equals("/exit")) {
                input = scanner.nextLine();

                if (input.isEmpty()) {
                    continue;
                } else if (input.equals("/help")) {
                    System.out.println("The program calculates the sum, the subtraction, " +
                            "the multiplication and the division of big numbers");
                    continue;
                } else if (input.equals("/exit")) {
                    break;
                } else if (input.matches(".*=.*")) {
                    assignVars(input);
                } else {
                    List<String> postfix = translateIntoPostfix(input);
                    if (flag) {
                        BigInteger result = calcPostfix(postfix);
                        if (result != null) {
                            System.out.println(result);
                        }
                    } else {
                        flag = true;
                    }
                }
            }
        }
        System.out.println("Bye!");
    }

    private static boolean isDelimeter(String token) {
        if (token.length() != 1) return false;
        for (int i = 0; i < delimiter.length(); i++) {
            if (token.charAt(0) == delimiter.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOperator(String token) {
        if (token.equals("u-")) return true;
        for (int i = 0; i < operators.length(); i++) {
            if (token.charAt(0) == operators.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    private static int setPriority(String token) {
        switch (token) {
            case "(":
                return 1;
            case "-":
            case "+":
                return 2;
            case "*":
            case "/":
                return 3;
            default:
                return 4;
        }
    }

    private static List<String> translateIntoPostfix(String infix) {

        List<String> postfix = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        // делит строку на токены по разделителям "() +-/*", включая разделители
        StringTokenizer tokenizer = new StringTokenizer(infix, delimiter, true);
        String prev = ""; // используется для распознавания унарного минуса u-
        String cur = ""; // текущий токен

        if (infix.matches("^\\/.*$")) {
            System.out.println("Unknown command");
            flag = false;
            return postfix;
        }

        // Разбираем исходную строку:
        // делаем, пока есть след.токен:
        while (tokenizer.hasMoreTokens()) {
            // запоминаем текущий токен:
            cur = tokenizer.nextToken();
            // если нет следующего токена и текущий токен - оператор, то выражение несогласовано:
            if (!tokenizer.hasMoreTokens() && isOperator(cur)) {
                System.out.println("Invalid expression");
                flag = false;
                return postfix;
            }
            // если текущий токен равен пробелу, идем к след.шагу:
            if (cur.equals(" ")) continue;

            // если текущий токен - разделитель:
            if (isDelimeter(cur)) {
                if (cur.equals("(")) {
                    // если "(" - кладем в стэк:
                    stack.push(cur);
                } else if (cur.equals(")")) {
                    // если ")" - пока на вершине стэка не будет "(":
                    try {
                        while (!stack.peek().equals("(")) {
                            // достаем в постификсный список элементы из стэка:
                            postfix.add(stack.pop());
                            // если стэк пуст, а откр.скобка так и не найдена, то выражение несогласовано:
                            if (stack.isEmpty()) {
                                System.out.println("Invalid expression");
                                flag = false;
                                return postfix;
                            }
                        }
                        // найденную "(" удаляем из стэка:
                    } catch (NullPointerException npe) {
                        continue;
                    }
                    stack.pop();
                    // иначе, если текущий токен равен "-" и предыдущий токен не задан
                    // или предыдущий токен - разделитель и не равен закрывающейся скобке:
                } else {
                    if (cur.equals("-") && prev.equals("-")) {
                        if (!stack.isEmpty() && stack.peek().equals("-")) {
                            stack.pop();
                            stack.push("+");
                        } else if (!stack.isEmpty() && stack.peek().equals("+")) {
                            stack.pop();
                            stack.push("-");
                        }
                        prev = cur;
                        continue;
                    } else if (cur.equals("+") && prev.equals("+")) {
                        prev = cur;
                        continue;
                    } else if (cur.equals("-") && prev.equals("") || (isDelimeter(prev) && !prev.equals(")"))) {
                        // текущий токен отмечаем как унарный минус:
                        cur = "u-";
                        // иначе пока приоритет текущего токена меньше или равен, чем токена на вершине стэка:
                    } else {
                        while (!stack.isEmpty() && (setPriority(cur) <= setPriority(stack.peek()))) {
                            // добавляем в постфиксный список элемент с вершины стэка:
                            postfix.add(stack.pop());
                        }
                    }
                    // кладем текущий элемент на вершину стэка:
                    stack.push(cur);
                }
                // если текущий токен - не разделитель, а число, переменная или оператор:
            } else {
                if (cur.matches("^[a-zA-Z]+$")) {
                    if (map.containsKey(cur)) {
                        // то добавляем его в постифксный список
                        postfix.add(String.valueOf(map.get(cur)));
                    } else {
                        System.out.println("Unknown variable");
                        flag = false;
                        return postfix;
                    }
                } else if (cur.matches("^\\d+$")) {
                    // то добавляем его в постифксный список
                    postfix.add(cur);
                } else {
                    System.out.println("Invalid expression");
                    flag = false;
                    return postfix;
                }
            }
            // текущий токен становится предыдущим:
            prev = cur;
        }

        // Исходную строку разобрали. Теперь выталкиваем из стэка в постфиксную строку:
        while (!stack.isEmpty()) {
            // если на вершине стэка оператор:
            if (isOperator(stack.peek())) {
                // перекладываем его из стэка в постфиксный список:
                postfix.add(stack.pop());
                // если другое, то выражение несогласовано:
            } else {
                System.out.println("Invalid expression");
                flag = false;
                return postfix;
            }
        }

        return postfix;
    }

    /*
    - если в записи встретили число, то кладём его в стэк;
    - если в записи встретили функцию или унарный минус, то :
        - вытаскиваем из стэка верхний элемент;
        - применяем функцию к нему;
        - кладём элемент обратно в стэк;
    - если в записи встретили бинарный оператор, то:
        - вытаскиваем из стэка два верхних элемента;
        - выполняем над ними операцию;
        - кладём в стэк результат выполнения операции;
    - выводим последний элемент стэка.
     */
    public static BigInteger calcPostfix(List<String> postfix) {
        Deque<BigInteger> stack = new ArrayDeque<>();
        try {
            for (String x : postfix) {
                if (x.equals("+")) {
                    BigInteger b = stack.pop();
                    BigInteger a = stack.pop();
                    stack.push(a.add(b));
                } else if (x.equals("-")) {
                    BigInteger b = stack.pop();
                    BigInteger a = stack.pop();
                    stack.push(a.subtract(b));
                } else if (x.equals("*")) {
                    BigInteger b = stack.pop();
                    BigInteger a = stack.pop();
                    stack.push(a.multiply(b));
                } else if (x.equals("/")) {
                    BigInteger b = stack.pop();
                    BigInteger a = stack.pop();
                    stack.push(a.divide(b));
                } else if (x.equals("u-")) {
                    BigInteger a = stack.pop();
                    stack.push(a.negate());
                } else {
                    stack.push(new BigInteger((x)));
                }
            }
            return stack.pop();
        } catch (NoSuchElementException e) {
            System.out.println("Invalid expression");
        }
        return null;
    }

    private static void assignVars(String input) {
        /* Корректное выражение присваивания x = y */
        if (input.matches("^([0-9a-zA-Z]+)\\s*=\\s*([0-9]+|[a-zA-Z]+)$")) {
            String[] pair = input.split("\\s*=\\s*");
            /* Если ключ и значение - символьные */
            if (pair[0].matches("^[a-zA-Z]+$") && pair[1].matches("^[a-zA-Z]+$")) {
                if (map.containsKey(pair[1])) {
                    BigInteger val = map.get(pair[1]);
                    map.put(pair[0], val);
                } else {
                    System.out.println("Unknown variable");
                    return;
                }
                /* Если значение - не число */
            } else if (pair[1].matches("\\D")) {
                System.out.println("Invalid assignment");

                /* Если переменная содержит не буквенные значения */
            } else if (!pair[0].matches("^[a-zA-Z]+$")) {
                System.out.println("Invalid identifier");

                /* Если все ок */
            } else if (pair[0].matches("^[a-zA-Z]+$") && pair[1].matches("^\\d+$")) {
                map.put(pair[0], new BigInteger((pair[1])));
            }
        } else {
            System.out.println("Invalid assignment");
        }
    }
}
