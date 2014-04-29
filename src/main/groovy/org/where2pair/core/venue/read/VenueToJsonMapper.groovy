package org.where2pair.core.venue.read

import groovy.transform.Immutable

@Immutable
class VenueToJsonMapper {

    OpenHoursToJsonMapper openHoursToJsonMapper

    Map<String, ?> toJson(Venue venue) {
        [
            id: venue.id.toString(),
            name: venue.name ?: '',
            location: [
                    latitude: venue.location.lat,
                    longitude: venue.location.lng
            ],
            address: [
                    addressLine1: venue.address.addressLine1 ?: '',
                    addressLine2: venue.address.addressLine2 ?: '',
                    addressLine3: venue.address.addressLine3 ?: '',
                    city: venue.address.city ?: '',
                    postcode: venue.address.postcode ?: '',
                    phoneNumber: venue.address.phoneNumber ?: ''
            ],
            openHours: openHoursToJsonMapper.asOpenHoursJson(venue.weeklyOpeningTimes),
            facilities: venue.facilities.collect()
        ]
    }

    List<Map<String, ?>> toJson(List<Venue> venues) {
        venues.collect { toJson(it) }
    }
}
