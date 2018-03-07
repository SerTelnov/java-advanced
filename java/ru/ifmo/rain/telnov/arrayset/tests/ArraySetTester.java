package ru.ifmo.rain.telnov.arrayset.tests;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.rain.telnov.arrayset.ArraySet;
import ru.ifmo.rain.telnov.arrayset.NavigableArraySet;

import java.util.*;

/**
 * Created by Telnov Sergey on 20.02.2018.
 */
public class ArraySetTester {
    @Test
    public void constructorTest() {
        List<Integer> list = getRandomIntegerList();

        SortedSet<Integer> set = new ArraySet<>(list);

        Collections.sort(list);

        int i = 0;
        for (Integer el : set) {
            Assert.assertEquals("invalid order", list.get(i++), el);
        }
    }

    @Test
    public void containsTest() {
        List<Integer> list = getRandomIntegerList();

        SortedSet<Integer> set = new ArraySet<>(list);

        SortedSet<Integer> treeSet = new TreeSet<>(list);

        try {
            boolean contains = treeSet.contains(1);
            Assert.assertEquals("exist null", contains, set.contains(null));
        } catch (NullPointerException | ClassCastException e) {
//            e.printStackTrace();
        }
    }

    @Test
    public void lowerTest() {
        List<Integer> list = getList();
        NavigableSet<Integer> set = new NavigableArraySet<>(list);

        Assert.assertEquals(new Integer(1), set.lower(3));
        Assert.assertEquals(null, set.lower(1));
        Assert.assertEquals(new Integer(5), set.lower(6));
        Assert.assertEquals(new Integer(7), set.lower(9));
        Assert.assertEquals(null, set.lower(0));
    }

    @Test
    public void higherTest() {
        List<Integer> list = getList();
        NavigableSet<Integer> set = new NavigableArraySet<>(list);

        Assert.assertEquals(new Integer(5), set.higher(3));
        Assert.assertEquals(new Integer(3), set.higher(2));
        Assert.assertEquals(new Integer(7), set.higher(6));
        Assert.assertEquals(null, set.higher(9));
        Assert.assertEquals(new Integer(1), set.higher(0));
    }

    @Test
    public void floorTest() {
        List<Integer> list = getList();
        NavigableSet<Integer> set = new NavigableArraySet<>(list);

        Assert.assertEquals(new Integer(3), set.floor(3));
        Assert.assertEquals(new Integer(1), set.floor(2));
        Assert.assertEquals(new Integer(5), set.floor(6));
        Assert.assertEquals(new Integer(7), set.floor(9));
        Assert.assertEquals(null, set.floor(0));
    }
    @Test
    public void ceilingSimpleTest() {
        List<Integer> list = getOneElementList();
        NavigableSet<Integer> set = new NavigableArraySet<>(list);

        Assert.assertEquals(new Integer(1), set.ceiling(1));
        Assert.assertEquals(new Integer(1), set.ceiling(0));
    }

    @Test
    public void ceilingAllEqualTest() {
        List<Integer> list = getOneElementList();
        NavigableSet<Integer> set = new NavigableArraySet<>(list, (o1, o2) -> 0);

        Assert.assertEquals(new Integer(1), set.ceiling(1));
        Assert.assertEquals(new Integer(1), set.ceiling(0));

    }

    @Test
    public void ceilingTest() {
        List<Integer> list = getList();
        NavigableSet<Integer> set = new NavigableArraySet<>(list);

        Assert.assertEquals(new Integer(3), set.ceiling(3));
        Assert.assertEquals(new Integer(3), set.ceiling(2));
        Assert.assertEquals(new Integer(7), set.ceiling(6));
        Assert.assertEquals(null, set.ceiling(9));
        Assert.assertEquals(new Integer(1), set.ceiling(0));
    }


    public static List<Integer> getRandomIntegerList() {
        Random random = new Random();

        final int size = Math.abs(random.nextInt()) % 1000;

        return new ArrayList<Integer>(size) {{
            for (int i = 0; i != size; i++) {
                add(random.nextInt());
            }
        }};
    }

    public static List<Integer> getOneElementList() {
        return new ArrayList<Integer>() {{
            add(1);
        }};
    }

    public static List<Integer> getList() {
        return new ArrayList<Integer>() {{
            add(1);
            add(3);
            add(5);
            add(7);
        }};
    }
}
