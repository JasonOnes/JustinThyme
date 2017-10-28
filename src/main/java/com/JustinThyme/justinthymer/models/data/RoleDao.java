package com.JustinThyme.justinthymer.models.data;

import com.JustinThyme.justinthymer.models.forms.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RoleDao extends CrudRepository<Role, Integer>{
}

