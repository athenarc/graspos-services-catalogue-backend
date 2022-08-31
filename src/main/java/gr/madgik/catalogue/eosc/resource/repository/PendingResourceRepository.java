package gr.madgik.catalogue.eosc.resource.repository;

import eu.einfracentral.domain.ServiceBundle;
import gr.athenarc.catalogue.service.GenericItemService;
import gr.madgik.catalogue.repository.RegistryCoreRepository;
import org.springframework.stereotype.Component;

@Component
public class PendingResourceRepository extends RegistryCoreRepository<ServiceBundle> {

    public PendingResourceRepository(GenericItemService itemService) {
        super(itemService);
    }

    @Override
    public String getResourceTypeName() {
        return "pending_service";
    }
}
