package br.com.fiap.epictask.controller.api;

import java.net.URI;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.fiap.epictask.model.Task;
import br.com.fiap.epictask.model.User;
import br.com.fiap.epictask.repository.UserRepository;

@RestController
@RequestMapping("/api/user")
public class ApiUserController {
	
	@Autowired
	private UserRepository repository;
	
	@GetMapping
	@Cacheable("users")
	public Page<User> index(@RequestParam(required = false) String name, @PageableDefault(size = 4) Pageable pageable){
		
		if(name == null) return repository.findAll(pageable);
		
		return repository.findByName("%" + name + "%", pageable);
	}
	
	
	@PostMapping
	@CacheEvict(value = "users", allEntries = true)
	public ResponseEntity<User> create(@Valid @RequestBody User user, UriComponentsBuilder uriBuilder){
		repository.save(user);
		URI uri = uriBuilder.path("/api/user/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}
	
	
	@GetMapping("{id}")
	public ResponseEntity<User> get(@PathVariable Long id) {
		return ResponseEntity.of(repository.findById(id));
	}
	
	
	@DeleteMapping("{id}")
	@CacheEvict(value = "users", allEntries = true)
	public ResponseEntity<User> delete(@PathVariable Long id){
		Optional<User> user = repository.findById(id);
		if(user.isEmpty()) return ResponseEntity.notFound().build();
		
		repository.deleteById(id);
		
		return ResponseEntity.ok().build();
	}
	
	
	@PutMapping("{id}")
	@CacheEvict(value = "users", allEntries = true)
	public ResponseEntity<User> update(@RequestBody User newUser, @PathVariable Long id){
		Optional<User> optional = repository.findById(id);
		
		if(optional.isEmpty())
			return ResponseEntity.notFound().build();
		
		User user = optional.get();
		
		user.setEmail(newUser.getEmail());
		user.setName(newUser.getName());
		user.setPassword(newUser.getPassword());
		
		repository.save(user);
		
		return ResponseEntity.ok(user);
	}
	

}
