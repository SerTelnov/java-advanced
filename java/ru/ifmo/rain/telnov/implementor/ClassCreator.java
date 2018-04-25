package ru.ifmo.rain.telnov.implementor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.StringJoiner;

/**
 * Class, which write class info to file.
 */
public class ClassCreator {
    private BufferedWriter bw;
    private int countBrackets = 0;

    /**
     * Constructor <tt>ClassCreator</tt>.
     * <p>
     *
     * @param bufferedWriter buffer, that will store class code
     */
    public ClassCreator(BufferedWriter bufferedWriter) {
        this.bw = bufferedWriter;
    }

    /**
     * Flush <tt>BufferWriter</tt> and write remaining info.
     * <p>
     * Write close brackets and flush bufferWriter.
     *
     * @throws IOException when can't write data.
     */
    public void flush() throws IOException {
        while (countBrackets != 0) {
            write(Default.CLOSE_BRACKET);
        }
        bw.flush();
    }

    /**
     * Write <tt>class</tt> name.
     * <p>
     * Write file <tt>package</tt> and class name.
     *
     * @param packageText   package text.
     * @param interfaceName class name.
     * @throws IOException then can't write data.
     */
    public void writeClassName(final String packageText, final String interfaceName) throws IOException {
        write(String.format("%s;\n", packageText));
        write(String.format("public class %sImpl implements %s {", interfaceName, interfaceName));
    }

    /**
     * Generate <tt>method</tt>'s params.
     * <p>
     *
     * @param params <tt>method</tt>'s params.
     * @return generated params.
     */
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

    /**
     * Write class's <tt>method</tt>.
     * <p>
     * Write method's <tt>modifier</tt> return type <tt>method</tt> name entry params
     * and zero-return value.
     *
     * @param method method to write.
     * @throws IOException then can't write data.
     */
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

    /**
     * Write <code>line</code> to <tt>BuffedWriter</tt>.
     * <p>
     *
     * @param line line to write.
     * @throws IOException then can't write data.
     */
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

    /**
     * Class that contain default <tt>static</tt> values and <tt>method</tt>.
     */
    private static class Default {
        static final String TAB = "    ";
        static final String NEW_LINE_SYMBOL = "\n";
        static final String OPEN_BRACKET = "{";
        static final String CLOSE_BRACKET = "}";

        /**
         * Generate <tt>Class</tt>'s zero value.
         * <p>
         *
         * @param type <tt>class</tt> type
         * @return zero value of <code>type</code>
         */
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
