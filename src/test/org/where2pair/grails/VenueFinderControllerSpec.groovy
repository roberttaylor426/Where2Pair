package org.where2pair.grails

import static org.where2pair.DayOfWeek.FRIDAY
import static org.where2pair.DayOfWeek.MONDAY
import static org.where2pair.DayOfWeek.SUNDAY
import static org.where2pair.DayOfWeek.THURSDAY
import static org.where2pair.DayOfWeek.WEDNESDAY
import grails.converters.JSON
import grails.test.mixin.*

import org.skyscreamer.jsonassert.JSONAssert
import org.where2pair.Address
import org.where2pair.Coordinates
import org.where2pair.FeaturesCriteria
import org.where2pair.OpenTimesCriteria
import org.where2pair.TimeProvider
import org.where2pair.Venue
import org.where2pair.VenueFinder
import org.where2pair.VenueRepository
import org.where2pair.VenueWithDistance
import org.where2pair.WeeklyOpeningTimesBuilder
import org.where2pair.DailyOpeningTimes.SimpleTime

import spock.lang.Specification
import spock.lang.Unroll

@TestFor(VenueFinderController)
class VenueFinderControllerSpec extends Specification {

	static final TIME_NOW = new SimpleTime(1, 2)
	static final TODAY = FRIDAY
	VenueFinder venueFinder = Mock()
	VenueRepository venueRepository = Mock()
	TimeProvider timeProvider = Mock()
	VenueJsonMarshaller venueJsonMarshaller = Mock() {
		asVenuesWithDistanceJson(_) >> [[:]]
	}
	
	def "should display search results for given coordinates"() {
		given:
		controller.params.'location1' = '1.0,0.1'

		when:
		controller.findNearest()

		then:
		1 *	venueFinder.findNearestTo(_,_,new Coordinates(1.0,0.1))
		response.status == 200
	}

	def "should support multiple supplied locations"() {
		given:
		(1..1000).each { controller.params."location$it" = '1.0,0.1'}
		List expectedCoordArgs = [new Coordinates(1.0,0.1)] * 1000

		when:
		controller.findNearest()

		then:
		1 * venueFinder.findNearestTo(_,_,expectedCoordArgs)
	}

	def "should reject more than 1000 supplied locations"() {
		given:
		(1..1001).each { controller.params."location$it" = '1.0,0.1'}

		when:
		controller.findNearest()


		then:
		response.text == "Only upto 1000 locations are supported at this time."
		response.status == 413
	}

	def "should reject 0 supplied locations"() {
		when:
		controller.findNearest()

		then:
		response.text == "Missing locations from the request parameters. I expect a query in the form: findNearest?location1=x1,y1&location2=x2,y2..."
		response.status == 413
	}

	@Unroll
	def "given openFrom: #openFromParam openUntil: #openUntilParam openDay: #openDayParam should find venues open during correct time range"() {
		given:
		controller.params.'location1' = '1.0,0.1'
		if (openFromParam != 'missing') controller.params.'openFrom' = openFromParam
		if (openUntilParam != 'missing') controller.params.'openUntil' = openUntilParam
		if (openDayParam != 'missing') controller.params.'openDay' = openDayParam
		
		when:
		controller.findNearest()
		
		then:
		1 * venueFinder.findNearestTo({ OpenTimesCriteria criteria ->
			criteria.openFrom == expectedOpenFrom
			criteria.openUntil == expectedOpenUntil
			criteria.dayOfWeek == expectedOpenDay
		}, _, _)
		
		where:
		openFromParam 	| openUntilParam 	| openDayParam  | expectedOpenFrom 			| expectedOpenUntil 		| expectedOpenDay
		'missing'		| 'missing'			| 'missing'		| TIME_NOW					| TIME_NOW					| TODAY
		'13.30'			| 'missing'			| 'missing'		| new SimpleTime(13, 30)	| new SimpleTime(13, 30)	| TODAY
		'missing'		| '13.30'			| 'missing'		| TIME_NOW					| new SimpleTime(13, 30)	| TODAY
		'13.30'			| '18.45'			| 'missing'		| new SimpleTime(13, 30)	| new SimpleTime(18, 45)	| TODAY
		'13.30'			| 'missing'			| 'monday'		| new SimpleTime(13, 30)	| new SimpleTime(13, 30)	| MONDAY
		'missing'		| '18.45'			| 'wednesday'	| TIME_NOW					| new SimpleTime(18, 45)	| WEDNESDAY
		'13.30'			| '18.45'			| 'thursday'	| new SimpleTime(13, 30)	| new SimpleTime(18, 45)	| THURSDAY
		'missing'		| 'missing'			| 'sunday'		| new SimpleTime(0, 0)		| new SimpleTime(35, 59)	| SUNDAY
	}

	def "parses features from request and uses them to find venues"() {
		given:
		controller.params.'location1' = '1.0,0.1'
		controller.params.'withFeatures' = 'wifi,baby_changing'
		
		when:
		controller.findNearest()
		
		then:
		1 * venueFinder.findNearestTo(_, { FeaturesCriteria criteria ->
			criteria.requestedFeatures == ['wifi', 'baby_changing'] as HashSet
		}, _)
	}
		
	def setup() {
		request.method = 'GET'
		timeProvider.timeNow() >> TIME_NOW
		timeProvider.today() >> TODAY
		controller.venueFinder = venueFinder
		controller.venueRepository = venueRepository
		controller.venueJsonMarshaller = venueJsonMarshaller
		controller.timeProvider = timeProvider
		Integer.mixin(VenuesMixin)
	}

	def cleanup() {
		Integer.metaClass = null
	}

	@Category(Integer)
	static class VenuesMixin {
		List venuesWithDistance() {
			(0..this).collect {
				new VenueWithDistance(venue: new Venue(
					location: new Coordinates(1.0, 0.5),
					weeklyOpeningTimes: new WeeklyOpeningTimesBuilder().build(), 
					address: new Address()),
				distanceInKm: 10.5)
			}
		}
	}
}