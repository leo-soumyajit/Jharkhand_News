package com.soumyajit.jharkhand_project.config;

import com.soumyajit.jharkhand_project.dto.CreatePropertyRequest;
import com.soumyajit.jharkhand_project.entity.Property;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setAmbiguityIgnored(true);

        modelMapper.createTypeMap(CreatePropertyRequest.class, Property.class)
                .addMappings(mapper -> {
                    mapper.skip(Property::setStatus); // PostStatus - set manually to PENDING
                    mapper.skip(Property::setAuthor); // User - set manually
                    mapper.skip(Property::setImageUrls); // Upload separately
                    mapper.skip(Property::setFloorPlanUrls); // Upload separately
                    mapper.skip(Property::setCreatedAt); // Auto-generated
                    mapper.skip(Property::setUpdatedAt); // Auto-generated
                    mapper.skip(Property::setViewCount); // Default value
                    mapper.skip(Property::setState); // Set manually to "Jharkhand"
                });

        return modelMapper;
    }
}
