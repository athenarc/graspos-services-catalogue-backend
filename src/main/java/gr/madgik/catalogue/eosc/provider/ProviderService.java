package gr.madgik.catalogue.eosc.provider;

import eu.einfracentral.domain.LoggingInfo;
import eu.einfracentral.domain.Provider;
import eu.einfracentral.domain.ProviderBundle;
import eu.einfracentral.domain.User;
import eu.openminted.registry.core.domain.FacetFilter;
import eu.openminted.registry.core.domain.Paging;
import gr.madgik.catalogue.Catalogue;
import gr.madgik.catalogue.SecurityService;
import gr.madgik.catalogue.eosc.provider.repository.ProviderRepository;
import gr.madgik.catalogue.utils.PagingUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProviderService {

    private final Catalogue<ProviderBundle, String> catalogue;
    private final ProviderRepository repository;
    private final SecurityService securityService;

    @Value("${project.catalogue.name}")
    private String catalogueName;

    public ProviderService(Catalogue<ProviderBundle, String> catalogue,
                           ProviderRepository repository,
                           SecurityService securityService) {
        this.catalogue = catalogue;
        this.repository = repository;
        this.securityService = securityService;
    }

    public boolean validate(Object provider) {
        throw new UnsupportedOperationException("Not implemented yet");
//        return null;
    }

    public Provider register(Provider provider) {
        return catalogue.register(new ProviderBundle(provider)).getProvider();
    }

    public Provider update(String id, Provider provider) {
        ProviderBundle bundle = repository.get(id);
        bundle.setProvider(provider);
        return catalogue.update(id, bundle).getProvider();
    }

    public void delete(String id) {
        catalogue.delete(id);
    }

    public Provider get(String id) {
        return repository.get(id).getProvider();
    }

    public Paging<Provider> get(Pageable pageable) {
        return repository.get(PagingUtils.toFacetFilter(pageable, repository.getResourceTypeName())).map(ProviderBundle::getProvider);
    }

    public Paging<Provider> get(FacetFilter filter) {
        return repository.get(filter).map(ProviderBundle::getProvider);
    }

    public ProviderBundle verify(String id, String status, Boolean active) {
        ProviderBundle bundle = repository.get(id);
        bundle.setActive(active);
        bundle.setStatus(status);
        return catalogue.update(id, bundle);
    }

    public ProviderBundle activate(String id, Boolean active) {
        ProviderBundle bundle = repository.get(id);
        bundle.setActive(active);
        return catalogue.update(id, bundle);
    }

    public ProviderBundle onboard(ProviderBundle provider, Authentication auth) {
        // create LoggingInfo
        String catalogueId = provider.getProvider().getCatalogueId();
        List<LoggingInfo> loggingInfoList = new ArrayList<>();
        loggingInfoList.add(LoggingInfo.createLoggingInfoEntry(User.of(auth).getEmail(), User.of(auth).getFullName(), securityService.getRoleName(auth),
                LoggingInfo.Types.ONBOARD.getKey(), LoggingInfo.ActionType.REGISTERED.getKey()));
        provider.setLoggingInfo(loggingInfoList);
        if (catalogueId == null) {
            // set catalogueId = eosc
            provider.getProvider().setCatalogueId(catalogueName);
            provider.setActive(false);
//            provider.setStatus(vocabularyService.get("pending provider").getId());
//            provider.setTemplateStatus(vocabularyService.get("no template status").getId());
            provider.setStatus("pending provider");
            provider.setTemplateStatus("no template status");
        } else {
            checkCatalogueIdConsistency(provider, catalogueId);
            provider.setActive(true);
//            provider.setStatus(vocabularyService.get("approved provider").getId());
//            provider.setTemplateStatus(vocabularyService.get("approved template").getId());
            provider.setStatus("approved provider");
            provider.setTemplateStatus("approved template");
            loggingInfoList.add(LoggingInfo.createLoggingInfoEntry(User.of(auth).getEmail(), User.of(auth).getFullName(), securityService.getRoleName(auth),
                    LoggingInfo.Types.ONBOARD.getKey(), LoggingInfo.ActionType.APPROVED.getKey()));
        }

        // latestOnboardingInfo
        provider.setLatestOnboardingInfo(loggingInfoList.get(loggingInfoList.size()-1));

        return provider;
    }

    private void checkCatalogueIdConsistency(ProviderBundle provider, String catalogueId){
//        catalogueService.existsOrElseThrow(catalogueId); // FIXME
        if (provider.getProvider().getCatalogueId() == null || provider.getProvider().getCatalogueId().equals("")){
//            throw new ValidationException("Provider's 'catalogueId' cannot be null or empty");
            throw new RuntimeException("Provider's 'catalogueId' cannot be null or empty");
        } else{
            if (!provider.getProvider().getCatalogueId().equals(catalogueId)){
//                throw new ValidationException("Parameter 'catalogueId' and Provider's 'catalogueId' don't match");
                throw new RuntimeException("Parameter 'catalogueId' and Provider's 'catalogueId' don't match");
            }
        }
    }
}
