package com.concesionaria.app.web.rest;

import com.concesionaria.app.service.UserService;
import com.concesionaria.app.service.dto.ActiveUserDTO;
import com.concesionaria.app.service.dto.UserDTO;
import com.concesionaria.app.repository.UserRepository;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api")
public class PublicUserResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList("id", "login", "firstName", "lastName", "email", "activated", "langKey")
    );

    private static final Logger LOG = LoggerFactory.getLogger(PublicUserResource.class);

    private final UserService userService;
    private final UserRepository userRepository;

    public PublicUserResource(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * {@code GET /users} : get all users with only public information - calling this method is allowed for anyone.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllPublicUsers(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all public User names");
        if (!onlyContainsAllowedProperties(pageable)) {
            return ResponseEntity.badRequest().build();
        }

        final Page<UserDTO> page = userService.getAllPublicUsers(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    @GetMapping("/users/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActiveUserDTO>> getActiveUsers() {
        LOG.debug("REST request to get active users for operator selections");
        List<ActiveUserDTO> users = userRepository
            .findAllByIdNotNullAndActivatedIsTrue(org.springframework.data.domain.PageRequest.of(0, 500, Sort.by("login").ascending()))
            .map(user -> {
                ActiveUserDTO dto = new ActiveUserDTO();
                dto.setId(user.getId());
                dto.setLogin(user.getLogin());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());
                dto.setEmail(user.getEmail());
                dto.setActivated(user.isActivated());
                return dto;
            })
            .getContent();
        return ResponseEntity.ok(users);
    }
}
