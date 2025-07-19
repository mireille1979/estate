package com.estate.domain.converter;

import com.estate.domain.enumaration.Mode;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class ModeListConverter implements AttributeConverter<List<Mode>, String> {
    @Override
    public String convertToDatabaseColumn(List<Mode> modes) {
        return ListUtils.defaultIfNull(modes, Collections.emptyList()).stream().map(Mode::name).collect(Collectors.joining(";"));
    }

    @Override
    public List<Mode> convertToEntityAttribute(String s) {
        Mode[] values = Mode.values();
        List<Mode> responsibilities = new ArrayList<>();
        for(Mode value: values){
            if(StringUtils.contains(s, value.name())) responsibilities.add(value);
        }
        return responsibilities;
    }
}
