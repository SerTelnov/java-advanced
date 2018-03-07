package ru.ifmo.rain.telnov.student;

import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Telnov Sergey on 02.03.2018.
 */
public class StudentDB implements StudentQuery {

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapCollection(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapCollection(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mapCollection(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapCollection(students, s -> String.format("%s %s", s.getFirstName(), s.getLastName()));
    }

    private List<String> mapCollection(List<Student> list, Function<? super Student, String> mapper) {
        return list
                .stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    protected <E> String getValueFromOptional(Optional<E> optional, Function<? super E, String> mapper) {
        return optional
                .map(mapper)
                .orElse("");
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return getValueFromOptional(
                students
                    .stream()
                    .min(Student::compareTo),
                Student::getFirstName);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortCollection(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortCollection(students,
                Comparator.comparing(Student::getLastName)
                        .thenComparing(Comparator.comparing(Student::getFirstName))
                        .thenComparing(Comparator.comparing(Student::getId)));
    }

    private List<Student> sortCollection(Collection<Student> collection, Comparator<? super Student> comparator) {
        return collection
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return getSortedStudentBy(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return getSortedStudentBy(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return getSortedStudentBy(students, Student::getGroup, group);
    }

    private List<Student> getSortedStudentBy(Collection<Student> students,
                                             Function<? super Student, String> method,
                                             String type) {
        return sortStudentsByName(
                getFilterStream(students, method, type)
                        .collect(Collectors.toList()));
    }

    private Stream<Student> getFilterStream(Collection<Student> collection,
                                            Function<? super Student, String> method,
                                            String type) {
        return collection
                .stream()
                .filter(s -> method
                        .apply(s)
                        .equals(type));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return getFilterStream(students, Student::getGroup, group)
                .collect(Collectors
                        .toMap(
                                Student::getLastName,
                                Student::getFirstName,
                                (name1, name2) -> name1.compareTo(name2) < 0 ? name1 : name2));
    }
}
