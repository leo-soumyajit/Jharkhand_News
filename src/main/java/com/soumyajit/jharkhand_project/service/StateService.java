package com.soumyajit.jharkhand_project.service;

import com.soumyajit.jharkhand_project.dto.CreateStateRequest;
import com.soumyajit.jharkhand_project.dto.StateDto;
import com.soumyajit.jharkhand_project.entity.State;
import com.soumyajit.jharkhand_project.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateService {

    private final StateRepository stateRepository;
    private final ModelMapper modelMapper;

    @Cacheable(value = "states", key = "'all'")
    public List<StateDto> getAllStates() {
        List<State> states = stateRepository.findAll();
        return states.stream()
                .map(state -> modelMapper.map(state, StateDto.class))
                .collect(Collectors.toList());
    }

    public StateDto createState(CreateStateRequest request) {
        State state = new State();
        state.setName(request.getName());
        state.setCode(request.getCode());

        State savedState = stateRepository.save(state);
        return modelMapper.map(savedState, StateDto.class);
    }
}
