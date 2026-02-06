package com.sobolev.travel.service;

import com.sobolev.travel.dto.geography.SearchHistoryDto;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final EntityMapper mapper;

    public SearchHistoryService(SearchHistoryRepository searchHistoryRepository, EntityMapper mapper) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryDto> getUserSearchHistory(Integer userId) {
        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId).stream()
            .map(mapper::toSearchHistoryDto)
            .toList();
    }

    @Transactional
    public void clearUserSearchHistory(Integer userId) {
        searchHistoryRepository.deleteByUserId(userId);
    }
}
