package com.aerotravel.flightticketbooking.rest.v0;

import com.aerotravel.flightticketbooking.exception.EntityNotFoundException;
import com.aerotravel.flightticketbooking.model.dto.IdedEntity;
import com.aerotravel.flightticketbooking.services.EntityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractRestController<E, D extends IdedEntity> {

    @Autowired
    protected ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;

    protected abstract EntityService<E> getService();
    protected abstract Class<E> getEntityClass();

    protected abstract D convertToDto(E entity);

    protected abstract E convertToEntity(D entityDto);

    @GetMapping
    @Operation(summary = "Get all entities available.")
    public Iterable<D> findAll() {
        log.debug("Getting all records.");
        return getService()
                .getAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/page/{number}")
    @Operation(summary = "Get entities available on the page.")
    public Iterable<D> findAllPaged(@PathVariable int number) {
        log.debug("Getting all records on page {}.", number);
        return getService()
                .getAllPaged(number)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get entities as a paged list.")
    public Page<D> findPaged(@ParameterObject Pageable pageable) {
        return getService().getPaged(pageable).map(this::convertToDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an entity by its id.")
    public D findById(@PathVariable Long id) {
        log.info("Getting a record by id={}.", id);
        return convertToDto(getService().getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Attempt to create an entity by using its DTO.")
    public D create(@Valid @RequestBody D entityDto) {
        log.info("Attempting to create a record. {}", entityDto);
        return convertToDto(getService()
                .save(convertToEntity(entityDto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Attempt to update an entity by using its DTO.")
    public D update(@Valid @RequestBody D entityDto, @PathVariable Long id) {
        if (entityDto.getId() != id) {
            throw new IllegalArgumentException("Ids mismatch.");
        }
        log.info("Attempting to update the record. {}", entityDto);
        // Check whether it exists at all.
        getService().getById(id);

        //TODO: Think about merging not re-writing.
        return create(entityDto);
    }

    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @Operation(summary = "Partial update using JSON-Patch operations.",
            description = "See <a href='https://www.baeldung.com/spring-rest-json-patch'>this</a> for operations usage examples." +
                    "</br>Short example:</br>" +
                    "<p>'...a JSON patch operation to update the customer's telephone number:'</p></br>" +
                    "<pre>" +
                    "[{\n</br>" +
                    "    \"op\":\"replace\",\n</br>" +
                    "    \"path\":\"/telephone\",\n</br>" +
                    "    \"value\":\"001-321-5478\"\n</br>" +
                    "}]" +
                    "</br> </pre>")
    public ResponseEntity<D> partialUpdate(@PathVariable Long id, @RequestBody JsonPatch patch) {
        try {
            log.info("Attempting to do partial update of {} record {}. Operation: \n{}",
                    getEntityClass().getSimpleName(), id, patch);
            E existingEntity = getService().getById(id);
            E patchedEntity = applyPatch(patch, existingEntity);
            patchedEntity = getService().save(patchedEntity);
            return ResponseEntity.ok(convertToDto(patchedEntity));
        } catch (JsonPatchException | JsonProcessingException e) {
            log.error("Failed to update record / convert data.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private E applyPatch(
            JsonPatch patch, E targetCustomer) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetCustomer, JsonNode.class));
        return objectMapper.treeToValue(patched, getEntityClass());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Attempt to delete an entity by its id.")
    public void delete(@PathVariable Long id) {
        log.info("Attempting to delete record {}", id);
        // Check whether it exists at all.
        getService().getById(id);
        getService().deleteById(id);
    }
}

