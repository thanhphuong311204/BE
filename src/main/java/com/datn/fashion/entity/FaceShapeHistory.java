package com.datn.fashion.entity;
import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="face_shape_history")
public class FaceShapeHistory {
 public enum Shape {HEART,OBLONG,OVAL,ROUND,SQUARE}
 public enum Source {AI_CONFIRMED,MANUAL}
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="user_id",nullable=false) public Long userId;
 @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR) @Column(name="previous_shape",length=16) public Shape previousShape;
 @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR) @Column(name="confirmed_shape",nullable=false,length=16) public Shape confirmedShape;
 @Enumerated(EnumType.STRING) @JdbcTypeCode(SqlTypes.VARCHAR) @Column(name="confirmation_source",nullable=false,length=16) public Source confirmationSource;
 @JdbcTypeCode(SqlTypes.TIMESTAMP) @Column(name="changed_at",nullable=false,columnDefinition="TIMESTAMP") public Instant changedAt;
}
