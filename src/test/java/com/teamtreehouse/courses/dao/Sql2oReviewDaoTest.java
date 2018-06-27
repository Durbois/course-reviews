package com.teamtreehouse.courses.dao;

import static org.junit.Assert.*;

import com.teamtreehouse.courses.exc.DaoException;
import com.teamtreehouse.courses.model.Course;
import com.teamtreehouse.courses.model.Review;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Arrays;
import java.util.List;

public class Sql2oReviewDaoTest {
  private Sql2oReviewDao sql2oReviewDao;
  private Sql2oCourseDao sql2oCourseDao;
  private Connection conn;
  private Course course;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception{
    String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
    Sql2o sql2o = new Sql2o(connectionString, "", "");

    // Keep connection open through entire test so that it doesn't wipe out
    conn = sql2o.open();
    sql2oReviewDao = new Sql2oReviewDao(sql2o);
    sql2oCourseDao = new Sql2oCourseDao(sql2o);
    course = new Course("Test", "http://test.com");
    sql2oCourseDao.add(course);
  }

  @After
  public void tearDown() throws Exception{
    conn.close();
  }

  @Test
  public void addingReviewSetsId() throws Exception{
    Review review = newTestReview();
    int originalReviewId = review.getId();

    sql2oReviewDao.add(review);

    assertNotEquals(originalReviewId, review.getId());
  }

  @Test
  public void addedReviewsAreReturnedFromFindAll() throws Exception{
    Review review = newTestReview();

    sql2oReviewDao.add(review);

    assertEquals(1, sql2oReviewDao.findAll().size());
  }

  @Test
  public void multipleReviewsAreFoundWhenTheyExistForACourse() throws DaoException{
    Review review = new Review(course.getId(), 1, "TestComment");
    Review review2 = new Review(course.getId(), 2, "TestComment2");
    Review review3 = new Review(course.getId(), 3, "TestComment3");

    sql2oReviewDao.add(review);
    sql2oReviewDao.add(review2);
    sql2oReviewDao.add(review3);

    List<Review> foundReviews = sql2oReviewDao.findByCourseId(course.getId());
    assertEquals(3, foundReviews.size());
  }

  @Test(expected = DaoException.class)
  public void addingReviewToANonExistingCourseFails() throws Exception{

    Review review = new Review(8, 1, "TestComment");

    sql2oReviewDao.add(review);
  }

  @Test
  public void existingReviewsCanNotBeFoundById() throws Exception{

    List<Review> foundReviews = sql2oReviewDao.findByCourseId(6);

    assertEquals(0, foundReviews.size());
  }

  private Review newTestReview() throws DaoException {
    return new Review(course.getId(), 1, "TestComment");
  }
}