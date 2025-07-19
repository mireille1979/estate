package com.estate.domain.form;

import com.estate.domain.helper.SmsHelper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class Phone {
    @NotBlank
    private String code = "+237";
    @Pattern(regexp = "[0-9]*", message = "uniquement les chiffres")
    private String number;

    public String format(){
        return StringUtils.isBlank(number) ? "" : code + " " + number;
    }

    public static Phone parse(String telephone){
        Phone phone = new Phone();
        if(telephone != null) {
            String[] parts = telephone.trim().split(" ");
            phone.setCode(SmsHelper.countryCodes.contains(parts[0]) ? parts[0] : "+237");
            if(parts.length > 1) {
                phone.setNumber(parts[1]);
            } else {
                phone.setNumber("");
            }
        } else {
            phone.setCode("+237");
            phone.setNumber("");
        }
        return phone;
    }
}
