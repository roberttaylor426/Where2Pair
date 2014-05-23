package org.where2pair.common.venue

import static CoordinatesBuilder.coordinates
import static java.util.UUID.randomUUID

import org.where2pair.write.venue.NewVenueId

class NewVenueIdBuilder {

    private String name = 'Starbucks'
    private CoordinatesBuilder coordinatesBuilder = coordinates().withLatitude(1.0).withLongitude(0.1)
    private String addressLine1 = '9 Appold Street'

    static NewVenueIdBuilder aVenueId() {
        new NewVenueIdBuilder()
    }

    static NewVenueId aRandomVenueId() {
        aVenueId().withName(randomUUID() as String)
                .withLongitude(randomDouble())
                .withLatitude(new Random().nextDouble())
                .withAddressLine1(randomUUID() as String)
                .build()
    }

    private static double randomDouble() {
        new Random().nextDouble()
    }

    NewVenueIdBuilder withName(String name) {
        this.name = name
        this
    }

    NewVenueIdBuilder withLatitude(double latitude) {
        coordinatesBuilder.withLatitude(latitude)
        this
    }

    NewVenueIdBuilder withLongitude(double longitude) {
        coordinatesBuilder.withLongitude(longitude)
        this
    }

    NewVenueIdBuilder withAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1
        this
    }

    NewVenueId build() {
        new NewVenueId(name, coordinatesBuilder.build(), addressLine1)
    }
}

