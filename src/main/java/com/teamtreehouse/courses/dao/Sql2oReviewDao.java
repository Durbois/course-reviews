package com.teamtreehouse.courses.dao;

import com.teamtreehouse.courses.exc.DaoException;
import com.teamtreehouse.courses.model.Review;

import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oReviewDao implements ReviewDao {

  private final Sql2o sql2o;

  public Sql2oReviewDao(Sql2o sql2o) {
    this.sql2o = sql2o;
  }

  @Override
  public void add(Review review) throws DaoException {
    String sql = "INSERT INTO reviews(course_id, rating, comment) VALUES (:courseId, :rating, :comment)";
    try(Connection con = sql2o.open()){
      int id = (int)con.createQuery(sql)
          .bind(review)
          .executeUpdate()
          .getKey();

      review.setId(id);
    }catch(Sql2oException ex){
      throw new DaoException(ex, "Problem adding review");
    }

  }

  @Override
  public List<Review> findAll() throws DaoException {
    try(Connection con = sql2o.open()){
    String sql = "SELECT * FROM reviews";
    return con.createQuery(sql)
        .addColumnMapping("COURSE_ID", "courseId")
        .executeAndFetch(Review.class);
    }catch(Sql2oException ex){
      ex.printStackTrace();
      throw new DaoException(ex, "Problem finding all reviews");
    }
  }

  @Override
  public List<Review> findByCourseId(int courseId) throws DaoException {
    try(Connection con = sql2o.open()){
      String sql = "SELECT * FROM reviews WHERE course_id = :courseId";
      return con.createQuery(sql)
          .addColumnMapping("COURSE_ID", "courseId")
          .addParameter("courseId", courseId)
          .executeAndFetch(Review.class);
    }catch(Sql2oException ex){
      throw new DaoException(ex, "Problem finding all reviews");
    }
  }
}
