package ru.ifmo.rain.telnov.implementor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.StringJoiner;

/**
 * Created by Telnov Sergey on 13.03.2018.
 */
public class ClassCreator {
    private BufferedWriter bw;
    private int countBrackets = 0;

    public ClassCreator(BufferedWriter bufferedWriter) {
        this.bw = bufferedWriter;
    }

    public void flush() throws IOException {
        while (countBrackets != 0) {
            write(Default.CLOSE_BRACKET);
        }
        bw.flush();
    }

    public void writeClassName(final String packageText, final String interfaceName) throws IOException {
        write(String.format("%s;\n", packageText));
        write(String.format("public class %sImpl implements %s {", interfaceName, interfaceName));
    }

    private String getParams(Class<?>[] params) {
        StringJoiner stringJoiner = new StringJoiner(", ", "(", ")");
        for (int i = 0; i != params.length; i++) {
            stringJoiner
                    .add(String.format("%s arg%d",
                            params[i].getCanonicalName(),
                            i + 1));
        }
        return stringJoiner.toString();
    }

    public void writeMethod(Method method) throws IOException {
        if (Modifier.isAbstract(method.getModifiers()) && !method.isDefault()) {
            write(String.format("public %s %s%s {",
                    method.getReturnType().getCanonicalName(),
                    method.getName(),
                    getParams(method.getParameterTypes())));
            write(String.format("return %s;",
                    Default.getTypeDefaultValue(method.getReturnType())));
            write(Default.CLOSE_BRACKET);
        }
    }

    private void write(String line) throws IOException {
        if (line.contains(Default.CLOSE_BRACKET)) {
            countBrackets--;
        }

        for (int i = 0; i != countBrackets; i++) {
            bw.write(Default.TAB);
        }

        bw.write(line);
        bw.write(Default.NEW_LINE_SYMBOL);
        if (line.contains(Default.OPEN_BRACKET)) {
            countBrackets++;
        }
    }

    private static class Default {
        static final String TAB = "    ";
        static final String NEW_LINE_SYMBOL = "\n";
        static final String OPEN_BRACKET = "{";
        static final String CLOSE_BRACKET = "}";

        static String getTypeDefaultValue(Class<?> type) {
            String typeName = type.getCanonicalName();
            if (type.isPrimitive()) {
                switch (typeName) {
                    case "byte":
                        return "0";
                    case "short":
                        return "0";
                    case "int":
                        return "0";
                    case "long":
                        return "0L";
                    case "char":
                        return "'\u0000'";
                    case "float":
                        return "0.0F";
                    case "double":
                        return "0.0";
                    case "boolean":
                        return "false";
                    default:
                        return "";
                }
            } else {
                return "null";
            }
        }
    }
}
