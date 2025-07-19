package com.estate.domain.service.impl;

import com.estate.domain.entity.Setting;
import com.estate.domain.entity.Standing;
import com.estate.domain.entity.User;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.enumaration.Role;
import com.estate.domain.enumaration.SettingCode;
import com.estate.repository.SettingRepository;
import com.estate.repository.StandingRepository;
import com.estate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InitDataService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final StandingRepository standingRepository;
    private final SettingRepository settingRepository;

    @Override
    public void run(String... args) throws Exception {
        User user = userRepository.findByEmail("gatienjordanlonlaep@gmail.com").orElse(new User());
        if(StringUtils.isBlank(user.getId())){
            user.setEmail("gatienjordanlonlaep@gmail.com");
            user.setFirstName("LONLA");
            user.setLastName("Gatien Jordan");
            user.setPhone("+237 695463868");
            user.setProfil(Profil.STAFF);
            user.setPassword(new BCryptPasswordEncoder().encode("admin"));
            user.setRoles(Collections.singletonList(Role.ROLE_ADMIN));
            userRepository.save(user);
        }

        Standing standing = standingRepository.findByName("HAUT").orElse(new Standing());
        if(StringUtils.isBlank(standing.getId())) {
            standing.setName("HAUT");
            standing.setRent(20000);
            standing.setCaution(50000);
            standing.setRepair(35000);
            standing.setDescription("Lit d'une place & demi, paillasse pour laver les assiettes, placard, table d'étude et chaise, bidet (pot), colonne à douche et robinet");
            standingRepository.save(standing);
        }

        standing = standingRepository.findByName("MOYEN").orElse(new Standing());
        if(StringUtils.isBlank(standing.getId())) {
            standing.setName("MOYEN");
            standing.setRent(12000);
            standing.setCaution(34000);
            standing.setRepair(22000);
            standing.setDescription("Lit d'une place & demi, placard, table d'étude et chaise, toilettes externes pour 02 à 03 max personnes ( pot bien confectionné )");
            standingRepository.save(standing);
        }


        List<Setting> settings = new ArrayList<>(Arrays.asList(
            new Setting(SettingCode.ORANGE_MONEY_MERCHANT_CODE, ""),
            new Setting(SettingCode.ORANGE_MONEY_MERCHANT_NAME, ""),
            new Setting(SettingCode.MTN_MOBILE_MONEY_MERCHANT_CODE, ""),
            new Setting(SettingCode.MTN_MOBILE_MONEY_MERCHANT_NAME, ""),
            new Setting(SettingCode.BANK_NAME, ""),
            new Setting(SettingCode.BANK_ACCOUNT_NAME, ""),
            new Setting(SettingCode.BANK_ACCOUNT_NUMBER,""),
            new Setting(SettingCode.PAYPAL_LINK, ""),
            new Setting(SettingCode.TELEPHONE_PUBLIC, ""),
            new Setting(SettingCode.WHATSAPP, ""),
            new Setting(SettingCode.LANDLORD_NAME, ""),
            new Setting(SettingCode.LANDLORD_CARD_ID, ""),
            new Setting(SettingCode.LANDLORD_ADDRESS, ""),
            new Setting(SettingCode.LANDLORD_PHONE, ""),
            new Setting(SettingCode.CITY_CATEGORY, "")
        ));
        settingRepository.saveAll(settings.stream().filter(setting -> !settingRepository.existsByCode(setting.getCode())).collect(Collectors.toList()));
        List<SettingCode> codes = settings.stream().map(Setting::getCode).collect(Collectors.toList());
        settings = settingRepository.findAll().stream().filter(setting -> !codes.contains(setting.getCode())).collect(Collectors.toList());
        settingRepository.deleteAll(settings);
    }
}
