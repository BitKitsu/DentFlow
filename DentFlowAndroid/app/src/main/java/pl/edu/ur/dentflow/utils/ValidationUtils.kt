package pl.edu.ur.dentflow.utils

object ValidationUtils {
    val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
    val PHONE_REGEX = Regex("^\\+?[0-9][\\s\\-]?([0-9][\\s\\-]?){8,14}$")
    val NAME_REGEX  = Regex("^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\s\\-]{2,50}$")
    val ZIP_REGEX   = Regex("^[0-9]{5}$")

    fun isEmailValid(email: String) = EMAIL_REGEX.matches(email)
    fun isPhoneValid(phone: String) = PHONE_REGEX.matches(phone)
    fun isNameValid(name: String) = NAME_REGEX.matches(name)
    fun isZipValid(zip: String) = ZIP_REGEX.matches(zip)
    fun isStreetValid(street: String) = street.length >= 3
    fun isCityValid(city: String) = city.length >= 2
    fun isCountryValid(country: String) = country.length >= 2
    fun isPasswordValid(password: String) = password.length >= 8
}
