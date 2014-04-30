package org.where2pair.infra.venue.write

import org.where2pair.core.venue.write.InvalidVenueJsonException
import org.where2pair.core.venue.write.NewVenueService
import org.where2pair.infra.venue.web.StatusCode
import spock.lang.Specification

import static org.where2pair.core.venue.VenueIdBuilder.aVenueId
import static org.where2pair.core.venue.read.VenueBuilder.aVenue

class NewVenueControllerSpec extends Specification {

    def newVenueService = Mock(NewVenueService)
    def controller = new NewVenueController(newVenueService: newVenueService)
    def venueJson = aVenue().toJson()

    def 'saves new venues and returns venue id'() {
        given:
        def expectedVenueId = aVenueId().build()
        newVenueService.save(venueJson) >> expectedVenueId

        when:
        def response = controller.save(venueJson)

        then:
        response.responseBody == expectedVenueId.toString()
        response.statusCode == StatusCode.OK
    }

    def 'returns the error message if there was a problem whilst saving'() {
        given:
        newVenueService.save(venueJson) >> { throw new InvalidVenueJsonException('Could not parse Json') }

        when:
        def response = controller.save(venueJson)

        then:
        response.responseBody == 'Could not parse Json'
        response.statusCode == StatusCode.BAD_REQUEST
    }
}
