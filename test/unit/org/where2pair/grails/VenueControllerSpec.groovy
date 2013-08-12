package org.where2pair.grails

import grails.converters.JSON
import grails.test.mixin.*

import org.skyscreamer.jsonassert.JSONAssert
import org.where2pair.Coordinates
import org.where2pair.Venue
import org.where2pair.WeeklyOpeningTimesBuilder

import spock.lang.Specification

@TestFor(VenueController)
class VenueControllerSpec extends Specification {

	static final String VENUE_NAME = 'my venue'
    static final long VENUE_ID = 1L
    GormVenueRepository gormVenueRepository = Mock()
	VenueToJsonConverter venueConverter = Mock()

    def "should show the specified venue"() {
        given:
        request.method = 'GET'
        Venue venue = new Venue(name: VENUE_NAME)
        Map venueJson = [name: VENUE_NAME]
        gormVenueRepository.get(VENUE_ID) >> venue
        venueConverter.asVenueJson(venue) >> venueJson

        when:
        controller.show(VENUE_ID)

        then:
        response.text.equalToJsonOf(venueJson)
    }

    def "should show 404 if venue not found"() {
        request.method = 'GET'
        Venue venue = new Venue(name: VENUE_NAME)
        gormVenueRepository.get(VENUE_ID) >> null

        when:
        controller.show(VENUE_ID)

        then:
        response.status == 404
		response.text == "Venue with id $VENUE_ID could not be found"
    }

	def "should show all venues"() {
		given:
		request.method = 'GET'
        List venues = 100.venues()
		List venuesJson = 100.venuesJson()
		gormVenueRepository.getAll() >> venues
        venueConverter.asVenuesJson(venues) >> venuesJson

		when:
		controller.showAll()

		then:
		response.text.equalToJsonOf(venuesJson)
		response.status == 200
	}
	
	def "should save new venues"() {
		given:
		request.method = 'POST'
		VenueDto venueDto = new VenueDto(
				name: 'name',
				latitude: 1.0,
				longitude: 0.1,
				addressLine1: 'addressLine1',
				addressLine2: 'addressLine2',
				addressLine3: 'addressLine3',
				city: 'city',
				postcode: 'postcode',
				phoneNumber: '01234567890',
				openHours: [monday: [
						[openHour: 12, openMinute: 0, closeHour: 18, closeMinute: 30]
					],
					tuesday: [
						[openHour: 8, openMinute: 0, closeHour: 11, closeMinute: 0]
					]],
				features: ['wifi', 'mobile payments']
				)
		request.json = venueDto

		when:
		controller.save()

		then:
		1 * gormVenueRepository.save(venueDto)
		response.text.equalToJsonOf(venueDto)
		response.status == 200
	}

	def setup() {
		controller.gormVenueRepository = gormVenueRepository
		controller.venueConverter = venueConverter
		String.mixin(JSONMatcher)
		Integer.mixin(VenuesMixin)
	}

	def cleanup() {
		String.metaClass = null
		Integer.metaClass = null
	}

	@Category(String)
	static class JSONMatcher {
		boolean equalToJsonOf(Object object) {
			JSONAssert.assertEquals(new JSON(object).toString(), this, false)
			true
		}
	}

	@Category(Integer)
	static class VenuesMixin {
		List venues() {
			(0..this).collect {
				new Venue(location: new Coordinates(1.0, 0.5),
				weeklyOpeningTimes: new WeeklyOpeningTimesBuilder().build())
			}
		}

        List venuesJson() {
            (0..this).collect {
                [:]
            }
        }
	}
}