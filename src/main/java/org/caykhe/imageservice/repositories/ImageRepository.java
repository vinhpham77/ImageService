package org.caykhe.imageservice.repositories;

import org.caykhe.imageservice.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Date;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {

    @Query("SELECT i FROM Image i WHERE i.id IN :ids")
    List<Image> findByIdIn(@Param("ids") List<Integer> ids);

    @Query("SELECT i FROM Image i WHERE i.status = :status AND i.createdAt < :createdAt")
    List<Image> findImagesByStatusAndCreatedAt(@Param("status") boolean status, @Param("createdAt") Instant createdAt);
}
