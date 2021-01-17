package za.co.discovery.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import za.co.discovery.assignment.orm.Planet;

@RepositoryRestResource
public interface PlanetRepository extends JpaRepository<Planet, String> {


}
