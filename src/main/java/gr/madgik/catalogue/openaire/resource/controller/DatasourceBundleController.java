package gr.madgik.catalogue.openaire.resource.controller;

import eu.openminted.registry.core.domain.Paging;
import gr.madgik.catalogue.dto.BulkOperation;
import gr.madgik.catalogue.openaire.domain.DatasourceBundle;
import gr.madgik.catalogue.openaire.domain.ServiceBundle;
import gr.madgik.catalogue.openaire.resource.DatasourceBundleService;
import gr.madgik.catalogue.repository.RegistryCoreRepository;
import gr.madgik.catalogue.utils.PagingUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bundles/datasources")
public class DatasourceBundleController {

    private static final Logger logger = LoggerFactory.getLogger(DatasourceBundleController.class);

    private final DatasourceBundleService datasourceBundleService;
    private final RegistryCoreRepository<DatasourceBundle, String> datasourceRepository;

    public DatasourceBundleController(DatasourceBundleService datasourceBundleService,
                                      RegistryCoreRepository<DatasourceBundle, String> datasourceRepository) {
        this.datasourceBundleService = datasourceBundleService;
        this.datasourceRepository = datasourceRepository;
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "Keyword to refine the search", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "from", value = "Starting index in the result set", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "quantity", value = "Quantity to be fetched", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "order", value = "asc / desc", dataTypeClass = String.class, paramType = "query"),
            @ApiImplicitParam(name = "orderField", value = "Order field", dataTypeClass = String.class, paramType = "query")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Paging<DatasourceBundle> getAll(@ApiIgnore @RequestParam Map<String, Object> allRequestParams) {
        return datasourceBundleService.getWithEnrichedFacets(PagingUtils.createFacetFilter(allRequestParams));
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public DatasourceBundle get(@PathVariable("id") String id) {
        return datasourceRepository.findById(id).orElse(null);
    }

    @PatchMapping(path = "{id}/verify", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public DatasourceBundle verifyProvider(@PathVariable("id") String id, @RequestParam(required = false) Boolean active,
                                           @RequestParam(required = false) String status) {
        DatasourceBundle datasource = datasourceBundleService.verify(id, status, active);
        logger.info("User updated Datasource with name '{}' [status: {}] [active: {}]", datasource.getPayload().getName(), status, active);
        return datasource;
    }

    // Activate/Deactivate a Datasource.
    @PatchMapping(path = "{id}/publish", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('ADMIN')")
    public DatasourceBundle publish(@PathVariable("id") String id, @RequestParam(required = false) Boolean active) {
        DatasourceBundle datasource = datasourceBundleService.activate(id, active);
        logger.info("User updated Datasource with name '{}' [active: {}]", datasource.getPayload().getName(), active);
        return datasource;
    }

    @PostMapping("bulk")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BulkOperation<DatasourceBundle> addAll(@RequestBody List<DatasourceBundle> bundles) {
        BulkOperation<DatasourceBundle> datasources = new BulkOperation<>();
        for (DatasourceBundle bundle : bundles) {
            try {
                datasources.getSuccessful().add(datasourceRepository.create(bundle));
            } catch (Exception e) {
                datasources.getFailed().add(bundle);
            }
        }
        return datasources;
    }

    @PutMapping("bulk")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BulkOperation<DatasourceBundle> updateAll(@RequestBody List<DatasourceBundle> bundles) {
        BulkOperation<DatasourceBundle> datasources = new BulkOperation<>();
        for (DatasourceBundle bundle : bundles) {
            try {
                datasources.getSuccessful().add(datasourceRepository.update(bundle.getId(), bundle));
            } catch (Exception e) {
                datasources.getFailed().add(bundle);
            }
        }
        return datasources;
    }
}
