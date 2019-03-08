package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataRepository;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Provides the REST interface that can be interacted with at [base]/api/data.
 */
@RestController
public class DataController {
  private static Logger logger = Logger.getLogger(Application.class.getName());


  @Autowired
  private DataRepository repository;

  @Autowired
  private RequestRepository requestRepository;

  /**
   * Basic constructor to initialize both data repositories.
   * @param repository the database for the data (rules)
   * @param requestRepository the database for request logging
   */
  @Autowired
  public DataController(DataRepository repository, RequestRepository requestRepository) {
    this.repository = repository;
    this.requestRepository = requestRepository;

  }

  @GetMapping(value = "/api/requests")
  @CrossOrigin
  public Iterable<RequestLog> showAllLogs() {
    return requestRepository.findAll();
  }

  @GetMapping(value = "/api/data")
  @CrossOrigin
  public Iterable<CoverageRequirementRule> showAll() {
    return repository.findAll();
  }

  /**
   * Gets some data from the repository.
   * @param id the id of the desired data.
   * @return the data from the repository
   */
  @CrossOrigin
  @GetMapping("/api/data/{id}")
  public CoverageRequirementRule getRule(@PathVariable long id) {
    Optional<CoverageRequirementRule> rule = repository.findById(id);

    if (!rule.isPresent()) {
      throw new RuleNotFoundException();
    }

    return rule.get();
  }

  @GetMapping(path = "/download/{id}")
  public ResponseEntity<Resource> download(@PathVariable long id) throws IOException {


    Optional<CoverageRequirementRule> rule = repository.findById(id);
    CoverageRequirementRule crr = rule.get();
    String path = crr.getCqlPackagePath();
    String outputName = crr.getCode() + "_" + crr.getId() + ".zip";
    File file = new File(path);
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputName + "\"")
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(resource);
  }


  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such rule")  // 404
  public class RuleNotFoundException extends RuntimeException {
  }


}
