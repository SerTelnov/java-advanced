package ru.ifmo.rain.telnov.student.tests;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.ifmo.rain.telnov.arrayset.tests.ArraySetTester.getRandomIntegerList;

/**
 * Created by Telnov Sergey on 04.03.2018.
 */
public class StudentTester {
    private class Something {
        public int value;
        public String someString;

        public Something() {
            someString = "";
        }
    }

    @Test
    public void example() {

    }

    public List<Something> getRandomSomeList() {
        Random random = new Random();

        final int size = Math.abs(random.nextInt()) % 1000;

        return new ArrayList<Something>(size) {{
            for (int i = 0; i != size; i++) {
                add(new Something());
            }
        }};
    }
}
