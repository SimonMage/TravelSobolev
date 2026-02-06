package com.sobolev.travel.service;

import com.sobolev.travel.dto.geography.TagDto;
import com.sobolev.travel.mapper.EntityMapper;
import com.sobolev.travel.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servizio per operazioni sui tag (categorie).
 */
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final EntityMapper mapper;

    public TagService(TagRepository tagRepository, EntityMapper mapper) {
        this.tagRepository = tagRepository;
        this.mapper = mapper;
    }

    /**
     * Restituisce tutti i tag presenti nel sistema.
     *
     * @return lista di {@link TagDto} rappresentanti tutti i tag
     */
    @Transactional(readOnly = true)
    public List<TagDto> getAllTags() {
        return tagRepository.findAll().stream()
            .map(mapper::toTagDto)
            .toList();
    }
}
