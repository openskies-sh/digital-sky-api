package com.ispirit.digitalsky.service;

import com.ispirit.digitalsky.domain.AirspaceCategory;
import com.ispirit.digitalsky.domain.UserPrincipal;
import com.ispirit.digitalsky.dto.Errors;
import com.ispirit.digitalsky.exception.EntityNotFoundException;
import com.ispirit.digitalsky.exception.ValidationException;
import com.ispirit.digitalsky.repository.AirspaceCategoryRepository;
import com.ispirit.digitalsky.service.api.AirspaceCategoryService;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AirspaceCategoryServiceImpl implements AirspaceCategoryService {

    private AirspaceCategoryRepository airspaceCategoryRepository;

    public AirspaceCategoryServiceImpl(AirspaceCategoryRepository airspaceCategoryRepository) {
        this.airspaceCategoryRepository = airspaceCategoryRepository;
    }

    @Override
    public AirspaceCategory createNewAirspaceCategory(AirspaceCategory airspaceCategory) {
        validatePolygonGeometry(airspaceCategory);
        airspaceCategory.setCreatedDate(LocalDateTime.now());
        airspaceCategory.setModifiedDate(LocalDateTime.now());
        airspaceCategory.setCreatedById(UserPrincipal.securityContext().getId());
        airspaceCategory.setModifiedById(UserPrincipal.securityContext().getId());
        return airspaceCategoryRepository.save(airspaceCategory);
    }

    @Override
    public AirspaceCategory updateAirspaceCategory(long id, AirspaceCategory airspaceCategory) {
        AirspaceCategory currentEntity = find(id);
        validatePolygonGeometry(airspaceCategory);
        currentEntity.setModifiedById(UserPrincipal.securityContext().getId());
        currentEntity.setModifiedDate(LocalDateTime.now());
        currentEntity.setName(airspaceCategory.getName());
        currentEntity.setType(airspaceCategory.getType());
        currentEntity.setGeoJson(airspaceCategory.getGeoJson());
        currentEntity.setGeoJsonString(airspaceCategory.getGeoJsonString());
        return airspaceCategoryRepository.save(currentEntity);
    }

    @Override
    public AirspaceCategory find(long id) {
        AirspaceCategory airspaceCategory = airspaceCategoryRepository.findOne(id);
        if (airspaceCategory == null) {
            throw new EntityNotFoundException("AirspaceCategory", id);
        }
        return airspaceCategory;
    }

    @Override
    public List<AirspaceCategory> findAll() {
        Iterable<AirspaceCategory> categories = airspaceCategoryRepository.findAll();
        List<AirspaceCategory> result = new ArrayList<>();
        for (AirspaceCategory category : categories) {
            result.add(category);
        }
        result.sort((o1, o2) -> o1.getType() != o2.getType() ? (o1.getType().getLayerOrder() - o2.getType().getLayerOrder()) : o1.getName().compareTo(o2.getName()));
        return result;
    }

    private void validatePolygonGeometry(AirspaceCategory airspaceCategory) {
        FeatureCollection featureCollection = (FeatureCollection) airspaceCategory.getGeoJson();
        for (Feature feature : featureCollection.getFeatures()) {
            if (!(feature.getGeometry() instanceof Polygon)) {
                throw new ValidationException(new Errors("Only polygon features accepted"));
            }
        }
    }
}
