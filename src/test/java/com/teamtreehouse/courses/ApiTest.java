package com.teamtreehouse.courses;

import static org.junit.Assert.*;

import com.google.gson.Gson;

import com.teamtreehouse.courses.dao.CourseDao;
import com.teamtreehouse.courses.dao.ReviewDao;
import com.teamtreehouse.courses.dao.Sql2oCourseDao;
import com.teamtreehouse.courses.dao.Sql2oReviewDao;
import com.teamtreehouse.courses.exc.DaoException;
import com.teamtreehouse.courses.model.Course;
import com.teamtreehouse.courses.model.Review;
import com.teamtreehouse.testing.ApiClient;
import com.teamtreehouse.testing.ApiResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Spark;

public class ApiTest {

  public static final String PORT = "4568";
  public static final String TEST_DATASOURCE = "jdbc:h2:mem:testing";
  private Connection conn;
  private ApiClient client;
  private Gson gson;
  private CourseDao courseDao;
  private ReviewDao reviewDao;

  @BeforeClass
  public static void startServer(){
    String[] args = {PORT, TEST_DATASOURCE};
    Api.main(args);
  }

  @AfterClass
  public static void stopServer(){
    Spark.stop();
  }

  @Before
  public void setUp() throws Exception {
    String connectionString = String.format("%s;INIT=RUNSCRIPT from 'classpath:db/init.sql'", TEST_DATASOURCE);
    Sql2o sql2o = new Sql2o(connectionString, "", "");
    courseDao = new Sql2oCourseDao(sql2o);
    reviewDao = new Sql2oReviewDao(sql2o);
    // Keep connection open through entire test so that it doesn't wipe out
    conn = sql2o.open();
    client = new ApiClient("http://localhost:" + PORT);
    gson = new Gson();
  }

  @After
  public void tearDown() throws Exception {
    conn.close();
  }

  @Test
  public void addingCoursesReturnsCreatedStatus() throws Exception{
    Map<String, String> values = new HashMap<>();
    values.put("name", "Test");
    values.put("url", "http://test.com");
    ApiResponse res = client.request("POST", "/courses", gson.toJson(values));
    assertEquals(201, res.getStatus());
  }

  @Test
  public void addingReviewReturnsCreatedStatus() throws Exception{
    Course course = newTestCourse();
    courseDao.add(course);

    Map<String, Object> values = new HashMap<>();
    values.put("rating", 4);
    values.put("comment", "bla bla");
    ApiResponse res = client.request("POST", String.format("/courses/%d/reviews", course.getId()),
        gson.toJson(values));
    assertEquals(201, res.getStatus());
  }

  @Test
  public void addingReviewReturnsErrorStatus() throws Exception{

    Map<String, Object> values = new HashMap<>();
    values.put("rating", 4);
    values.put("comment", "bla bla");
    ApiResponse res = client.request("POST", String.format("/courses/%d/reviews", 34), gson.toJson(values));
    assertEquals(500, res.getStatus());
  }

  @Test
  public void multipleReviewsByOneSingleCourseWork() throws Exception {
    Course course = newTestCourse();
    courseDao.add(course);

    Review review = new Review(course.getId(), 1, "Comment1");
    Review review2 = new Review(course.getId(), 2, "Comment2");
    Review review3 = new Review(course.getId(), 3, "Comment3");

    reviewDao.add(review);
    reviewDao.add(review2);
    reviewDao.add(review3);

    ApiResponse res = client.request("GET", String.format("/courses/%d/reviews", course.getId()));

    Review[] reviews = gson.fromJson(res.getBody(), Review[].class);

    assertEquals(3, reviews.length);
  }

  private Course newTestCourse() {
    return new Course("Test", "http://test.com");
  }

  @Test
  public void coursesCanBeAccessedById() throws Exception{
    Course course = newTestCourse();
    courseDao.add(course);

    ApiResponse res = client.request("GET",
        "/courses/"+course.getId());

    Course retrieved = gson.fromJson(res.getBody(), Course.class);
    assertEquals(course, retrieved);
  }

  @Test
  public void missingCoursesReturnNotFoundStatus() throws Exception {
    ApiResponse res = client.request("GET", "/courses/42");

    assertEquals(404, res.getStatus());
  }
}