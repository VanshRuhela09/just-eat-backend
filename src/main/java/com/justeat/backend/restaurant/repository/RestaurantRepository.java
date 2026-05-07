package com.justeat.backend.restaurant.repository;

import com.justeat.backend.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @EntityGraph(attributePaths = {"owner"})
    List<Restaurant> findAll();

    @EntityGraph(attributePaths = {"owner"})
    Optional<Restaurant> findById(Long id);

    @Query("SELECT r FROM Restaurant r JOIN FETCH r.owner WHERE " +
            "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) OR " +
            "(:location IS NULL OR LOWER(r.location) LIKE LOWER(CONCAT('%', :location, '%'))) OR " +
            "(:cuisine IS NULL OR LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :cuisine, '%')))" )
    List<Restaurant> search(@Param("name") String name,
                            @Param("location") String location,
                            @Param("cuisine") String cuisine);

    @Query("SELECT r FROM Restaurant r JOIN FETCH r.owner WHERE r.owner.id = :ownerId")
    List<Restaurant> findByOwnerId(@Param("ownerId") Long ownerId);
}
