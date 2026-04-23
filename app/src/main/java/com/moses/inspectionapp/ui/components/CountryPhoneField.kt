package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.moses.inspectionapp.data.validator.InputValidators
import com.moses.inspectionapp.ui.theme.AppColors
import com.moses.inspectionapp.ui.theme.LocalAppSpacing
import java.util.Locale

private data class PhoneCountry(
    val isoCode: String,
    val displayName: String,
    val dialCode: Int,
) {
    val flag: String
        get() = isoCode.uppercase(Locale.getDefault())
            .take(2)
            .map { char -> Character.toChars(char.code + 127397).concatToString() }
            .joinToString("")
}

@Composable
fun CountryPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    defaultCountryIso: String? = null,
) {
    val countries = remember { phoneCountries() }
    val defaultIso = remember(countries, defaultCountryIso) {
        val requested = defaultCountryIso?.uppercase(Locale.getDefault()).orEmpty()
        val localeIso = Locale.getDefault().country.uppercase(Locale.getDefault())
        when {
            countries.any { it.isoCode == requested } -> requested
            countries.any { it.isoCode == localeIso } -> localeIso
            countries.any { it.isoCode == "RW" } -> "RW"
            else -> countries.firstOrNull()?.isoCode.orEmpty()
        }
    }

    var selectedIso by rememberSaveable { mutableStateOf(defaultIso) }
    var nationalDigits by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = countries.firstOrNull { it.isoCode == selectedIso } ?: countries.firstOrNull()
    val displayNumber = remember(selectedIso, nationalDigits) {
        formatAsYouType(selectedIso, nationalDigits)
    }
    val sp = LocalAppSpacing.current

    LaunchedEffect(value) {
        if (value.isBlank()) {
            if (nationalDigits.isNotEmpty()) {
                nationalDigits = ""
            }
            return@LaunchedEffect
        }
        val parsed = parsePhoneValue(value)
        if (parsed != null) {
            if (countries.any { it.isoCode == parsed.first } && parsed.first != selectedIso) {
                selectedIso = parsed.first
            }
            if (parsed.second != nationalDigits) {
                nationalDigits = parsed.second
            }
        }
    }

    LaunchedEffect(selectedIso, nationalDigits) {
        val e164 = buildE164(selectedIso, nationalDigits, countries)
        if (e164 != value) {
            onValueChange(e164)
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.width(132.dp)) {
                Surface(
                    color = AppColors.InputSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isError) AppColors.AccentRed else AppColors.BorderLight,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = sp.inputHeight)
                        .clickable(enabled = enabled && countries.isNotEmpty()) { expanded = true },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = selectedCountry?.flag.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = selectedCountry?.let { "+${it.dialCode}" }.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            tint = AppColors.TextSecondary,
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .widthIn(min = 260.dp, max = 320.dp)
                        .background(AppColors.CardSurface),
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${country.flag} ${country.displayName} (+${country.dialCode})",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            onClick = {
                                selectedIso = country.isoCode
                                expanded = false
                            },
                        )
                    }
                }
            }

            StyledTextField(
                value = displayNumber,
                onValueChange = { typed ->
                    nationalDigits = typed.filter { it.isDigit() }.take(15)
                },
                label = label,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = enabled,
                isError = isError,
                errorText = null,
                modifier = Modifier.weight(1f),
            )
        }

        if (isError && !errorText.isNullOrBlank()) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.AccentRed,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

private fun phoneCountries(): List<PhoneCountry> {
    val util = PhoneNumberUtil.getInstance()
    return util.supportedRegions
        .mapNotNull { region ->
            val iso = region.uppercase(Locale.getDefault())
            val dial = runCatching { util.getCountryCodeForRegion(iso) }.getOrDefault(0)
            if (dial <= 0) return@mapNotNull null
            val name = Locale("", iso).getDisplayCountry(Locale.getDefault()).ifBlank { iso }
            PhoneCountry(
                isoCode = iso,
                displayName = name,
                dialCode = dial,
            )
        }
        .distinctBy { it.isoCode }
        .sortedBy { it.displayName.lowercase(Locale.getDefault()) }
}

private fun parsePhoneValue(value: String): Pair<String, String>? {
    val normalized = InputValidators.normalizePhone(value)
    if (normalized.isBlank()) return null
    val util = PhoneNumberUtil.getInstance()
    return try {
        val parsed = util.parse(normalized, null)
        val iso = util.getRegionCodeForNumber(parsed)?.uppercase(Locale.getDefault()) ?: return null
        val national = parsed.nationalNumber.toString()
        iso to national
    } catch (_: Exception) {
        null
    }
}

private fun formatAsYouType(countryIso: String, digits: String): String {
    if (digits.isBlank()) return ""
    val util = PhoneNumberUtil.getInstance()
    val formatter = util.getAsYouTypeFormatter(countryIso.ifBlank { "RW" })
    var formatted = ""
    digits.forEach { digit ->
        formatted = formatter.inputDigit(digit)
    }
    return formatted.ifBlank { digits }
}

private fun buildE164(
    countryIso: String,
    nationalDigits: String,
    countries: List<PhoneCountry>,
): String {
    if (nationalDigits.isBlank()) return ""
    val util = PhoneNumberUtil.getInstance()
    val selected = countries.firstOrNull { it.isoCode == countryIso }
    val dialCode = selected?.dialCode ?: util.getCountryCodeForRegion(countryIso)
    if (dialCode <= 0) return nationalDigits
    val raw = "+$dialCode$nationalDigits"
    return try {
        val parsed = util.parse(raw, null)
        util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
    } catch (_: Exception) {
        raw
    }
}
