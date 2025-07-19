package com.estate.domain.converter;

import com.estate.domain.enumaration.Role;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class RoleListConverter implements AttributeConverter<List<Role>, String> {
    @Override
    public String convertToDatabaseColumn(List<Role> roles) {
        return ListUtils.defaultIfNull(roles, Collections.emptyList()).stream().map(Role::name).collect(Collectors.joining(";"));
    }

    @Override
    public List<Role> convertToEntityAttribute(String s) {
        Role[] values = Role.values();
        List<Role> privileges = new ArrayList<>();
        for(Role value: values){
            if(StringUtils.contains(s, value.name())) privileges.add(value);
        }
        return privileges;
    }
}
