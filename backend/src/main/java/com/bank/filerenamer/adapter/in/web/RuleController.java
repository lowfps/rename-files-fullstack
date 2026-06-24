package com.bank.filerenamer.adapter.in.web;

import com.bank.filerenamer.adapter.in.web.dto.RuleRequest;
import com.bank.filerenamer.adapter.in.web.dto.RuleResponse;
import com.bank.filerenamer.domain.port.in.ManageRulesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Administración del catálogo de reglas (CRUD + versionamiento). */
@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final ManageRulesUseCase manageRules;

    public RuleController(ManageRulesUseCase manageRules) {
        this.manageRules = manageRules;
    }

    @GetMapping
    public List<RuleResponse> list() {
        return manageRules.listRules().stream().map(RuleResponse::from).toList();
    }

    @GetMapping("/{id}")
    public RuleResponse get(@PathVariable Long id) {
        return RuleResponse.from(manageRules.getRule(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleResponse create(@Valid @RequestBody RuleRequest request) {
        return RuleResponse.from(manageRules.createRule(request.toCommand()));
    }

    @PutMapping("/{id}")
    public RuleResponse update(@PathVariable Long id, @Valid @RequestBody RuleRequest request) {
        return RuleResponse.from(manageRules.updateRule(id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    public RuleResponse deactivate(@PathVariable Long id) {
        return RuleResponse.from(manageRules.deactivateRule(id));
    }

    @GetMapping("/{id}/versions")
    public List<RuleResponse> versions(@PathVariable Long id) {
        return manageRules.getRuleVersions(id).stream().map(RuleResponse::from).toList();
    }
}
