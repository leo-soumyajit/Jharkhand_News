package com.soumyajit.jharkhand_project.service;


import com.soumyajit.jharkhand_project.dto.CreateDistrictRequest;
import com.soumyajit.jharkhand_project.dto.DistrictDto;
import com.soumyajit.jharkhand_project.entity.District;
import com.soumyajit.jharkhand_project.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final ModelMapper modelMapper;

    @Cacheable(value = "districts", key = "'all'")
    public List<DistrictDto> getAllDistricts() {
        List<District> districts = districtRepository.findAll();
        return districts.stream()
                .map(district -> modelMapper.map(district, DistrictDto.class))
                .collect(Collectors.toList());
    }
    public DistrictDto createDistrict(CreateDistrictRequest request) {
        District district = new District();
        district.setName(request.getName());
        district.setCode(request.getCode());

        District savedDistrict = districtRepository.save(district);
        return modelMapper.map(savedDistrict, DistrictDto.class);
    }

}
