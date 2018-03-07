package ru.ifmo.rain.telnov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Telnov Sergey on 03.03.2018.
 */
public class StudentGroupDB extends StudentDB implements StudentGroupQuery {

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsFromCollection(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsFromCollection(students, this::sortStudentsById);
    }

    private List<Group> getGroupsFromCollection(Collection<Student> students,
                                                Function<List<Student>, List<Student>> comparator) {
        return students
                .stream()
                .collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream()
                .map(e -> new Group(e.getKey(), comparator.apply(e.getValue())))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    private String getMaxElement(Collection<Student> students, Comparator<? super Group> comparator) {
        return getValueFromOptional(
                getGroupsFromCollection(students, Function.identity())
                        .stream()
                        .max(comparator),
                Group::getName);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getMaxElement(students,
                Comparator.comparingInt(g -> g.getStudents().size()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getMaxElement(students,
                Comparator.comparingInt(g -> getDistinctFirstNames(g.getStudents()).size()));
    }
}
