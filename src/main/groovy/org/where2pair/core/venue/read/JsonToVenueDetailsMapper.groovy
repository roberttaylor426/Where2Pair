package org.where2pair.core.venue.read

import org.where2pair.core.venue.common.Coordinates

class JsonToVenueDetailsMapper {

    private final JsonToOpenHoursMapper jsonToOpenHoursMapper = new JsonToOpenHoursMapper()

    VenueDetails toVenueDetails(Map<String, ?> json) {
        String name = json.name
        Coordinates location = coordinatesFrom(json.location)
        Address address = addressFrom(json.address)
        WeeklyOpeningTimes weeklyOpeningTimes = jsonToOpenHoursMapper.asWeeklyOpeningTimes(json.openHours)
        Set<String> facilities = json.facilities
        new VenueDetails(name, location, address, weeklyOpeningTimes, facilities)
    }

    private Coordinates coordinatesFrom(Map<String, ?> json) {
        double latitude = json.latitude
        double longitude = json.longitude
        new Coordinates(latitude, longitude)
    }

    private Address addressFrom(Map<String, String> json) {
        String addressLine1 = json.addressLine1
        String addressLine2 = json.addressLine2
        String addressLine3 = json.addressLine3
        String postcode = json.postcode
        String city = json.city
        String phoneNumber = json.phoneNumber
        new Address(addressLine1, addressLine2, addressLine3, city, postcode, phoneNumber)
    }
}