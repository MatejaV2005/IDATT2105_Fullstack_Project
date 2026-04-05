package com.grimni.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grimni.domain.MappingPointResponsibleUser;
import com.grimni.domain.ids.MappingPointResponsibleUserId;

public interface MappingPointResponsibleUserRepository extends JpaRepository<MappingPointResponsibleUser, MappingPointResponsibleUserId>  {

}
