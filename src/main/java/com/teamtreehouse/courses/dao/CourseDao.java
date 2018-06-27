package com.teamtreehouse.courses.dao;

import com.teamtreehouse.courses.exc.DaoException;
import com.teamtreehouse.courses.model.Course;

import java.util.List;

public interface CourseDao {
  void add(Course course) throws DaoException;
  List<Course> findAll() throws DaoException;
  Course findById(int id) throws DaoException;
}
