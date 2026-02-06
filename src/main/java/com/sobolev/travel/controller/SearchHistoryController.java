package com.sobolev.travel.controller;

import com.sobolev.travel.dto.geography.SearchHistoryDto;
import com.sobolev.travel.security.CustomUserDetails;
import com.sobolev.travel.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller per le operazioni sulla cronologia di ricerca dell'utente.
 * Endpoint protetti che richiedono autenticazione (bearer JWT).
 */
@RestController
@RequestMapping("/api/search-history")
@Tag(name = "Search History", description = "User search history endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    public SearchHistoryController(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @GetMapping
    @Operation(summary = "Get user search history")
    public ResponseEntity<List<SearchHistoryDto>> getSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(searchHistoryService.getUserSearchHistory(userDetails.getId()));
    }

    @DeleteMapping
    @Operation(summary = "Clear user search history")
    public ResponseEntity<Void> clearSearchHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        searchHistoryService.clearUserSearchHistory(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
