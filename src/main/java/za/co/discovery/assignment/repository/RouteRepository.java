package za.co.discovery.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import za.co.discovery.assignment.orm.Route;

@RepositoryRestResource
public interface RouteRepository extends JpaRepository<Route, Short> {

}
