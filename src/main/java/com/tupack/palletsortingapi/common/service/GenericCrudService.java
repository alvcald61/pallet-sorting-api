package com.tupack.palletsortingapi.common.service;

import com.tupack.palletsortingapi.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generic CRUD service base class to eliminate code duplication across services.
 * Provides common CRUD operations (Create, Read, Update, Delete) that can be reused.
 *
 * @param <T> The entity type
 * @param <ID> The ID type of the entity
 * @param <DTO> The DTO type for responses
 */
@Slf4j
public abstract class GenericCrudService<T, ID, DTO> {

  /**
   * Get the repository for this service
   */
  protected abstract JpaRepository<T, ID> getRepository();

  /**
   * Get the mapper function to convert entity to DTO
   */
  protected abstract Function<T, DTO> getMapper();

  /**
   * Get the entity name for logging and error messages
   */
  protected abstract String getEntityName();

  /**
   * Find all entities and map to DTOs
   */
  @Transactional(readOnly = true)
  public List<DTO> findAll() {
    log.debug("Finding all {}", getEntityName());
    List<T> entities = getRepository().findAll();
    log.debug("Found {} {}", entities.size(), getEntityName());
    return entities.stream()
        .map(getMapper())
        .collect(Collectors.toList());
  }

  /**
   * Find entity by ID and map to DTO
   *
   * @throws ResourceNotFoundException if entity not found
   */
  @Transactional(readOnly = true)
  public DTO findById(ID id) {
    log.debug("Finding {} by id: {}", getEntityName(), id);
    return getRepository().findById(id)
        .map(entity -> {
          log.debug("{} found with id: {}", getEntityName(), id);
          return getMapper().apply(entity);
        })
        .orElseThrow(() -> {
          log.warn("{} not found with id: {}", getEntityName(), id);
          return new ResourceNotFoundException(getEntityName(), "id", id.toString());
        });
  }

  /**
   * Check if entity exists by ID
   */
  @Transactional(readOnly = true)
  public boolean existsById(ID id) {
    log.debug("Checking if {} exists with id: {}", getEntityName(), id);
    return getRepository().existsById(id);
  }

  /**
   * Count all entities
   */
  @Transactional(readOnly = true)
  public long count() {
    log.debug("Counting all {}", getEntityName());
    return getRepository().count();
  }

  /**
   * Delete entity by ID
   *
   * @throws ResourceNotFoundException if entity not found
   */
  @Transactional
  public void deleteById(ID id) {
    log.info("Deleting {} with id: {}", getEntityName(), id);
    if (!getRepository().existsById(id)) {
      log.warn("Cannot delete - {} not found with id: {}", getEntityName(), id);
      throw new ResourceNotFoundException(getEntityName(), "id", id.toString());
    }
    getRepository().deleteById(id);
    log.info("{} deleted successfully: {}", getEntityName(), id);
  }

  /**
   * Save entity (create or update)
   */
  @Transactional
  protected T save(T entity) {
    log.debug("Saving {}", getEntityName());
    T saved = getRepository().save(entity);
    log.info("{} saved successfully", getEntityName());
    return saved;
  }
}
