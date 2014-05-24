package org.where2pair.write.venue

import static org.where2pair.common.venue.NewVenueIdBuilder.aRandomVenueId
import static org.where2pair.common.venue.NewVenueIdBuilder.aVenueId

import spock.lang.Specification

class NewVenueIdSpec extends Specification {

    def 'provides unique string representation'() {
        given:
        List<NewVenueId> venueIds = []
        1000.times { venueIds << aRandomVenueId() }

        when:
        List<String> encodedVenueIds = venueIds*.encode()

        then:
        encodedVenueIds.unique().size() == 1000
    }

    def 'equivalent venue ids should encode the same'() {
        given:
        def venueIdBuilder = aVenueId()
        def venueId1 = venueIdBuilder.build()
        def venueId2 = venueIdBuilder.build()

        when:
        def encodedVenueId1 = venueId1.encode()
        def encodedVenueId2 = venueId2.encode()

        then:
        encodedVenueId1 == encodedVenueId2
    }
}
