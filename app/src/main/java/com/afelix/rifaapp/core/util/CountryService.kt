package com.afelix.rifaapp.core.util

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

data class Country(
    val isoCode: String,
    val name: String,
    val dialCode: String,
    val flag: String
)

object CountryService {
    private val phoneUtil = PhoneNumberUtil.getInstance()

    val allCountries: List<Country> by lazy {
        val countries = mutableListOf<Country>()
        val supportedRegions = phoneUtil.supportedRegions
        
        for (region in supportedRegions) {
            val dialCode = phoneUtil.getCountryCodeForRegion(region)
            val locale = Locale("", region)
            val name = locale.getDisplayCountry(Locale("es", "CO"))
            
            if (name.isNotEmpty()) {
                countries.add(
                    Country(
                        isoCode = region,
                        name = name,
                        dialCode = "+$dialCode",
                        flag = getFlagEmoji(region)
                    )
                )
            }
        }
        countries.sortedBy { it.name }
    }

    private fun getFlagEmoji(countryCode: String): String {
        if (countryCode.length != 2) return ""
        val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
}
